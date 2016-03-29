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
import com.google.api.ads.adwords.axis.v201603.cm.Money;

import org.apache.commons.lang.SystemUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Represents a set of keywords with information about their quality ({@link KeywordInfo}) as well
 * as related information in order to evaluate this quality information (
 * {@link AdditionalInfoHolder}). This collections is implemented as a map with {@link Keyword}'s as
 * keys and {@link KeywordInfo}'s as values to eliminate duplicates.
 */
public class KeywordCollection extends AdditionalInfoHolder implements Iterable<KeywordInfo> {
  private final Map<Keyword, KeywordInfo> keywords;

  /**
   * Creates a new {@link KeywordCollection} using the given max cpc.
   * 
   * @param maxCpc the maximum cpc (cost per click) as a {@link Money} object
   */
  public KeywordCollection(@Nullable Money maxCpc) {
    super(maxCpc);
    keywords = new HashMap<Keyword, KeywordInfo>();
  }

  /**
   * Creates a new i{@link KeywordCollection} based on the settings of a given
   * {@link AdditionalInfoHolder}. This clones all "additional" settings (additional criteria, max
   * cpc), but not the ones defined in any subclasses.
   * 
   * @param other the prototype info holder to take the data from
   */
  public KeywordCollection(AdditionalInfoHolder other) {
    super(other);
    keywords = new HashMap<Keyword, KeywordInfo>();
  }

  /**
   * Adds keyword information about a keyword to this collection.
   * 
   * @param info the keyword information to be added
   */
  public void add(KeywordInfo info) {
    keywords.put(info.getKeyword(), info);
  }

  /**
   * Returns the size of this collection.
   * 
   * @return the number of {@link Keyword}s contained in this collection
   */
  public int size() {
    return keywords.size();
  }

  /**
   * Returns whether information about a given keyword exists in this collection.
   * 
   * @param keyword they {@link Keyword} to be checked
   * @return true if the list contains the {@link Keyword}
   */
  public boolean contains(Keyword keyword) {
    return keywords.containsKey(keyword);
  }

  /**
   * Returns the set of contained {@link Keyword}s.
   * 
   * @return the set of contained {@link Keyword}s
   */
  public Set<Keyword> getKeywords() {
    return keywords.keySet();
  }

  /**
   * Returns the distinct texts of all containing {@link Keyword}s.
   * 
   * @return the distinct texts of all containing {@link Keyword}s
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
   * 
   * @return the distinct match types of all containing {@link Keyword}s
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
   * 
   * @return a list of all contained {@link KeywordInfo}s sorted by score (best first)
   */
  public List<KeywordInfo> getListSortedByScore() {
    List<KeywordInfo> keywordInfoList = new ArrayList<KeywordInfo>(keywords.values());
    Collections.sort(keywordInfoList, new ScoreComparator());
    return Collections.unmodifiableList(keywordInfoList);
  }
  
  /**
   * Returns a list of all contained {@link KeywordInfo}s sorted by keywords (alphabetic order).
   * 
   * @return a list of all contained {@link KeywordInfo}s sorted by keywords (alphabetic order)
   */
  public List<KeywordInfo> getListSortedByKeyword() {
    List<KeywordInfo> keywordInfoList = new ArrayList<KeywordInfo>(keywords.values());
    Collections.sort(keywordInfoList, new KeywordComparator());
    return Collections.unmodifiableList(keywordInfoList);
  }

  /**
   * Returns the best x {@link KeywordInfo}s in this collection.
   * 
   * @param count the number of {@link KeywordInfo}s to return (=x)
   * @return the best x {@link KeywordInfo}s
   */
  public KeywordCollection getBest(int count) {
    List<KeywordInfo> sortedList = getListSortedByScore();
    KeywordCollection bestKeywords = new KeywordCollection(this);
    for (int i = 0; i < count && i < sortedList.size(); i++) {
      bestKeywords.add(sortedList.get(i));
    }
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
    StringBuilder out = new StringBuilder();

    for (KeywordInfo keyword : keywords.values()) {
      out.append(keyword.toString()).append(SystemUtils.LINE_SEPARATOR);
    }

    return out.toString();
  }
}
