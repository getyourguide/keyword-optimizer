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
import com.google.api.ads.adwords.axis.v201607.o.AttributeType;
import com.google.api.ads.adwords.axis.v201607.o.CategoryProductsAndServicesSearchParameter;
import com.google.api.ads.adwords.axis.v201607.o.IdeaType;
import com.google.api.ads.adwords.axis.v201607.o.RequestType;
import com.google.api.ads.adwords.axis.v201607.o.SearchParameter;
import com.google.api.ads.adwords.axis.v201607.o.TargetingIdeaSelector;
import com.google.api.ads.adwords.axis.v201607.o.TargetingIdeaService;
import com.google.api.ads.adwords.axis.v201607.o.TargetingIdeaServiceInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creates a set of seed keywords derived from a given products and services category using the
 * {@link TargetingIdeaService} (see
 * https://developers.google.com/adwords/api/docs/appendix/productsservices).
 */
public class TisCategorySeedGenerator extends TisBasedSeedGenerator {
  private final int categoryId;

  /**
   * Creates a new {@link TisCategorySeedGenerator} using the given category id (see
   * https://developers.google.com/adwords/api/docs/appendix/productsservices).
   *
   * @param tis the API interface to the TargetingIdeaService
   * @param clientCustomerId the AdWords customer ID
   * @param categoryId category id to be used
   * @param matchTypes match types to be used for seed keyword creation
   * @param campaignConfiguration additional campaign-level settings for keyword evaluation
   */
  public TisCategorySeedGenerator(
      TargetingIdeaServiceInterface tis,
      Long clientCustomerId,
      int categoryId,
      Set<KeywordMatchType> matchTypes,
      CampaignConfiguration campaignConfiguration) {
    super(tis, clientCustomerId, matchTypes, campaignConfiguration);
    this.categoryId = categoryId;
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
    CategoryProductsAndServicesSearchParameter categoryParameter =
        new CategoryProductsAndServicesSearchParameter();
    categoryParameter.setCategoryId(categoryId);
    searchParameters.add(categoryParameter);

    // Now add all other criteria.
    searchParameters.addAll(
        KeywordOptimizerUtil.toSearchParameters(
            getCampaignConfiguration().getAdditionalCriteria()));

    selector.setSearchParameters(searchParameters.toArray(new SearchParameter[] {}));

    return selector;
  }
}
