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

import java.util.Comparator;

/**
 * Comparator for comparing {@link KeywordInfo}s by score. It sorts keywords in increasing order of
 * their score, worst ones first.
 */
public class ScoreComparator implements Comparator<KeywordInfo> {
  @Override
  public int compare(KeywordInfo o1, KeywordInfo o2) {
    if (o1.getScore() == null && o2.getScore() == null) {
      return 0;
    }

    return Double.compare(o1.getScore(), o2.getScore());
  }
}
