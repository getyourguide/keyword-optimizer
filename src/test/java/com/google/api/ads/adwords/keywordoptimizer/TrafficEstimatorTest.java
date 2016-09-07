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
 * Test case for the {@link TrafficEstimator} and {@link TrafficEstimate} classes.
 */
@RunWith(JUnit4.class)
public class TrafficEstimatorTest {
  private Keyword plumbing;
  private Keyword plumbingBroad;
  private Keyword plumbingSpecialist;
  private Money maxCpc;

  private KeywordCollection keywords;

  private StatsEstimate minStats;
  private StatsEstimate maxStats;

  private TrafficEstimator trafficEstimator = new MockTrafficEstimator();

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
    keywords.add(new KeywordInfo(plumbing, null, null));
    keywords.add(new KeywordInfo(plumbingBroad, null, null));
    keywords.add(new KeywordInfo(plumbingSpecialist, null, null));

    minStats = new StatsEstimate();
    minStats.setClicksPerDay(10F);
    minStats.setImpressionsPerDay(1000F);

    maxStats = new StatsEstimate();
    maxStats.setClicksPerDay(20F);
    maxStats.setImpressionsPerDay(2000F);
  }

  /**
   * Create estimates & check all keywords are in there.
   */
  @Test
  public void checkContainsKeywords() throws KeywordOptimizerException {
    KeywordCollection estimates = trafficEstimator.estimate(keywords);

    assertTrue(estimates.contains(plumbing));
    assertTrue(estimates.contains(plumbingBroad));
    assertTrue(estimates.contains(plumbingSpecialist));

    assertEquals(3, estimates.size());
  }

  /**
   * Create estimates & check that all were actually set.
   */
  @Test
  public void checkHasEstimates() throws KeywordOptimizerException {
    KeywordCollection estimates = trafficEstimator.estimate(keywords);

    for (KeywordInfo keyword : estimates) {
      assertTrue(keyword.hasEstimate());
    }
  }

  /**
   * Create estimates & check values (same as defined by mock estimator).
   */
  @Test
  public void checkMinMaxValues() throws KeywordOptimizerException {
    KeywordCollection estimates = trafficEstimator.estimate(keywords);

    for (KeywordInfo keyword : estimates) {
      assertEquals(10D, keyword.getEstimate().getMin().getClicksPerDay().doubleValue(), 0);
      assertEquals(1000D, keyword.getEstimate().getMin().getImpressionsPerDay().doubleValue(), 0);

      assertEquals(20D, keyword.getEstimate().getMax().getClicksPerDay().doubleValue(), 0);
      assertEquals(2000D, keyword.getEstimate().getMax().getImpressionsPerDay().doubleValue(), 0);
    }
  }

  /**
   * Create estimates & check values (average as defined by mock estimator).
   */
  @Test
  public void checkMeanValues() throws KeywordOptimizerException {
    KeywordCollection estimates = trafficEstimator.estimate(keywords);

    for (KeywordInfo keyword : estimates) {
      assertEquals(15D, keyword.getEstimate().getMean().getClicksPerDay().doubleValue(), 0);
      assertEquals(1500D, keyword.getEstimate().getMean().getImpressionsPerDay().doubleValue(), 0);
    }
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
        estimates.add(new KeywordInfo(keyword.getKeyword(), te, null));
      }

      return estimates;
    }
  }
}
