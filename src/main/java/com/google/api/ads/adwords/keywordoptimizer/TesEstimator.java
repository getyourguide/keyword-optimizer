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
import com.google.api.ads.adwords.axis.v201603.cm.Criterion;
import com.google.api.ads.adwords.axis.v201603.o.AdGroupEstimateRequest;
import com.google.api.ads.adwords.axis.v201603.o.CampaignEstimateRequest;
import com.google.api.ads.adwords.axis.v201603.o.KeywordEstimate;
import com.google.api.ads.adwords.axis.v201603.o.KeywordEstimateRequest;
import com.google.api.ads.adwords.axis.v201603.o.TrafficEstimatorResult;
import com.google.api.ads.adwords.axis.v201603.o.TrafficEstimatorSelector;
import com.google.api.ads.adwords.axis.v201603.o.TrafficEstimatorService;
import com.google.api.ads.adwords.axis.v201603.o.TrafficEstimatorServiceInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * This estimator uses the {@link TrafficEstimatorService} of the AdWords API to create traffic
 * estimates. It is the only implementation of a {@link TrafficEstimator} provided by this utility,
 * but can be exchanged for your own implementation.
 */
public class TesEstimator implements TrafficEstimator {
  private TrafficEstimatorServiceInterface tes;
  private Long clientCustomerId;

  /**
   * Creates a new {@link TesEstimator}.
   *
   * @param context holding shared objects during the optimization process
   */
  public TesEstimator(OptimizationContext context) {
    tes = context.getAdwordsApiUtil().getService(TrafficEstimatorServiceInterface.class);
    clientCustomerId = context.getAdwordsApiUtil().getClientCustomerId();
  }

  /**
   * Creates the TES request for a list of {@link KeywordCollection}.
   *
   * @param keywords the {@link KeywordCollection} to be evaluated
   * @return a {@link TrafficEstimatorSelector} to use for TES requests
   */
  private static TrafficEstimatorSelector createSelector(KeywordCollection keywords) {
    if (keywords == null || keywords.size() == 0) {
      throw new IllegalArgumentException("Need at least one keyword");
    }

    // First create requests for individual keywords.
    List<KeywordEstimateRequest> keywordEstimateRequests = new ArrayList<KeywordEstimateRequest>();

    for (KeywordInfo keywordInfo : keywords.getListSortedByKeyword()) {
      KeywordEstimateRequest keywordEstimateRequest = new KeywordEstimateRequest();
      keywordEstimateRequest.setKeyword(keywordInfo.getKeyword());
      keywordEstimateRequests.add(keywordEstimateRequest);
    }

    // Now wrap all that in an ad group request.
    List<AdGroupEstimateRequest> adGroupEstimateRequests = new ArrayList<AdGroupEstimateRequest>();
    AdGroupEstimateRequest adGroupEstimateRequest = new AdGroupEstimateRequest();

    // Convert the list of keywords into array.
    adGroupEstimateRequest.setKeywordEstimateRequests(keywordEstimateRequests
        .toArray(new KeywordEstimateRequest[] {}));
    adGroupEstimateRequest.setMaxCpc(keywords.getMaxCpc());
    adGroupEstimateRequests.add(adGroupEstimateRequest);


    // Wrap into campaign request (and add additional criteria).
    List<CampaignEstimateRequest> campaignEstimateRequests =
        new ArrayList<CampaignEstimateRequest>();
    CampaignEstimateRequest campaignEstimateRequest = new CampaignEstimateRequest();

    // Convert the list of ad groups into an array.
    campaignEstimateRequest.setAdGroupEstimateRequests(adGroupEstimateRequests
        .toArray(new AdGroupEstimateRequest[] {}));
    campaignEstimateRequest.setCriteria(keywords.getAdditionalCriteria()
        .toArray(new Criterion[] {}));

    campaignEstimateRequests.add(campaignEstimateRequest);

    TrafficEstimatorSelector selector =
        new TrafficEstimatorSelector(
            campaignEstimateRequests.toArray(new CampaignEstimateRequest[] {}));
    return selector;
  }

  /**
   * Create {@link KeywordCollection} from a given TES result.
   *
   * @param result the result from the TES
   * @param keywords the {@link KeywordCollection} to be evaluated
   * @return a {@link KeywordCollection} containing the a copy of the given {@link KeywordInfo}s,
   *         with {@link TrafficEstimate}s
   */
  private static KeywordCollection createEstimates(TrafficEstimatorResult result,
      KeywordCollection keywords) {
    if (result == null || result.getCampaignEstimates() == null
        || result.getCampaignEstimates().length != 1
        || result.getCampaignEstimates()[0].getAdGroupEstimates() == null
        || result.getCampaignEstimates()[0].getAdGroupEstimates().length == 0
        || result.getCampaignEstimates()[0].getAdGroupEstimates()[0].getKeywordEstimates() 
           == null) {
      throw new IllegalArgumentException("The given result is invalid, it cannot be null and must "
          + "have exactly one campaign estimate with exactly one ad group estimate");
    }
    List<KeywordInfo> sortedKeywords = keywords.getListSortedByKeyword();

    KeywordCollection estimates = new KeywordCollection(keywords);

    KeywordEstimate[] keywordEstimates =
        result.getCampaignEstimates()[0].getAdGroupEstimates()[0].getKeywordEstimates();
    for (int i = 0; i < keywordEstimates.length; i++) {
      KeywordInfo originalKeyword = sortedKeywords.get(i);
      KeywordInfo estimate =
          new KeywordInfo(
              originalKeyword.getKeyword(), new TrafficEstimate(keywordEstimates[i]), null);
      estimates.add(estimate);
    }
    return estimates;
  }

  @Override
  public KeywordCollection estimate(KeywordCollection keywords) throws KeywordOptimizerException {
    try {
      // If there are no keywords in list, return empty estimate.
      if (keywords.size() == 0) {
        KeywordCollection emptyEstimates = new KeywordCollection(keywords);
        return emptyEstimates;
      }

      final TrafficEstimatorSelector selector = createSelector(keywords);
      TrafficEstimatorResult result = AwapiRateLimiter.getInstance()
          .run(new AwapiCall<TrafficEstimatorResult>() {
            @Override
            public TrafficEstimatorResult invoke() throws ApiException, RemoteException {
              return tes.get(selector);
            }
          }, clientCustomerId);
      KeywordCollection estimates = createEstimates(result, keywords);

      return estimates;
    } catch (ApiException e) {
      throw new KeywordOptimizerException("Problem while querying traffic estimator service", e);
    } catch (RemoteException e) {
      throw new KeywordOptimizerException("Problem while connecting to the AdWords API", e);
    }
  }
}
