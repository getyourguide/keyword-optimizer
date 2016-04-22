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

import static org.hamcrest.core.Is.isA;

import com.google.api.ads.adwords.axis.v201603.cm.ApiError;
import com.google.api.ads.adwords.axis.v201603.cm.ApiException;
import com.google.api.ads.adwords.axis.v201603.cm.RateExceededError;
import com.google.api.ads.adwords.axis.v201603.cm.RateExceededErrorReason;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.rmi.RemoteException;

/**
 * Test case for the {@link AwapiRateLimiter} class.
 */
@RunWith(JUnit4.class)
public class AwapiRateLimiterTest {
  
  private static final Long TEST_CID = 1L;
  private static final Object DUMMY_OBJECT = new Object();

  private static final RateExceededError rateExceededError = new RateExceededError("path",
      "trigger", "error string", "error type", RateExceededErrorReason.RATE_EXCEEDED, "rate name",
      "DEVELOPER", 1);
  private static final ApiException rateExceededException = new ApiException("message",
      "error type", new ApiError[] { rateExceededError });

  private static final ApiException otherApiException = new ApiException("message",
      "error type", new ApiError[] {  });
  private static final RemoteException remoteException = new RemoteException("message");
  
  @Rule
  public final ExpectedException thrown = ExpectedException.none();
  
  /**
   * Test that AdWords API call succeeds
   */
  @Test
  public void testPass() throws ApiException, RemoteException {
    AwapiRateLimiter.getInstance().run(new AwapiCall<Object>() {
      @Override
      public Object invoke() {
        return DUMMY_OBJECT;
      }
    }, TEST_CID);
  }

  /**
   * Test that AdWords API call failed with RateExceededError first, but succeeds on retry
   */
  @Test
  public void testPassAfterRetry() throws ApiException, RemoteException {
    AwapiRateLimiter.getInstance().run(new AwapiCall<Object>() {
      private boolean invoked = false;
      
      @Override
      public Object invoke() throws ApiException {
        if (invoked) {
          throw rateExceededException;
        } else {
          return DUMMY_OBJECT;
        }
      }
    }, TEST_CID);
  }
  
  /**
   * Test that AdWords API call failed with RateExceededError with all retries
   */
  @Test
  public void testFailWithRateExceededError() throws ApiException, RemoteException {
    thrown.expect(RuntimeException.class);
    thrown.expectCause(isA(ApiException.class));

    AwapiRateLimiter.getInstance().run(new AwapiCall<Object>() {
      @Override
      public Object invoke() throws ApiException {
        throw rateExceededException;
      }
    }, TEST_CID);
  }
  
  /**
   * Test that AdWords API call failed with other ApiException
   */
  @Test
  public void testFailWithOtherApiException() throws ApiException, RemoteException {
    thrown.expect(ApiException.class);

    AwapiRateLimiter.getInstance().run(new AwapiCall<Object>() {
      @Override
      public Object invoke() throws ApiException {
        throw otherApiException;
      }
    }, TEST_CID);
  }

  /**
   * Test that AdWords API call failed with RemoteException
   */
  @Test
  public void testFailWithRemoteException() throws ApiException, RemoteException {
    thrown.expect(RemoteException.class);

    AwapiRateLimiter.getInstance().run(new AwapiCall<Object>() {
      @Override
      public Object invoke() throws RemoteException {
        throw remoteException;
      }
    }, TEST_CID);
  }
}
