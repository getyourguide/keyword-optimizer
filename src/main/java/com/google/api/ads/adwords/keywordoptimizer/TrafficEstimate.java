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
import com.google.api.ads.adwords.axis.v201607.o.KeywordEstimate;
import com.google.api.ads.adwords.axis.v201607.o.StatsEstimate;
import com.google.api.ads.adwords.axis.v201607.o.TrafficEstimatorService;
import org.apache.commons.lang.SystemUtils;

/**
 * Represents a combined traffic estimate for a single {@link Keyword}, consisting of minimum, mean
 * and maximum traffic statistics.
 */
public class TrafficEstimate {
  private final StatsEstimate min;
  private final StatsEstimate mean;
  private final StatsEstimate max;

  /**
   * Creates a new combined estimate by specifying all attributes.
   * 
   * @param min the minimum estimated statistics
   * @param mean the mean estimated statistics
   * @param max the maximum estimated statistics
   */
  public TrafficEstimate(StatsEstimate min, StatsEstimate mean, StatsEstimate max) {
    this.min = min;
    this.mean = mean;
    this.max = max;
  }

  /**
   * Creates a new combined estimate from a {@link KeywordEstimate} (wrapping min and max values).
   * 
   * @param keywordEstimate the keyword estimate, as given by the {@link TrafficEstimatorService}
   */
  public TrafficEstimate(KeywordEstimate keywordEstimate) {
    this.min = keywordEstimate.getMin();
    this.mean =
        KeywordOptimizerUtil.calculateMean(keywordEstimate.getMin(), keywordEstimate.getMax());
    this.max = keywordEstimate.getMax();
  }

  /**
   * Creates a new combined estimate by specifying minimum and maximum estimates (mean is
   * automatically calculated).
   * 
   * @param min the minimum estimated statistics
   * @param max the maximum estimated statistics
   */
  public TrafficEstimate(StatsEstimate min, StatsEstimate max) {
    this.min = min;
    this.mean = KeywordOptimizerUtil.calculateMean(min, max);
    this.max = max;
  }

  /**
   * Returns the minimum estimated statistics.
   */
  public StatsEstimate getMin() {
    return min;
  }

  /**
   * Returns the mean estimated statistics.
   */
  public StatsEstimate getMean() {
    return mean;
  }

  /**
   * Returns the maximum estimated statistics.
   */
  public StatsEstimate getMax() {
    return max;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();

    out.append("- Min:  ").append(KeywordOptimizerUtil.toString(min))
        .append(SystemUtils.LINE_SEPARATOR);
    out.append("- Mean: ").append(KeywordOptimizerUtil.toString(mean))
        .append(SystemUtils.LINE_SEPARATOR);
    out.append("- Max:  ").append(KeywordOptimizerUtil.toString(max));

    return out.toString();
  }
}
