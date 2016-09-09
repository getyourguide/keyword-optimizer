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

import com.google.api.ads.adwords.axis.v201607.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201607.cm.Paging;
import com.google.api.ads.adwords.axis.v201607.o.IdeaType;
import com.google.api.ads.adwords.axis.v201607.o.RelatedToQuerySearchParameter;
import com.google.api.ads.adwords.axis.v201607.o.RequestType;
import com.google.api.ads.adwords.axis.v201607.o.SearchParameter;
import com.google.api.ads.adwords.axis.v201607.o.TargetingIdeaSelector;
import com.google.api.ads.adwords.axis.v201607.o.TargetingIdeaServiceInterface;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a set of seed keywords derived from a list of example search terms.
 */
public class TisSearchTermsSeedGenerator extends TisBasedSeedGenerator {
  private final Set<String> seedKeywords;

  /**
   * Creates a new {@link TisSearchTermsSeedGenerator}. Please note that example keywords have to be
   * added separately.
   *
   * @param tis the API interface to the TargetingIdeaService
   * @param clientCustomerId the AdWords customer ID
   * @param matchTypes match types to be used for seed keyword creation
   * @param campaignConfiguration additional campaign-level settings for keyword evaluation
   */
  public TisSearchTermsSeedGenerator(
      TargetingIdeaServiceInterface tis,
      Long clientCustomerId,
      Set<KeywordMatchType> matchTypes,
      CampaignConfiguration campaignConfiguration) {
    super(tis, clientCustomerId, matchTypes, campaignConfiguration);
    seedKeywords = new HashSet<String>();
  }

  @Override
  protected TargetingIdeaSelector getSelector() {
    TargetingIdeaSelector selector = new TargetingIdeaSelector();
    selector.setRequestType(RequestType.IDEAS);
    selector.setIdeaType(IdeaType.KEYWORD);
    selector.setPaging(new Paging(0, PAGE_SIZE));
    selector.setRequestedAttributeTypes(KeywordOptimizerUtil.TIS_ATTRIBUTE_TYPES);

    List<SearchParameter> searchParameters = new ArrayList<SearchParameter>();

    // Get ideas related to query search parameter.
    RelatedToQuerySearchParameter relatedToQuerySearchParameter =
        new RelatedToQuerySearchParameter();
    relatedToQuerySearchParameter.setQueries(seedKeywords.toArray(new String[] {}));
    searchParameters.add(relatedToQuerySearchParameter);

    // Now add all other criteria.
    searchParameters.addAll(
        KeywordOptimizerUtil.toSearchParameters(getCampaignConfiguration().getAdditionalCriteria()));

    selector.setSearchParameters(searchParameters.toArray(new SearchParameter[] {}));

    return selector;
  }

  /**
   * Adds a search term to the generator.
   * 
   * @param keyword the search term to the generator to be added
   */
  public void addSearchTerm(String keyword) {
    seedKeywords.add(keyword);
  }
}
