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

import com.google.api.ads.adwords.axis.v201603.cm.Paging;
import com.google.api.ads.adwords.axis.v201603.o.AttributeType;
import com.google.api.ads.adwords.axis.v201603.o.IdeaType;
import com.google.api.ads.adwords.axis.v201603.o.RelatedToUrlSearchParameter;
import com.google.api.ads.adwords.axis.v201603.o.RequestType;
import com.google.api.ads.adwords.axis.v201603.o.SearchParameter;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdeaSelector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a set of seed keywords derived from the content of a list of given URLs.
 */
public class TisUrlSeedGenerator extends TisBasedSeedGenerator {
  private final Set<String> urls;

  /**
   * Creates a new {@link TisUrlSeedGenerator}. Please note that URLs have to be added separately.
   *
   * @param context holding shared objects during the optimization process
   * @param campaignConfiguration additional campaign-level settings for keyword evaluation
   */
  public TisUrlSeedGenerator(
      OptimizationContext context, CampaignConfiguration campaignConfiguration) {
    super(context, campaignConfiguration);
    urls = new HashSet<String>();
  }

  @Override
  protected TargetingIdeaSelector getSelector() {
    TargetingIdeaSelector selector = new TargetingIdeaSelector();
    selector.setRequestType(RequestType.IDEAS);
    selector.setIdeaType(IdeaType.KEYWORD);
    selector.setPaging(new Paging(0, PAGE_SIZE));

    selector.setRequestedAttributeTypes(new AttributeType[] {AttributeType.KEYWORD_TEXT});

    List<SearchParameter> searchParameters = new ArrayList<SearchParameter>();

    // Get ideas related to query search parameter.
    RelatedToUrlSearchParameter relatedToUrlSearchParameter = new RelatedToUrlSearchParameter();
    relatedToUrlSearchParameter.setUrls(urls.toArray(new String[] {}));
    searchParameters.add(relatedToUrlSearchParameter);

    // Now add all other criteria.
    searchParameters.addAll(
        KeywordOptimizerUtil.toSearchParameters(getCampaignConfiguration().getAdditionalCriteria()));

    selector.setSearchParameters(searchParameters.toArray(new SearchParameter[] {}));

    return selector;
  }

  /**
   * Adds an URL to the list of URLs that are scanned for content / keywords.
   * 
   * @param url the URL to be added
   */
  public void addUrl(String url) {
    urls.add(url);
  }
}
