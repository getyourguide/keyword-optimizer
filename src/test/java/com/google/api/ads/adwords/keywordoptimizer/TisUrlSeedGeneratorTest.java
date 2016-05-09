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

import com.google.api.ads.adwords.axis.v201603.cm.ApiException;
import com.google.api.ads.adwords.axis.v201603.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201603.o.Attribute;
import com.google.api.ads.adwords.axis.v201603.o.AttributeType;
import com.google.api.ads.adwords.axis.v201603.o.StringAttribute;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdea;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdeaPage;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdeaSelector;
import com.google.api.ads.adwords.axis.v201603.o.TargetingIdeaServiceInterface;
import com.google.api.ads.adwords.axis.v201603.o.Type_AttributeMapEntry;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.rmi.RemoteException;
import java.util.Set;

/**
 * Test case for the {@link TisUrlSeedGenerator} class.
 */
@RunWith(JUnit4.class)
public class TisUrlSeedGeneratorTest {

  private static final String PLUMBING = "plumbing";
  private static final String PLUMBING_PROFESSIONAL = "plumbing professional";
  private static final String PIPES = "pipes";

  private Set<KeywordMatchType> matchTypes;
  private TisUrlSeedGenerator seedGenerator;

  /**
   * Setup a sample parameters and mock seed generator.
   */
  @Before
  public void setUp() {
    matchTypes = Sets.newHashSet(KeywordMatchType.EXACT, KeywordMatchType.PHRASE);
    CampaignConfiguration campaignConfiguration = CampaignConfiguration.builder().build();

    seedGenerator =
        new TisUrlSeedGenerator(
            new MockTargetingIdeaService(), 12345L, matchTypes, campaignConfiguration);
  }

  /**
   * Check that all keywords texts and match types returned by the mock targeting idea service are
   * in the generated seed.
   */
  @Test
  public void checkCreateSeedKeywords() throws KeywordOptimizerException {
    KeywordCollection seedKeywords = seedGenerator.generate();
    Set<String> seedKeywordTexts = seedKeywords.getContainingKeywordTexts();
    Set<KeywordMatchType> seedMatchTypes = seedKeywords.getContainingMatchTypes();

    // Check list sizes.
    assertEquals(6, seedKeywords.size());

    // Check contents.
    assertEquals(ImmutableSet.of(PLUMBING, PLUMBING_PROFESSIONAL, PIPES), seedKeywordTexts);
    assertEquals(matchTypes, seedMatchTypes);
  }

  /**
   * A mock implementation for the targeting idea service that always returns a fixed set of keyword
   * ideas.
   */
  private static class MockTargetingIdeaService implements TargetingIdeaServiceInterface {

    @Override
    public TargetingIdeaPage get(TargetingIdeaSelector selector)
        throws RemoteException, ApiException {
      TargetingIdeaPage page = new TargetingIdeaPage();

      TargetingIdea[] entries =
          new TargetingIdea[] {
            createTargetingIdea(PLUMBING),
            createTargetingIdea(PLUMBING_PROFESSIONAL),
            createTargetingIdea(PIPES)
          };
      page.setEntries(entries);
      page.setTotalNumEntries(entries.length);

      return page;
    }

    /**
     * Convenience method for creating a new {@link TargetingIdea} containing a given keyword text.
     *
     * @param keyword the keyword text to use as an {@link Attribute} in the {@link TargetingIdea}
     * @return a targeting idea containing the keyword text only
     */
    private static TargetingIdea createTargetingIdea(String keyword) {
      StringAttribute keywordAttribute = new StringAttribute();
      keywordAttribute.setValue(keyword);
      Type_AttributeMapEntry data =
          new Type_AttributeMapEntry(AttributeType.KEYWORD_TEXT, keywordAttribute);
      TargetingIdea idea = new TargetingIdea();
      idea.setData(new Type_AttributeMapEntry[] {data});
      return idea;
    }
  }
}
