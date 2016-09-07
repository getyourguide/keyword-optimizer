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

import com.google.api.ads.adwords.axis.v201607.cm.KeywordMatchType;
import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test case for the {@link SimpleSeedGenerator} class.
 */
@RunWith(JUnit4.class)
public class SimpleSeedGeneratorTest {
  /**
   * Check that all keywords are in there.
   */
  @Test
  public void checkCreateAllPossibilities() throws KeywordOptimizerException {
    CampaignConfiguration campaignSettings = CampaignConfiguration.builder().build();
    SimpleSeedGenerator seedGenerator =
        new SimpleSeedGenerator(
            Sets.newHashSet(
                KeywordMatchType.BROAD, KeywordMatchType.EXACT, KeywordMatchType.PHRASE),
            campaignSettings);
    seedGenerator.addKeyword("plumbing");
    seedGenerator.addKeyword("plumber");

    KeywordCollection keywords = seedGenerator.generate();
    Set<String> keywordTexts = keywords.getContainingKeywordTexts();
    Set<KeywordMatchType> matchTypes = keywords.getContainingMatchTypes();

    // Check list sizes.
    assertEquals(6, keywords.size());
    assertEquals(2, keywordTexts.size());
    assertEquals(3, matchTypes.size());
    
    // Check contents.
    assertTrue(keywordTexts.contains("plumbing"));
    assertTrue(keywordTexts.contains("plumber"));
    assertFalse(keywordTexts.contains("pipes"));
    
    assertTrue(matchTypes.contains(KeywordMatchType.BROAD));
    assertTrue(matchTypes.contains(KeywordMatchType.EXACT));
    assertTrue(matchTypes.contains(KeywordMatchType.PHRASE));
  }
}
