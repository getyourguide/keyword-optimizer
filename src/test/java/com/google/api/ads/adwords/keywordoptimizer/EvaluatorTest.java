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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.api.ads.adwords.axis.v201607.cm.Keyword;
import com.google.api.ads.adwords.axis.v201607.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201607.cm.Money;
import com.google.api.ads.adwords.axis.v201607.o.StatsEstimate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test case for the {@link Evaluator}.
 */
@RunWith(JUnit4.class)
public class EvaluatorTest {
  private Keyword plumbing;
  private Keyword plumbingBroad;
  private Keyword plumbingSpecialist;
  private Money maxCpc;

  private KeywordCollection keywords;

  private StatsEstimate minStats;
  private StatsEstimate maxStats;

  private Evaluator clicksEvaluator;
  private Evaluator impressionsEvaluator;

  /**
   * Setup some sample keywords.
   */
  @Before
  public void setUp() {
    plumbing = new Keyword();
    plumbing.setText("plumbing");
    plumbing.setMatchType(KeywordMatchType.EXACT);

    plumbingBroad = new Keyword();
    plumbingBroad.setText("plumbing");
    plumbingBroad.setMatchType(KeywordMatchType.BROAD);

    plumbingSpecialist = new Keyword();
    plumbingSpecialist.setText("plumbing specialist");
    plumbingSpecialist.setMatchType(KeywordMatchType.EXACT);

    maxCpc = new Money();
    maxCpc.setMicroAmount(1000000L); // 1 usd
    
    CampaignConfiguration campaignSettings = CampaignConfiguration.builder()
        .withMaxCpc(maxCpc)
        .build();
    keywords = new KeywordCollection(campaignSettings);
    keywords.add(new KeywordInfo(plumbing, IdeaEstimate.EMPTY_ESTIMATE, null, null));
    keywords.add(new KeywordInfo(plumbingBroad, IdeaEstimate.EMPTY_ESTIMATE, null, null));
    keywords.add(new KeywordInfo(plumbingSpecialist, IdeaEstimate.EMPTY_ESTIMATE, null, null));

    minStats = new StatsEstimate();
    minStats.setClicksPerDay(10F);
    minStats.setImpressionsPerDay(1000F);

    maxStats = new StatsEstimate();
    maxStats.setClicksPerDay(20F);
    maxStats.setImpressionsPerDay(2000F);

    clicksEvaluator =
        new EstimatorBasedEvaluator(new MockTrafficEstimator(), new ClicksScoreCalculator());

    impressionsEvaluator =
        new EstimatorBasedEvaluator(new MockTrafficEstimator(), new ImpressionsScoreCalculator());
  }

  /**
   * Create evaluates & check all keywords are in there (clicks evaluator).
   */
  @Test
  public void checkContainsKeywordsClicks() throws KeywordOptimizerException {
    KeywordCollection evaluations = clicksEvaluator.evaluate(keywords);

    assertTrue(evaluations.contains(plumbing));
    assertTrue(evaluations.contains(plumbingBroad));
    assertTrue(evaluations.contains(plumbingSpecialist));

    assertEquals(3, evaluations.size());
  }
  
  /**
   * Create estimates + scores & check that all were actually set.
   */
  @Test
  public void checkHasEstimatesAndScores() throws KeywordOptimizerException {
    KeywordCollection evaluations = clicksEvaluator.evaluate(keywords);

    for (KeywordInfo keyword : evaluations) {
      assertTrue(keyword.hasEstimate());
      assertTrue(keyword.hasScore());
    }
  }

  /**
   * Create evaluates & check all keywords are in there (impressions evaluator).
   */
  @Test
  public void checkContainsKeywordsImpressions() throws KeywordOptimizerException {
    KeywordCollection evaluations = impressionsEvaluator.evaluate(keywords);

    assertTrue(evaluations.contains(plumbing));
    assertTrue(evaluations.contains(plumbingBroad));
    assertTrue(evaluations.contains(plumbingSpecialist));

    assertEquals(3, evaluations.size());
  }

  /**
   * Checks that the scores are correct (clicks evaluator).
   */
  @Test
  public void checkScoreClicks() throws KeywordOptimizerException {
    KeywordCollection evaluations = clicksEvaluator.evaluate(keywords);

    for (KeywordInfo evaluation : evaluations) {
      assertEquals(15D, evaluation.getScore(), 0);
    }

    assertEquals(15D, evaluations.getAverageScore(), 0);
  }

  /**
   * Checks that the scores are correct (impressions evaluator).
   */
  @Test
  public void checkScoreImpressions() throws KeywordOptimizerException {
    KeywordCollection evaluations = impressionsEvaluator.evaluate(keywords);

    for (KeywordInfo evaluation : evaluations) {
      assertEquals(1500D, evaluation.getScore(), 0);
    }

    assertEquals(1500D, evaluations.getAverageScore(), 0);
  }

  /**
   * A mock traffic estimator, always returning the previously setup stats.
   */
  private class MockTrafficEstimator implements TrafficEstimator {
    @Override
    public KeywordCollection estimate(KeywordCollection keywords) {
      KeywordCollection estimates = new KeywordCollection(keywords.getCampaignConfiguration());

      for (KeywordInfo keyword : keywords) {
        TrafficEstimate te = new TrafficEstimate(minStats, maxStats);
        estimates.add(
            new KeywordInfo(keyword.getKeyword(), IdeaEstimate.EMPTY_ESTIMATE, te, null));
      }

      return estimates;
    }
  }
}
