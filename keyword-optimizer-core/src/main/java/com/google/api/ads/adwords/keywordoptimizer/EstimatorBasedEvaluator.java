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

/**
 * Evaluates {@link KeywordCollection} by first obtaining an {@link TrafficEstimate} for each
 * keyword and then rating them using a {@link ScoreCalculator}.
 */
public class EstimatorBasedEvaluator implements Evaluator {
  private final TrafficEstimator estimator;
  private final ScoreCalculator scoreCalculator;

  /**
   * Creates a new {@link EstimatorBasedEvaluator} that uses the given arguments for calculating the
   * score.
   * 
   * @param estimator An {@link TrafficEstimator} to retrieve traffic estimates from
   * @param scoreCalculator A {@link ScoreCalculator} to derive a score from these estimates
   */
  public EstimatorBasedEvaluator(TrafficEstimator estimator, ScoreCalculator scoreCalculator) {
    this.estimator = estimator;
    this.scoreCalculator = scoreCalculator;
  }

  @Override
  public KeywordCollection evaluate(KeywordCollection keywords) throws KeywordOptimizerException {
    KeywordCollection evaluations = new KeywordCollection(keywords.getCampaignConfiguration());

    KeywordCollection estimates = estimator.estimate(keywords);

    for (KeywordInfo estimate : estimates) {
      double score = scoreCalculator.calculate(estimate.getTrafficEstimate());

      KeywordInfo evaluation =
          new KeywordInfo(
              estimate.getKeyword(),
              estimate.getIdeaEstimate(),
              estimate.getTrafficEstimate(),
              score);
      evaluations.add(evaluation);
    }

    return evaluations;
  }
}
