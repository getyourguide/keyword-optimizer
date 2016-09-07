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

import com.google.api.ads.adwords.axis.v201607.cm.Keyword;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link TrafficEstimator} uses an internal cache for storing {@link TrafficEstimate}s that
 * have been received already. It's basically a decorator around another {@link TrafficEstimator} in
 * order to increase efficiency.
 */
public class CachedEstimator implements TrafficEstimator {
  private static final Logger logger = LoggerFactory.getLogger(CachedEstimator.class);

  // Map storing traffic estimates by keyword.
  private final Map<Keyword, TrafficEstimate> cache;
  private final TrafficEstimator estimator;

  /**
   * Creates a new {@link CachedEstimator} around an existing {@link TrafficEstimator}.
   * 
   * @param estimator the nested {@link TrafficEstimator}, which will be used
   *                  whenever no entry is found in the cache
   */
  public CachedEstimator(TrafficEstimator estimator) {
    this.estimator = estimator;
    cache = new HashMap<Keyword, TrafficEstimate>();
  }

  @Override
  public KeywordCollection estimate(KeywordCollection keywords) throws KeywordOptimizerException {
    KeywordCollection cachedEstimates = new KeywordCollection(keywords.getCampaignConfiguration());
    KeywordCollection retrieveKeywords = new KeywordCollection(keywords.getCampaignConfiguration());

    // Check if there are any keywords already in the cache.
    for (Keyword keyword : keywords.getKeywords()) {
      TrafficEstimate cachedEstimate = cache.get(keyword);

      // Check if there is a cached entry related to that key which is equal to the keyword.
      if (cachedEstimate != null) {
        cachedEstimates.add(new KeywordInfo(keyword, cachedEstimate, null));
      } else {
        retrieveKeywords.add(new KeywordInfo(keyword, null, null));
      }
    }

    // Actually retrieve stats for all keywords that are not cached.
    KeywordCollection estimates = estimator.estimate(retrieveKeywords);
    for (KeywordInfo estimate : estimates) {
      cache.put(estimate.getKeyword(), estimate.getEstimate());
      estimates.add(estimate);
    }

    logger.info("Estimated " + keywords.size() + " keywords (" + cachedEstimates.size()
        + " cached, " + retrieveKeywords.size() + " retrieved)");

    return estimates;
  }
}
