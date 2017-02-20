// Copyright 2017 Google Inc. All Rights Reserved.
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

package com.google.api.ads.adwords.keywordoptimizer.formula;

import com.google.api.ads.adwords.keywordoptimizer.KeywordOptimizerProperty;
import com.google.api.ads.adwords.keywordoptimizer.OptimizationContext;
import com.google.api.ads.adwords.keywordoptimizer.ScoreCalculator;
import com.google.api.ads.adwords.keywordoptimizer.TrafficEstimate;
import com.google.common.annotations.VisibleForTesting;

/**
 * A score calculator based on a given formula defined as a string. The formula allows basic
 * arithmetic expressions:
 * <ul>
 *  <li>Constants (int or double)</li>
 *  <li>Basic operators (+, -, * and /)</li>
 *  <li>Brackets</li>
 *  <li>Variables filled during runtime (see {@link FormulaContext#init})</li>
 * </ul>
 * Valid formulas are (e.g.):
 * <ul>
 *  <li>5 (constant score of 5)</li>
 *  <li>(5 + 5) * 2 (constant score of 20)</li>
 *  <li>mean.clicksPerDay (average clicks per day)</li>
 *  <li>mean.clicksPerDay*0.9 + mean.impressionsPerDay*0.1 (weighted sum of 90% clicks and 10% 
 *      impressions)</li>
 *  <li>...</li>
 * </ul>
 */
public class FormulaScoreCalculator implements ScoreCalculator {

  private ASTExpression expression;

  public FormulaScoreCalculator(String formula) throws FormulaException {
    expression = parseExpression(formula);
  }
  
  public FormulaScoreCalculator(OptimizationContext context) throws FormulaException {
    expression = parseExpression(
            context.getConfiguration()
                .getString(KeywordOptimizerProperty.ScoreCalculatorFormula.getName()));
  }

  @Override
  public double calculate(TrafficEstimate estimate) throws FormulaException {
    if (estimate == null) {
      throw new IllegalArgumentException("The given estimate cannot be null");
    }
    if (expression == null) {
      throw new IllegalStateException("The expression cannot be null");
    }
    
    FormulaContext context = new FormulaContext(estimate);
    return expression.calculateScore(context);
  }

  /**
   * Parses a given expression as a formula and returns the abstract syntax tree (AST). The AST can
   * be used to evaluate that formula.
   */
  @VisibleForTesting
  public static ASTExpression parseExpression(String formula) throws FormulaException {
    try {
      FormulaParser parser = new FormulaParser(formula);
      return parser.Expression();
    } catch (Throwable ex) {
      throw new FormulaException("Invalid formula: " + formula, ex);
    }
  }
}
