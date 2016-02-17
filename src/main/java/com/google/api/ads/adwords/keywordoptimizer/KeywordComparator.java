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
 * Comparator for comparing {@link KeywordInfo}s by keyword. It uses first the keyword text and then
 * the matchType for comparisons.
 */
public class KeywordComparator implements Comparator<KeywordInfo> {
  @Override
  public int compare(KeywordInfo o1, KeywordInfo o2) {
    int compareText = o1.getKeyword().getText().compareTo(o2.getKeyword().getText());
    if (compareText != 0) {
      return compareText;
    }

    return o1.getKeyword().getMatchType().getValue()
        .compareTo(o2.getKeyword().getMatchType().getValue());
  }
}
