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
 * Calculates a score derived from a previously obtained {@link TrafficEstimate}. A {@link
 * ScoreCalculator} is typically used by an {@link Evaluator} to calculate a score based on the 
 * {@link TrafficEstimate}s returned by a {@link TrafficEstimator}.
 */
public interface ScoreCalculator {
  /**
   * Calculates a score for a given {@link TrafficEstimate}.
   * 
   * @param estimate the {@link TrafficEstimate} to be evaluated
   * @return a score derived from the given estimate (higher scores are considered better)
   * @throws KeywordOptimizerException in case of a problem calculating the score
   */
  double calculate(TrafficEstimate estimate) throws KeywordOptimizerException;
}
