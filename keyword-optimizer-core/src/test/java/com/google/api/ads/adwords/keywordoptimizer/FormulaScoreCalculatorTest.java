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

package com.google.api.ads.adwords.keywordoptimizer;

import static org.junit.Assert.assertEquals;

import com.google.api.ads.adwords.keywordoptimizer.formula.ASTExpression;
import com.google.api.ads.adwords.keywordoptimizer.formula.FormulaContext;
import com.google.api.ads.adwords.keywordoptimizer.formula.FormulaScoreCalculator;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test case for the {@link SimpleSeedGenerator} class.
 */
@RunWith(JUnit4.class)
public class FormulaScoreCalculatorTest {
  
  private static FormulaContext context;
  
  @Rule
  public final ExpectedException thrown = ExpectedException.none();
  
  /**
   * Sets up the context with example variables.
   */
  @BeforeClass
  public static void setUp() {
    context = new FormulaContext(null);
    context.setValue("test", 3);
  }

  /**
   * Checks if basic formulas can be parsed correctly.
   */
  @Test
  public void testParseFormula() throws KeywordOptimizerException {
    checkParseFormula("1");
    checkParseFormula("test");
    checkParseFormula("3+8");
    checkParseFormula("3+4*2");
    checkParseFormula("3+test*2");
  }
  
  /**
   * Evaluates some basic formulas and checks whether the result is correct.
   */
  @Test
  public void testEvaluateFormula() throws KeywordOptimizerException {
    checkEvaluateFormula("1", 1);
    checkEvaluateFormula("test", 3);
    checkEvaluateFormula("3+8", 11);
    checkEvaluateFormula("3+4*2", 11);
    checkEvaluateFormula("(3+4)*2", 14);
    checkEvaluateFormula("3+test*2", 9);
    checkEvaluateFormula("(3+test*2)/2", 4.5);
  }
  
  /**
   * Tries to parse an invalid formula and checks if an exception is thrown.
   */
  @Test
  public void testInvalidFormulaInvalidToken() throws KeywordOptimizerException {
    thrown.expect(KeywordOptimizerException.class);
    thrown.expectMessage("Invalid formula: %");
    checkParseFormula("%");
  }
  
  /**
   * Tries to parse an invalid formula and checks if an exception is thrown.
   */
  @Test
  public void testInvalidFormulaInvalidAdd() throws KeywordOptimizerException {
    thrown.expect(KeywordOptimizerException.class);
    thrown.expectMessage("Invalid formula: 3+");
    checkParseFormula("3+");
  }
  
  /**
   * Tries to parse an invalid formula and checks if an exception is thrown.
   */
  @Test
  public void testInvalidFormulaInvalidBrackets() throws KeywordOptimizerException {
    thrown.expect(KeywordOptimizerException.class);
    thrown.expectMessage("Invalid formula: (3+8");
    checkParseFormula("(3+8");
  }
  
  /**
   * Tries to parse an invalid formula and checks if an exception is thrown.
   */
  @Test
  public void testInvalidFormulaEmpty() throws KeywordOptimizerException {
    thrown.expect(KeywordOptimizerException.class);
    thrown.expectMessage("Invalid formula: ");
    checkParseFormula("");
  }
  
  /**
   * Attempt to parse a given formula into an AST, convert it back to a string and compare it with
   * the original input. 
   */
  private void checkParseFormula(String formula) throws KeywordOptimizerException {
    ASTExpression expression = FormulaScoreCalculator.parseExpression(formula);
    assertEquals(formula, expression.toString());
  }
  
  /**
   * Attempt to parse a given formula, evaluate it's value based on the default context and compare
   * it to a given expected value.
   */
  private void checkEvaluateFormula(String formula, double expectedValue)
      throws KeywordOptimizerException {
    ASTExpression expression = FormulaScoreCalculator.parseExpression(formula);
    double actualValue = expression.calculateScore(context);
    assertEquals(expectedValue, actualValue, 0.01);
  }
}
