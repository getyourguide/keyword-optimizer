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

import com.google.api.ads.adwords.axis.v201603.cm.Keyword;
import com.google.api.ads.adwords.axis.v201603.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201603.cm.Money;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

/**
 * Test cases for the {@link KeywordCollection} class that relate to scoring (sorting, extracting
 * the best keywords etc.).
 */
@RunWith(JUnit4.class)
public class KeywordCollectionScoreTest {
  private Keyword alpha;
  private Keyword beta;
  private Keyword betaBroad;
  private Keyword gamma;
  private KeywordInfo alphaInfo;
  private KeywordInfo betaInfo;
  private KeywordInfo betaBroadInfo;
  private KeywordInfo gammaInfo;
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
    alphaInfo = new KeywordInfo(alpha, null, 3d);

    beta = new Keyword();
    beta.setText("beta");
    beta.setMatchType(KeywordMatchType.EXACT);
    betaInfo = new KeywordInfo(beta, null, 1d);
    
    betaBroad = new Keyword();
    betaBroad.setText("beta");
    betaBroad.setMatchType(KeywordMatchType.BROAD);
    betaBroadInfo = new KeywordInfo(betaBroad, null, 2d);

    gamma = new Keyword();
    gamma.setText("gamma");
    gamma.setMatchType(KeywordMatchType.EXACT);
    gammaInfo = new KeywordInfo(gamma, null, 4d);

    maxCpc = new Money();
    maxCpc.setMicroAmount(1000000L); // 1 usd

    CampaignConfiguration campaignSettings = CampaignConfiguration.builder()
        .withMaxCpc(maxCpc)
        .build();
    keywords = new KeywordCollection(campaignSettings);
    keywords.add(gammaInfo);
    keywords.add(betaInfo);
    keywords.add(alphaInfo);
    keywords.add(betaBroadInfo);
  }

  /**
   * Check sorting keywords by text / match type ({@link KeywordComparator}).
   * 
   */
  @Test
  public void checkKeywordComparator() {
    List<KeywordInfo> sortedKeywords = keywords.getListSortedByKeyword();
    
    assertEquals(ImmutableList.of(alphaInfo, betaBroadInfo, betaInfo, gammaInfo), sortedKeywords);
  }

  /**
   * Check sorting keywords by score ({@link ScoreComparator}).
   * 
   */
  @Test
  public void checkScoreComparator() {
    List<KeywordInfo> sortedKeywords = keywords.getListSortedByScore();
    
    assertEquals(ImmutableList.of(gammaInfo, alphaInfo, betaBroadInfo, betaInfo), sortedKeywords);
  }
  
  /**
   * Check sorting keywords by score ({@link ScoreComparator}).
   */
  @Test
  public void checkBestX() {
    // Check returning no keywords leads to an empty list.
    assertTrue(Iterables.elementsEqual(keywords.getBest(0), ImmutableList.of()));
    
    // Check returning the best keyword works correctly.
    assertTrue(Iterables.elementsEqual(keywords.getBest(1), ImmutableList.of(gammaInfo)));

    // Check returning the best two keywords works correctly.
    assertTrue(
        Iterables.elementsEqual(
            keywords.getBest(2).getListSortedByScore(), ImmutableList.of(gammaInfo, alphaInfo)));

    // Check returning the best three keywords works correctly.
    assertTrue(
        Iterables.elementsEqual(
            keywords.getBest(3).getListSortedByScore(),
            ImmutableList.of(gammaInfo, alphaInfo, betaBroadInfo)));

    // Check returning all keywords leads to the same elements.
    assertTrue(Iterables.elementsEqual(keywords.getBest(4), keywords));

    // Check returning more than the number of contained keywords still leads to the the same list.
    assertTrue(Iterables.elementsEqual(keywords.getBest(5), keywords));
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
