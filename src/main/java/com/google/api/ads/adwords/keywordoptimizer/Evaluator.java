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

/**
 * Evaluates a list of given {@link KeywordCollection} by calculating a score for it. The way the
 * score is calculated is determined by the implementation and a better score should indicate a
 * "better" {@link Keyword} according to some metric. The score is for comparison only.
 */
public interface Evaluator {
  /**
   * Bulk evaluates a list of given {@link KeywordCollection}. The result of this operation is a
   * copy of the given {@link KeywordCollection} with scores set in the containing
   * {@link KeywordInfo} objects.
   *
   * @param keywords the {@link KeywordCollection} to be evaluated (these should typically have
   *        {@link TrafficEstimate}s set)
   * @return {@link KeywordCollection} a copy of the given keyword infos, with scores set
   * @throws KeywordOptimizerException in case of an error while evaluating keywords
   */
  KeywordCollection evaluate(KeywordCollection keywords) throws KeywordOptimizerException;
}
