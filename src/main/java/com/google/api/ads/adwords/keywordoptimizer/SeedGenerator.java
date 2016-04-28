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
  KeywordCollection generate() throws KeywordOptimizerException;
  
  /**
   * Returns additional campaign-level settings for keyword evaluation.
   * 
   * @return additional campaign settings
   */
  CampaignConfiguration getCampaignConfiguration();
}
