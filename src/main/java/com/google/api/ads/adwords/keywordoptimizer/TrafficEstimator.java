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

/**
 * Retrieves traffic estimates / statistics for given {@link KeywordCollection}. A
 * {@link TrafficEstimator} is usually used by an {@link Evaluator} to first obtain estimates and
 * then rate them using a {@link ScoreCalculator}.
 */
public interface TrafficEstimator {
  /**
   * Estimates the traffic for all given keywords (bulk requests). This method returns a copy of the
   * given {@link KeywordCollection} with new {@link KeywordInfo} that contain the {@link Keyword}
   * as well as the obtained {@link TrafficEstimate}.
   * 
   * @param keywords the {@link KeywordCollection} to be evaluated
   * @return {@link KeywordCollection} a copy of the given keyword infos, with
   *         {@link TrafficEstimate}s set
   * @throws KeywordOptimizerException in case of an error estimating they keyword traffic
   */
  KeywordCollection estimate(KeywordCollection keywords) throws KeywordOptimizerException;
}
