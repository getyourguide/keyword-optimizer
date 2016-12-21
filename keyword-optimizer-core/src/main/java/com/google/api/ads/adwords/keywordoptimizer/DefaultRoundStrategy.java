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

import javax.annotation.Nullable;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the {@link RoundStrategy}, which defines how to change the current set
 * of {@link KeywordCollection} during the round-based optimization process from one round to
 * another. Every round, this strategy takes the best X keywords, derives new ones from it and adds
 * them to the list which is subsequently trimmed back to a maximum size. This is done until either
 * a maximum number of steps is reached or a minimum improvement could not be reached anymore.
 */
public class DefaultRoundStrategy implements RoundStrategy {
  private static final Logger logger = LoggerFactory.getLogger(RoundStrategy.class);

  private final Integer maxNumberOfSteps;
  private final Double minImprovementBetweenSteps;
  private final int maxPopulationSize;
  private final int maxNumberOfAlternatives;

  private int currentStep;
  private Double lastAvgScore;

  /**
   * Creates a new {@link DefaultRoundStrategy}.
   * 
   * @param maxNrSteps maximum number of round / steps (<code>null</code> for no restriction)
   * @param minImprovement minimum improvement of the average score to be reached until the process
   *                       stops (<code>null</code> for no restriction)
   * @param maxPopulationSize maximum size of the population at any time
   * @param replicateBestCount number of keywords to be used for replication
   */
  public DefaultRoundStrategy(@Nullable Integer maxNrSteps, @Nullable Double minImprovement,
      int maxPopulationSize, int replicateBestCount) {
    this.maxNumberOfSteps = maxNrSteps;
    this.minImprovementBetweenSteps = minImprovement;
    this.maxPopulationSize = maxPopulationSize;
    this.maxNumberOfAlternatives = replicateBestCount;
  
    lastAvgScore = null;
  }

  /**
   * Creates a new {@link DefaultRoundStrategy} and takes its parameters from a property file.
   */
  public DefaultRoundStrategy(OptimizationContext context) {
    Configuration config = context.getConfiguration();
  
    maxNumberOfSteps = config.getInt(KeywordOptimizerProperty.RoundStrategyMaxSteps.getName(), 10);
    minImprovementBetweenSteps = config.getDouble(
        KeywordOptimizerProperty.RoundStrategyMinImprovementBetweenSteps.getName(), 0);
    maxPopulationSize =
        config.getInt(KeywordOptimizerProperty.RoundStrategyMaxPopulation.getName(), 100);
    maxNumberOfAlternatives =
        config.getInt(KeywordOptimizerProperty.RoundStrategyReplicateBest.getName(), 10);
  
    lastAvgScore = null;
  }

  @Override
  public KeywordCollection nextRound(KeywordCollection currentPopulation,
      AlternativesFinder alternativesFinder, Evaluator evaluator) throws KeywordOptimizerException {
    if (isFinished(currentPopulation)) {
      return null;
    }

    // 1. Trim to max size (already remove worst X ones).
    KeywordCollection nextPopulation =
        currentPopulation.getBest(maxPopulationSize - maxNumberOfAlternatives);
    logger.info("- Trimmed population to " + nextPopulation.size());

    // 2. Replicate best ones.
    KeywordCollection bestKeywords = nextPopulation.getBest(maxNumberOfAlternatives);
    KeywordCollection alternativeKeywords = alternativesFinder.derive(bestKeywords);
    logger.info("- Found " + alternativeKeywords.size() + " keywords based on "
        + maxNumberOfAlternatives + " current best");

    // 3. Rate best ones.
    KeywordCollection evaluatedAlternatives = evaluator.evaluate(alternativeKeywords);

    // 4. Add the best ones to the list.
    for (KeywordInfo evaluation : evaluatedAlternatives) {
      if (!nextPopulation.contains(evaluation.getKeyword())) {
        nextPopulation.add(evaluation);
      }
    }
    logger.info("- Merged population, new size is " + nextPopulation.size());

    // 5. Trim population back to max size.
    nextPopulation = nextPopulation.getBest(maxPopulationSize);
    logger.info("- Trimmed population back to size " + nextPopulation.size());

    lastAvgScore = nextPopulation.getAverageScore();
    currentStep++;
    
    return nextPopulation;
  }

  @Override
  public boolean isFinished(KeywordCollection currentPopulation) {
    if (maxNumberOfSteps != null && currentStep >= maxNumberOfSteps) {
      return true;
    }

    if (minImprovementBetweenSteps != null && lastAvgScore != null) {
      double currentAvgScore = currentPopulation.getAverageScore();
      double improvement = (currentAvgScore - lastAvgScore) / lastAvgScore;

      if (improvement < minImprovementBetweenSteps) {
        return true;
      }
    }

    return false;
  }
}
