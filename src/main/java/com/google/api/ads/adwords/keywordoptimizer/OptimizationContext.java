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

import org.apache.commons.configuration.Configuration;

/**
 * This class holds shared objects during the optimization projects. 
 */
public class OptimizationContext {
  private Configuration configuration;
  private AdWordsApiUtil adwordsApiUtil;

  public OptimizationContext(Configuration configuration, AdWordsApiUtil adwordsApiUtil) {
    this.configuration = configuration;
    this.adwordsApiUtil = adwordsApiUtil;
  }

  public AdWordsApiUtil getAdwordsApiUtil() {
    return adwordsApiUtil;
  }

  public Configuration getConfiguration() {
    return configuration;
  }
}
