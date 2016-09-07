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
import javax.annotation.Nullable;
import org.apache.commons.lang.SystemUtils;

/**
 * Represents quality information about a keyword, namely the {@link Keyword} itself, a
 * {@link TrafficEstimate} and a score.
 */
public class KeywordInfo {
  private final Keyword keyword;
  private final TrafficEstimate estimate;
  private final Double score;

  /**
   * Creates a new {@link KeywordInfo} object with the given attributes.
   * 
   * @param keyword the keyword itself
   * @param estimate the estimated traffic statistics
   * @param score the quality score
   */
  public KeywordInfo(Keyword keyword, @Nullable TrafficEstimate estimate, @Nullable Double score) {
    this.keyword = keyword;
    this.estimate = estimate;
    this.score = score;
  }

  /**
   * Returns the keyword.
   */
  public Keyword getKeyword() {
    return keyword;
  }

  /**
   * Returns the estimated traffic statistics.
   */
  public TrafficEstimate getEstimate() {
    return estimate;
  }

  /**
   * Returns the quality score.
   */
  public Double getScore() {
    return score;
  }

  /**
   * Returns whether a traffic estimate has been set.
   */
  public boolean hasEstimate() {
    return estimate != null;
  }

  /**
   * Returns whether a quality score has been set.
   */
  public boolean hasScore() {
    return score != null;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();

    out.append(KeywordOptimizerUtil.toString(keyword));
    if (hasScore()) {
      out.append(": ").append(KeywordOptimizerUtil.format(score));
    }
    out.append(SystemUtils.LINE_SEPARATOR);
    if (hasEstimate()) {
      out.append(estimate);
    }

    return out.toString();
  }
}
