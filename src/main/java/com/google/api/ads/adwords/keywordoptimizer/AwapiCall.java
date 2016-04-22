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

import com.google.api.ads.adwords.axis.v201603.cm.ApiException;

import java.rmi.RemoteException;

/**
 * A simple wrapper on any AdWords API function to feed {@link AwapiRateLimiter}
 */
public interface AwapiCall<T> {
  /**
   * @return <T> the type of return value of the AdWords API call
   */
  public T invoke() throws ApiException, RemoteException;
}
