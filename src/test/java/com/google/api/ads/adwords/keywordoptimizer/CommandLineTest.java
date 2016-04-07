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

import static org.hamcrest.core.Is.isA;

import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test case for the main {@link KeywordOptimizer} class. This basically checks if command line
 * parameters are right.
 */
@RunWith(JUnit4.class)
public class CommandLineTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  /**
   * Checks if specifying no arg works.
   */
  @Test
  public void checkNoArg() throws KeywordOptimizerException {
    thrown.expect(KeywordOptimizerException.class);
    thrown.expectCause(isA(MissingOptionException.class));
    KeywordOptimizer.main(new String[]{});
  }

  /**
   * Checks if the specifying some random argument work throws an exception.
   */
  @Test
  public void checkRandomArgDoesNotWork() throws KeywordOptimizerException {
    thrown.expect(KeywordOptimizerException.class);
    thrown.expectCause(isA(UnrecognizedOptionException.class));
    KeywordOptimizer.run("-x");
  }

  /**
   * Checks if the leaving out some arguments work throws an exception (missing -cpc).
   */
  @Test
  public void checkMissingArgumentsDoesNotWork() throws KeywordOptimizerException {
    thrown.expect(KeywordOptimizerException.class);
    KeywordOptimizer.run("-sk plumber plumbing \"plumbing specialist\" -m EXACT PHRASE");
  }

  /**
   * Checks if specifying multiple seed arguments throws an exception.
   */
  @Test
  public void checkMultipleSeedArguments() throws KeywordOptimizerException {
    thrown.expect(KeywordOptimizerException.class);
    KeywordOptimizer.run("-sk plumber plumbing -su www.google.com");
  }

  /**
   * Checks if specifying no seed arguments throws an exception.
   */
  @Test
  public void checkNoSeedArguments() throws KeywordOptimizerException {
    thrown.expect(KeywordOptimizerException.class);
    KeywordOptimizer.run("-cpc 1.0 -m EXACT");
  }
  
  /**
   * Checks if specifying a wrong combination of output parameters (CSV output without file)
   * throws an exception.
   */
  @Test
  public void checkWrongOutputArguments() throws KeywordOptimizerException {
    thrown.expect(KeywordOptimizerException.class);
    thrown.expectMessage("An output file must be specified if output mode is CSV");
    KeywordOptimizer.run(
        "-kp keyword-optimizer.properties -ap ads.properties -cpc 1.0 -m EXACT -sk plumber -o CSV");
  }
  
  /**
   * Checks if specifying a wrong output mode throws an exception.
   */
  @Test
  public void checkWrongOutputMode() throws KeywordOptimizerException {
    thrown.expect(KeywordOptimizerException.class);
    thrown.expectMessage("Output mode 'ABC' is not supported");
    KeywordOptimizer.run(
        "-kp keyword-optimizer.properties -ap ads.properties -cpc 1.0 -m EXACT -sk plumber -o ABC");
  }
}
