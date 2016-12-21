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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for this utility, responsible for coordination the whole keyword optimization process.
 * It works as follows:
 * <ol>
 *   <li>First, the given {@link SeedGenerator} is used to create a set of seed keywords (a.k.a. the
 * population)</li>
 *   <li>This list is evaluated by the given {@link Evaluator}, which assigns a score to each
 * keyword in the population)</li>
 *   <li>The keywords with the highest score are used to derive new, similar ones (using the given
 * {@link AlternativesFinder}), which will most likely have high scores as well</li>
 *   <li>The keywords with the lowest score will be dropped from the current population</li>
 *   <li>The process repeats from 2. until a certain end criterion (as defined by the {@link
 * RoundStrategy}) is reached</li>
 * </ol>
 */
public class Optimizer {
  private static final Logger logger = LoggerFactory.getLogger(Optimizer.class);

  private final SeedGenerator seedGenerator;
  private final AlternativesFinder alternativesFinder;
  private final Evaluator evaluator;
  private final RoundStrategy roundStrategy;

  /**
   * Creates a new {@link Optimizer} based on the given parameters.
   * 
   * @param seedGenerator used to create the initial keyword population
   * @param alternativesFinder used to derive keyword alternatives from the best keywords
   * @param evaluator used to assign a score to each keyword
   * @param roundStrategy used to determine how to go from round to round and when to end the
   *                      process
   */
  public Optimizer(SeedGenerator seedGenerator, AlternativesFinder alternativesFinder,
      Evaluator evaluator, RoundStrategy roundStrategy) {
    this.seedGenerator = seedGenerator;
    this.alternativesFinder = alternativesFinder;
    this.evaluator = evaluator;
    this.roundStrategy = roundStrategy;
  }

  /**
   * Executes the keyword optimization process based on the parameters given in the constructors.
   * 
   * @return A {@link KeywordCollection}, a list of {@link KeywordInfo}s) of the keywords that were
   *         found / optimized during the process, with their traffic estimates and quality scores
   * @throws KeywordOptimizerException in case of an error during the optimization process
   */
  public KeywordCollection optimize() throws KeywordOptimizerException {
    KeywordCollection seedKeywords = seedGenerator.generate();

    KeywordCollection seedPopulation = evaluator.evaluate(seedKeywords);
    KeywordCollection currentPopulation = seedPopulation;
    int currentStep = 0;

    logStatus(currentPopulation, currentStep);

    while (!roundStrategy.isFinished(currentPopulation)) {
      currentStep++;

      currentPopulation = roundStrategy.nextRound(currentPopulation, alternativesFinder, evaluator);
      logStatus(currentPopulation, currentStep);
    }

    return currentPopulation;
  }

  /**
   * Dumps the status of the current round to the logger.
   * 
   * @param currentPopulation the current set of keywords
   * @param currentStep the current search step
   */
  private static void logStatus(KeywordCollection currentPopulation, int currentStep) {
    logger.info(
        "--- Optimization step {} (Avg: {}) ---", currentStep, currentPopulation.getAverageScore());

    for (KeywordInfo evaluation : currentPopulation.getListSortedByScore()) {
      logger.debug(
          "{} -> {}",
          KeywordOptimizerUtil.toString(evaluation.getKeyword()),
          evaluation.getScore());
    }
  }
}
