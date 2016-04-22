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

import com.google.api.ads.adwords.axis.v201603.cm.Keyword;
import com.google.api.ads.adwords.axis.v201603.cm.KeywordMatchType;
import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;

import org.apache.commons.lang.SystemUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a set of keywords with information about their quality ({@link KeywordInfo}) as well
 * as related information in order to evaluate this quality information (
 * {@link CampaignConfiguration}). This collections is implemented as a map with {@link Keyword}s
 * as keys and {@link KeywordInfo}'s as values to eliminate duplicates.
 */
public class KeywordCollection implements Iterable<KeywordInfo> {
  private static final Joiner JOINER = Joiner.on(SystemUtils.FILE_SEPARATOR);
  private final CampaignConfiguration campaignConfiguration;
  private final Map<Keyword, KeywordInfo> keywords;

  /**
   * Creates a new {@link KeywordCollection} using the given settings.
   *
   * @param campaignConfiguration additional campaign-level settings for keyword evaluation
   */
  public KeywordCollection(CampaignConfiguration campaignConfiguration) {
    this.campaignConfiguration = campaignConfiguration;
    keywords = new HashMap<Keyword, KeywordInfo>();
  }

  /**
   * Adds information about a keyword to this collection.
   * 
   * @param info the keyword information to be added
   */
  public void add(KeywordInfo info) {
    keywords.put(info.getKeyword(), info);
  }
  
  /**
   * Convenience method to add several {@link KeywordInfo}s to this collection.
   * 
   * @param infos the {@link KeywordInfo}s to be added
   */
  public void addAll(Collection<KeywordInfo> infos) {
    for (KeywordInfo info : infos) {
      keywords.put(info.getKeyword(), info);
    }
  }

  /**
   * Returns the size of this collection.
   */
  public int size() {
    return keywords.size();
  }

  /**
   * Returns whether information about a given keyword exists in this collection.
   * 
   * @param keyword they {@link Keyword} to be checked
   */
  public boolean contains(Keyword keyword) {
    return keywords.containsKey(keyword);
  }

  /**
   * Returns the set of contained {@link Keyword}s.
   */
  public Set<Keyword> getKeywords() {
    return keywords.keySet();
  }

  /**
   * Returns additional campaign-level settings for keyword evaluation.
   */
  public CampaignConfiguration getCampaignConfiguration() {
    return campaignConfiguration;
  }
  
  /**
   * Returns the distinct texts of all containing {@link Keyword}s.
   */
  public Set<String> getContainingKeywordTexts() {
    Set<String> keywordTexts = new HashSet<String>();

    for (Keyword keyword : keywords.keySet()) {
      keywordTexts.add(keyword.getText());
    }

    return keywordTexts;
  }

  /**
   * Returns the distinct match types of all containing {@link Keyword}s.
   */
  public Set<KeywordMatchType> getContainingMatchTypes() {
    Set<KeywordMatchType> matchTypes = new HashSet<KeywordMatchType>();

    for (Keyword keyword : keywords.keySet()) {
      matchTypes.add(keyword.getMatchType());
    }

    return matchTypes;
  }

  /**
   * Returns a list of all contained {@link KeywordInfo}s sorted by score (best first).
   */
  public List<KeywordInfo> getListSortedByScore() {
    return Ordering.from(new ScoreComparator()).reverse().sortedCopy(keywords.values());
  }
  
  /**
   * Returns a list of all contained {@link KeywordInfo}s sorted by keywords (alphabetic order).
   */
  public List<KeywordInfo> getListSortedByKeyword() {
    return Ordering.from(new KeywordComparator()).sortedCopy(keywords.values());
  }

  /**
   * Returns the best x {@link KeywordInfo}s in this collection.
   * 
   * @param count the number of {@link KeywordInfo}s to return (=x)
   */
  public KeywordCollection getBest(int count) {
    List<KeywordInfo> bestKeywordList =
        Ordering.from(new ScoreComparator()).greatestOf(keywords.values(), count);
    KeywordCollection bestKeywords = new KeywordCollection(campaignConfiguration);
    bestKeywords.addAll(bestKeywordList);
    return bestKeywords;
  }

  /**
   * Returns the average score of the {@link KeywordInfo}s contained. Entries without a score (which
   * are not evaluated yet) are skipped in the calculation.
   * 
   * @return the average score of the {@link KeywordInfo}s contained ({@link Double#NaN} if the
   *         collection is empty or the entries are not evaluated yet)
   */
  public double getAverageScore() {
    double sum = 0;
    for (KeywordInfo keywordInfo : keywords.values()) {
      if (keywordInfo.hasScore()) {
        sum += keywordInfo.getScore();
      }
    }
    return sum / keywords.size();
  }

  @Override
  public Iterator<KeywordInfo> iterator() {
    return keywords.values().iterator();
  }
  
  @Override
  public String toString() {
    return JOINER.join(keywords.values()) + SystemUtils.LINE_SEPARATOR;
  }
}
