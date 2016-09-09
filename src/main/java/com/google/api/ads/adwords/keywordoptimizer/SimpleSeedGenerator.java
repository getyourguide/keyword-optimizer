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

import com.google.api.ads.adwords.axis.v201607.cm.KeywordMatchType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Simple implementation of a {@link SeedGenerator} that takes keywords as plain strings and returns
 * them again.
 */
public class SimpleSeedGenerator extends AbstractSeedGenerator {
  private final Collection<String> keywords;

  /**
   * Creates a new {@link SimpleSeedGenerator} containing no keywords (these have to be added by
   * {@link #addKeyword(String)}).
   *
   * @param matchTypes match types to be used for seed keyword creation
   * @param campaignConfiguration additional campaign-level settings for keyword evaluation
   */
  public SimpleSeedGenerator(
      Set<KeywordMatchType> matchTypes, CampaignConfiguration campaignConfiguration) {
    super(matchTypes, campaignConfiguration);
    keywords = new ArrayList<String>();
  }

  /**
   * Creates a new {@link SimpleSeedGenerator} containing a list of given keywords.
   *
   * @param matchTypes match types to be used for seed keyword creation
   * @param campaignConfiguration additional campaign-level settings for keyword evaluation
   * @param keywords the keywords (plain text) to use
   */
  public SimpleSeedGenerator(
      Set<KeywordMatchType> matchTypes,
      CampaignConfiguration campaignConfiguration,
      Collection<String> keywords) {
    super(matchTypes, campaignConfiguration);
    this.keywords = new ArrayList<String>(keywords);
  }

  /**
   * Creates a new {@link SimpleSeedGenerator} containing a list of given keywords.
   *
   * @param matchTypes match types to be used for seed keyword creation
   * @param campaignConfiguration additional campaign-level settings for keyword evaluation
   * @param keywords the keywords (plain text) to use
   */
  public SimpleSeedGenerator(
      Set<KeywordMatchType> matchTypes,
      CampaignConfiguration campaignConfiguration,
      String... keywords) {
    super(matchTypes, campaignConfiguration);
    this.keywords = new ArrayList<String>(keywords.length);

    for (String keyword : keywords) {
      this.keywords.add(keyword);
    }
  }

  @Override
  protected ImmutableMap<String, IdeaEstimate> getKeywordsAndEstimates() {
    Builder<String, IdeaEstimate> keywordsAndEstimatesBuilder = ImmutableMap.builder();
    
    for (String keyword : keywords) {
      keywordsAndEstimatesBuilder.put(keyword, IdeaEstimate.EMPTY_ESTIMATE);
    }
    
    return keywordsAndEstimatesBuilder.build();
  }

  /**
   * Adds a new plain text keyword to the generator.
   *
   * @param keyword the plain text keyword to be added
   */
  public void addKeyword(String keyword) {
    keywords.add(keyword);
  }
}
