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
 * In the keyword optimization process, a {@link RoundStrategy} is used to go from one round to the
 * other and to define when the process should stop.
 */
public interface RoundStrategy {
  /**
   * Takes the population from the current round and uses the other given parameters to create the
   * population for the next round. This method is called until isFinished returns true and should
   * return <code>null</code> in this is the case.
   *
   * @param currentPopulation the current keyword population
   * @param alternativesFinder for providing keyword alternatives for the best keywords
   * @param evaluator for evaluating the current population
   * @return next round's keyword population
   * @throws KeywordOptimizerException in case of an error while generating the new population
   */
  KeywordCollection nextRound(KeywordCollection currentPopulation,
      AlternativesFinder alternativesFinder, Evaluator evaluator) throws KeywordOptimizerException;

  /**
   * Determines if the process is finished or not.
   *
   * @param currentPopulation the current keyword population
   * @return should the process be stopped or continued
   * @throws KeywordOptimizerException in case of an error determining if the search is finished
   */
  boolean isFinished(KeywordCollection currentPopulation) throws KeywordOptimizerException;
}
