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
 * Enum representing all properties that are used in this project (with their names as defined in
 * the .properties file)
 */
public enum KeywordOptimizerProperty {
  
  AlternativesFinderClass("optimizer.alternativesFinder"),
  EstimatorClass("optimizer.estimator"),
  ScoreCalculatorClass("optimizer.scoreCalculator"),
  ScoreCalculatorFormula("optimizer.scoreCalculator.formula"),
  RoundStrategyClass("optimizer.roundStrategy"),
  RoundStrategyMaxSteps("optimizer.roundStrategy.maxSteps"),
  RoundStrategyMinImprovementBetweenSteps("optimizer.roundStrategy.minImprovement"),
  RoundStrategyMaxPopulation("optimizer.roundStrategy.maxPopulation"),
  RoundStrategyReplicateBest("optimizer.roundStrategy.replicateBest");
  
  private final String propertyName;

  private KeywordOptimizerProperty(String propertyName) {
    this.propertyName = propertyName;
  }

  /**
   * Returns the name of the property as defined in the .properties file.
   */
  public String getName() {
    return propertyName;
  }
}
