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

import com.google.api.ads.adwords.axis.v201607.cm.Money;
import com.google.api.ads.adwords.axis.v201607.o.MonthlySearchVolume;
import com.google.api.ads.adwords.axis.v201607.o.TargetingIdeaService;
import com.google.common.collect.ImmutableList;

/**
 * Represents estimated statistics for a keyword on Google Search as provided by the {@link
 * TargetingIdeaService}. See http://goo.gl/PqJ878.
 */
public class IdeaEstimate {
  
  // Instance representing an empty or unknown estimate. 
  public static final IdeaEstimate EMPTY_ESTIMATE =
      new IdeaEstimate(0, 0, KeywordOptimizerUtil.createMoney(0), new MonthlySearchVolume[0]);
  
  private final double competition;
  private final long searchVolume;
  private final Money averageCpc;
  private final ImmutableList<MonthlySearchVolume> targetedMonthlySearches;
  
  /** 
   * Returns the COMPETITION attribute from the {@link TargetingIdeaService}.
   */
  public double getCompetition() {
    return competition;
  }

  /** 
   * Returns the SEARCH_VOLUME attribute from the {@link TargetingIdeaService}.
   */
  public long getSearchVolume() {
    return searchVolume;
  }

  /** 
   * Returns the AVERAGE_CPC attribute from the {@link TargetingIdeaService}.
   */
  public Money getAverageCpc() {
    return averageCpc;
  }

  /** 
   * Returns the TARGETED_MONTHLY_SEARCHES attribute from the {@link TargetingIdeaService}.
   */
  public ImmutableList<MonthlySearchVolume> getTargetedMonthlySearches() {
    return targetedMonthlySearches;
  }

  /**
   * Create a new estimate based on the given arguments.
   *
   * @param competition the value returned as COMPETITION by the {@link TargetingIdeaService}
   * @param searchVolume the value returned as SEARCH_VOLUME by the {@link TargetingIdeaService}
   * @param averageCpc the value returned as AVERAGE_CPC by the {@link TargetingIdeaService}
   * @param targetedMonthlySearches the value returned as TARGETED_MONTHLY_SEARCHES by the {@link
   *     TargetingIdeaService}
   */
  public IdeaEstimate(
      double competition,
      long searchVolume,
      Money averageCpc,
      MonthlySearchVolume[] targetedMonthlySearches) {
    this.competition = competition;
    this.searchVolume = searchVolume;
    this.averageCpc = averageCpc;
    this.targetedMonthlySearches =
        targetedMonthlySearches == null
            ? ImmutableList.<MonthlySearchVolume>of()
            : ImmutableList.copyOf(targetedMonthlySearches);
  }
}
