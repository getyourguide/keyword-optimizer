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

import com.google.api.ads.adwords.axis.v201603.cm.Criterion;
import com.google.api.ads.adwords.axis.v201603.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201603.cm.Money;

/**
 * A {@link SeedGenerator} creates a set of initial keywords. Each implementation defines separately
 * how this is done (e.g. for a URL or category), but has to make sure the additional information 
 * (as defined by the set and add methods here) will be included in the generated 
 * {@link KeywordCollection}.
 */
public interface SeedGenerator {
  /**
   * Generate a list of seed keywords.
   * 
   * @return the generated {@link KeywordCollection}
   * @throws KeywordOptimizerException in case of an error generating the seed keywords
   */
  public KeywordCollection generate() throws KeywordOptimizerException;

  /**
   * Adds an additional criterion in order to refine the estimates for the keywords. The generated
   * keywords will contain these additional criteria.
   * 
   * @param criterion an additional criterion
   */
  public void addAdditionalCriterion(Criterion criterion);

  /**
   * Convenience method to add a location as an additional criterion.
   * 
   * @param locationId the ID of the location (e.g. 1023191 = New York)
   */
  public void addAdditionalLocation(long locationId);

  /**
   * Convenience method to add a language as an additional criterion.
   * 
   * @param languageId the ID of the language (e.g. 1000 = English)
   */
  public void addAdditionalLanguage(long languageId);

  /**
   * Set the max cpc for retrieving accurate estimates.
   * 
   * @param maxCpc the maximum cpc (cost per click) defined as {@link Money}
   */
  public void setMaxCpc(Money maxCpc);

  /**
   * Adds a keyword match type to the generator. Typically the generator will create keywords for
   * each of the specified match types.
   * 
   * @param matchType the {@link KeywordMatchType} to be added
   */
  public void addMatchType(KeywordMatchType matchType);
}
