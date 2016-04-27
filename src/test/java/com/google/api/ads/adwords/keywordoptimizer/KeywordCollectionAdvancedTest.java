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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.api.ads.adwords.axis.v201603.cm.Keyword;
import com.google.api.ads.adwords.axis.v201603.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201603.cm.Money;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

/**
 * Advanced test cases for the {@link KeywordCollection} class (estimates, sorting, ...).
 */
@RunWith(JUnit4.class)
public class KeywordCollectionAdvancedTest {
  private Keyword alpha;
  private Keyword beta;
  private Keyword betaBroad;
  private Keyword gamma;
  private Money maxCpc;

  private KeywordCollection keywords;

  /**
   * Setup some sample keywords.
   */
  @Before
  public void setUp() {
    alpha = new Keyword();
    alpha.setText("alpha");
    alpha.setMatchType(KeywordMatchType.EXACT);

    beta = new Keyword();
    beta.setText("beta");
    beta.setMatchType(KeywordMatchType.BROAD);
    
    betaBroad = new Keyword();
    betaBroad.setText("beta");
    betaBroad.setMatchType(KeywordMatchType.EXACT);

    gamma = new Keyword();
    gamma.setText("gamma");
    gamma.setMatchType(KeywordMatchType.EXACT);

    maxCpc = new Money();
    maxCpc.setMicroAmount(1000000L); // 1 usd

    CampaignConfiguration campaignSettings = CampaignConfiguration.builder()
        .withMaxCpc(maxCpc)
        .build();
    keywords = new KeywordCollection(campaignSettings);
    keywords.add(new KeywordInfo(gamma, null, 4d));
    keywords.add(new KeywordInfo(beta, null, 1d));
    keywords.add(new KeywordInfo(alpha, null, 3d));
    keywords.add(new KeywordInfo(betaBroad, null, 2d));
  }

  /**
   * Check sorting keywords by text / match type ({@link KeywordComparator}).
   * 
   */
  @Test
  public void checkKeywortComparator() {
    List<KeywordInfo> sortedKeywords = keywords.getListSortedByKeyword();
    
    assertEquals(alpha, sortedKeywords.get(0).getKeyword());
    assertEquals(beta, sortedKeywords.get(1).getKeyword());
    assertEquals(betaBroad, sortedKeywords.get(2).getKeyword());
    assertEquals(gamma, sortedKeywords.get(3).getKeyword());
  }

  /**
   * Check sorting keywords by score ({@link ScoreComparator}).
   * 
   */
  @Test
  public void checkScoreComparator() {
    List<KeywordInfo> sortedKeywords = keywords.getListSortedByScore();
    
    assertEquals(beta, sortedKeywords.get(0).getKeyword());
    assertEquals(betaBroad, sortedKeywords.get(1).getKeyword());
    assertEquals(alpha, sortedKeywords.get(2).getKeyword());
    assertEquals(gamma, sortedKeywords.get(3).getKeyword());
  }
  
  /**
   * Check sorting keywords by score ({@link ScoreComparator}).
   * 
   */
  @Test
  public void checkBestX() {
    KeywordCollection best = keywords.getBest(2);
    
    // The list contains only 2 as selected.
    assertEquals(2, best.size());
    
    // The list contains only score >= 3.
    for (KeywordInfo keyword : best) {
      assertTrue(keyword.getScore() >= 3);
    }
    
    // The list contains exactly the best 2 keywords.
    assertTrue(best.contains(alpha));
    assertTrue(best.contains(gamma));
    
    // The list does not contain exactly the other 2 keywords.
    assertFalse(best.contains(beta));
    assertFalse(best.contains(betaBroad));
  }
  
  /**
   * Check that the average score calculation works.
   * 
   */
  @Test
  public void checkAverage() {
    assertEquals(2.5D, keywords.getAverageScore(), 0.001);
  }
}
