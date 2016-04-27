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
 * An {@link AlternativesFinder} takes a list of keywords and derives new, similar ones from them.
 * In the keyword optimization process, this is used for the round-based optimization of the current
 * keyword "population".
 */
public interface AlternativesFinder {
  /**
   * Derive a list of new keywords from an existing collection. A {@link AlternativesFinder} is
   * typically used to derive other (hopefully good) keywords from existing good ones.
   * 
   * @param keywords sample {@link KeywordCollection}
   * @return a list of alternatives / derived {@link KeywordCollection}
   * @throws KeywordOptimizerException in case of an error while finding keyword alternatives
   */
  KeywordCollection derive(KeywordCollection keywords) throws KeywordOptimizerException;
}
