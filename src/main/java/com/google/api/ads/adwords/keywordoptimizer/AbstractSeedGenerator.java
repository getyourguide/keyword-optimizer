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

import com.google.api.ads.adwords.axis.v201607.cm.Keyword;
import com.google.api.ads.adwords.axis.v201607.cm.KeywordMatchType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Base seed generator class providing some of the general functionality used by specific seed
 * generators.
 */
public abstract class AbstractSeedGenerator implements SeedGenerator {
  private final CampaignConfiguration campaignConfiguration;
  private final ImmutableSet<KeywordMatchType> matchTypes;
  
  /**
   * Creates a new {@link AbstractSeedGenerator} using the given settings.
   *
   * @param matchTypes match types to be used for seed keyword creation (typically an implementing
   * class should create each keyword for each of the specified match types)
   * @param campaignConfiguration additional campaign-level settings for keyword evaluation
   */
  public AbstractSeedGenerator(
      Set<KeywordMatchType> matchTypes, CampaignConfiguration campaignConfiguration) {
    this.matchTypes = ImmutableSet.copyOf(matchTypes);
    this.campaignConfiguration = campaignConfiguration;
  }

  /**
   * Method for generating plain text keywords and related statistics about Google Search. This has
   * to be implemented by derived classes. The {@link AbstractSeedGenerator} will use these plain
   * texts to create actual {@link KeywordCollection} using the specified additional criteria and
   * match types. If an implementing class does not provide {@link IdeaEstimate}s, it should set
   * {@link IdeaEstimate#EMPTY_ESTIMATE} for each keyword.
   *
   * @return a {@link Map} of plain text keywords and their {@link IdeaEstimate}s
   * @throws KeywordOptimizerException in case of an error retrieving seed keywords
   */
  protected abstract ImmutableMap<String, IdeaEstimate> getKeywordsAndEstimates()
      throws KeywordOptimizerException;

  @Override
  public final KeywordCollection generate() throws KeywordOptimizerException {
    ImmutableMap<String, IdeaEstimate> keywordsAndEstimates = getKeywordsAndEstimates();

    KeywordCollection keywordCollection = new KeywordCollection(campaignConfiguration);

    for (Entry<String, IdeaEstimate> keywordEntry : keywordsAndEstimates.entrySet()) {
      for (KeywordMatchType matchType : matchTypes) {
        Keyword keyword = KeywordOptimizerUtil.createKeyword(keywordEntry.getKey(), matchType);
        keywordCollection.add(new KeywordInfo(keyword, keywordEntry.getValue(), null, null));
      }
    }

    return keywordCollection;
  }
  
  @Override
  public CampaignConfiguration getCampaignConfiguration() {
    return campaignConfiguration;
  }
}
