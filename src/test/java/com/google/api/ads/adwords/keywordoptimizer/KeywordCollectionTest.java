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
import com.google.api.ads.adwords.axis.v201607.cm.Language;
import com.google.api.ads.adwords.axis.v201607.cm.Location;
import com.google.api.ads.adwords.axis.v201607.cm.Money;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test cases for the {@link KeywordCollection} class regarding keyword handling (keyword texts,
 * match types, duplicates).
 */
@RunWith(JUnit4.class)
public class KeywordCollectionTest {
  private Keyword plumbing;
  private Keyword plumbingBroad;
  private Keyword plumbingSpecialist;
  private Location newYork;
  private Language english;
  private Money maxCpc;

  private KeywordCollection keywords;

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

    newYork = new Location();
    newYork.setId(1023191L);

    english = new Language();
    english.setId(1000L);

    maxCpc = new Money();
    maxCpc.setMicroAmount(1000000L); // 1 usd

    CampaignConfiguration campaignSettings = CampaignConfiguration.builder()
        .withMaxCpc(maxCpc)
        .withCriterion(english)
        .withCriterion(newYork)
        .build();
    keywords = new KeywordCollection(campaignSettings);
    
    keywords = new KeywordCollection(campaignSettings);
    keywords.add(new KeywordInfo(plumbing, null, null));
    keywords.add(new KeywordInfo(plumbingBroad, null, null));
    keywords.add(new KeywordInfo(plumbingSpecialist, null, null));
  }

  /**
   * Check all keywords are in there.
   */
  @Test
  public void checkContainsKeywords() {
    assertTrue(keywords.contains(plumbing));
    assertTrue(keywords.contains(plumbingSpecialist));

    assertEquals(3, keywords.size());
  }

  /**
   * Check all keyword texts are in there.
   */
  @Test
  public void checkKeywordTexts() {
    Collection<String> keywordTexts = keywords.getContainingKeywordTexts();

    assertTrue(keywordTexts.contains(plumbing.getText()));
    assertTrue(keywordTexts.contains(plumbingBroad.getText()));
    assertTrue(keywordTexts.contains(plumbingSpecialist.getText()));

    // Texts for plumbing and plumbingBroad are the same.
    assertEquals(2, keywordTexts.size());
  }

  /**
   * Check all additional criteria are in there.
   */
  @Test
  public void checkContainsAdditionalCriterions() {
    assertTrue(keywords.getCampaignConfiguration().getAdditionalCriteria().contains(newYork));
    assertTrue(keywords.getCampaignConfiguration().getAdditionalCriteria().contains(english));
    assertEquals(2, keywords.getCampaignConfiguration().getAdditionalCriteria().size());
  }

  /**
   * Check all keyword texts are in there.
   */
  @Test
  public void checkDuplicateKeywords() {
    assertEquals(3, keywords.size());

    // Add the same keyword as is.
    keywords.add(new KeywordInfo(plumbing, null, null));
    assertEquals(3, keywords.size());

    // Add the same keyword (different object).
    Keyword plumbing2 = new Keyword();
    plumbing2.setText("plumbing");
    plumbing2.setMatchType(KeywordMatchType.EXACT);
    keywords.add(new KeywordInfo(plumbing2, null, null));

    assertEquals(3, keywords.size());
  }
}
