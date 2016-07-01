// Copyright 2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.adwords.keywordoptimizer;

import com.google.api.ads.adwords.axis.v201603.cm.ApiException;
import com.google.api.ads.adwords.axis.v201603.cm.Keyword;
import com.google.api.ads.adwords.axis.v201603.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201603.cm.Paging;
import com.google.api.ads.adwords.axis.v201603.o.Attribute;
import com.google.api.ads.adwords.axis.v201603.o.AttributeType;
import com.google.api.ads.adwords.axis.v201603.o.IdeaType;
import com.google.api.ads.adwords.axis.v201603.o.RelatedToQuerySearchParameter;
import com.google.api.ads.adwords.axis.v201603.o.RequestType;
import com.google.api.ads.adwords.axis.v201603.o.SearchParameter;
import com.google.api.ads.adwords.axis.v201603.o.StringAttribute;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdea;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdeaPage;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdeaSelector;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdeaService;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdeaServiceInterface;
import com.google.api.ads.common.lib.utils.Maps;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Uses the {@link TargetingIdeaService} to create new keyword alternatives. This works pretty much
 * the same way as the {@link TisSearchTermsSeedGenerator}, meaning it creates keywords based on a
 * given set of already existing ones.
 */
public class TisAlternativesFinder implements AlternativesFinder {
  private TargetingIdeaServiceInterface tis;
  private final Long clientCustomerId;

  /**
   * Creates a new {@link TisAlternativesFinder}.
   * 
   * @param context holding shared objects during the optimization process
   */
  public TisAlternativesFinder(OptimizationContext context) {
    tis = context.getAdwordsApiUtil().getService(TargetingIdeaServiceInterface.class);
    clientCustomerId = context.getAdwordsApiUtil().getClientCustomerId();
  }

  @Override
  public KeywordCollection derive(KeywordCollection keywords) throws KeywordOptimizerException {
    Collection<String> keywordTexts = getKeywordTexts(keywords);

    KeywordCollection alternatives = new KeywordCollection(keywords.getCampaignConfiguration());
    for (String keywordText : keywordTexts) {
      for (KeywordMatchType matchType : keywords.getContainingMatchTypes()) {
        Keyword newKeyword = KeywordOptimizerUtil.createKeyword(keywordText, matchType);
        alternatives.add(new KeywordInfo(newKeyword, null, null));
      }
    }

    return alternatives;
  }

  /**
   * Creates the selector for the {@link TargetingIdeaService} based on a given set of
   * {@link KeywordCollection}.
   * 
   * @param keywords the {@link KeywordCollection} to create the selector
   * @return the selector for the {@link TargetingIdeaService}
   */
  protected TargetingIdeaSelector getSelector(KeywordCollection keywords) {
    TargetingIdeaSelector selector = new TargetingIdeaSelector();
    selector.setRequestType(RequestType.IDEAS);
    selector.setIdeaType(IdeaType.KEYWORD);

    selector.setRequestedAttributeTypes(new AttributeType[] {AttributeType.KEYWORD_TEXT});

    List<SearchParameter> searchParameters = new ArrayList<SearchParameter>();

    // Get ideas related to query search parameter.
    RelatedToQuerySearchParameter relatedToQuerySearchParameter =
        new RelatedToQuerySearchParameter();
    relatedToQuerySearchParameter.setQueries(keywords.getContainingKeywordTexts().toArray(
        new String[] {}));
    searchParameters.add(relatedToQuerySearchParameter);

    // Now add all other criteria.
    searchParameters.addAll(KeywordOptimizerUtil.toSearchParameters(keywords.getCampaignConfiguration()
        .getAdditionalCriteria()));

    selector.setSearchParameters(searchParameters.toArray(new SearchParameter[] {}));

    return selector;
  }

  /**
   * Finds a collection of plain text keywords based on the given set of keywords.
   * 
   * @param keywords the keywords to as a basis for finding new ones
   * @return a collection of plain text keywords
   * @throws KeywordOptimizerException in case of an error retrieving keywords from TIS
   */
  protected Collection<String> getKeywordTexts(KeywordCollection keywords)
      throws KeywordOptimizerException {
    final TargetingIdeaSelector selector = getSelector(keywords);
    Collection<String> keywordTexts = new ArrayList<String>();

    int offset = 0;

    try {
      TargetingIdeaPage page = null;
      final AwapiRateLimiter rateLimiter =
          AwapiRateLimiter.getInstance(AwapiRateLimiter.RateLimitBucket.OTHERS);

      do {
        selector.setPaging(new Paging(offset, TisBasedSeedGenerator.PAGE_SIZE));
        page = rateLimiter.run(new AwapiCall<TargetingIdeaPage>() {
          @Override
          public TargetingIdeaPage invoke() throws ApiException, RemoteException {
            return tis.get(selector);
          }
        }, clientCustomerId);

        if (page.getEntries() != null) {
          for (TargetingIdea targetingIdea : page.getEntries()) {
            Map<AttributeType, Attribute> data = Maps.toMap(targetingIdea.getData());

            StringAttribute keyword = (StringAttribute) data.get(AttributeType.KEYWORD_TEXT);
            keywordTexts.add(keyword.getValue());
          }
        }
        offset += TisBasedSeedGenerator.PAGE_SIZE;
      } while (offset < page.getTotalNumEntries());

    } catch (ApiException e) {
      throw new KeywordOptimizerException("Problem while querying the targeting idea service", e);
    } catch (RemoteException e) {
      throw new KeywordOptimizerException("Problem while connecting to the AdWords API", e);
    }

    return keywordTexts;
  }
}
