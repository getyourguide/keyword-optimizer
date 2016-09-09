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
import static org.junit.Assert.assertTrue;

import com.google.api.ads.adwords.axis.v201607.cm.ApiError;
import com.google.api.ads.adwords.axis.v201607.cm.ApiException;
import com.google.api.ads.adwords.axis.v201607.cm.RateExceededError;
import com.google.api.ads.adwords.axis.v201607.cm.RateExceededErrorReason;
import java.rmi.RemoteException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test case for the {@link AwapiRateLimiter} class.
 */
@RunWith(JUnit4.class)
public class AwapiRateLimiterTest {
  
  private static final Long TEST_CID = 1L;
  private static final Object DUMMY_OBJECT = new Object();
  
  private static final long SECOND_TO_MILLIS = 1000L;
  private static final int WAIT_AFTER_SECONDS = 1;
  private static final int LONG_WAIT_AFTER_SECONDS = 6;

  private static final RateExceededError rateExceededError = new RateExceededError("path",
      "trigger", "error string", "error type", RateExceededErrorReason.RATE_EXCEEDED, "rate name",
      "DEVELOPER", WAIT_AFTER_SECONDS);
  private static final ApiException rateExceededException = new ApiException("message",
      "error type", new ApiError[] { rateExceededError });
  
  // RateExceededError with long wait time
  private static final RateExceededError rateExceededErrorLong = new RateExceededError("path",
      "trigger", "error string", "error type", RateExceededErrorReason.RATE_EXCEEDED, "rate name",
      "DEVELOPER", LONG_WAIT_AFTER_SECONDS);
  private static final ApiException rateExceededExceptionLong = new ApiException("message",
      "error type", new ApiError[] { rateExceededErrorLong });
  
  private static final ApiException otherApiException = new ApiException("message",
      "error type", new ApiError[] {  });
  private static final RemoteException remoteException = new RemoteException("message");
  
  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  // Overwrite default configuration.
  private static final int MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR = 3;
  private static final int TIMEOUT_ON_RATE_EXCEEDED_ERROR = 5;
  
  static {
    System.setProperty(
        AwapiRateLimiter.MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR_PROPERTY,
        String.valueOf(MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR));
    System.setProperty(
        AwapiRateLimiter.TIMEOUT_ON_RATE_EXCEEDED_ERROR_PROPERTY,
        String.valueOf(TIMEOUT_ON_RATE_EXCEEDED_ERROR));
  }
  
  /**
   * Test that AdWords API call succeeds
   */
  @Test
  public void testPass() throws ApiException, RemoteException {
    AwapiRateLimiter.getInstance(AwapiRateLimiter.RateLimitBucket.OTHERS)
        .run(new AwapiCall<Object>() {
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
  public void testPassAfterOneRetry() throws ApiException, RemoteException {
    long startTime = System.currentTimeMillis();
    
    AwapiRateLimiter.getInstance(AwapiRateLimiter.RateLimitBucket.OTHERS)
        .run(new AwapiCall<Object>() {
          private boolean invoked = false;
          @Override
          public Object invoke() throws ApiException {
            if (!invoked) {
              invoked = true;
              throw rateExceededException;
            } else {
              return DUMMY_OBJECT;
            }
          }
        }, TEST_CID);
    
    long endTime = System.currentTimeMillis();
    long seconds = (endTime - startTime) / SECOND_TO_MILLIS;
    assertTrue(seconds >= WAIT_AFTER_SECONDS);
  }
  
  /**
   * Test that AdWords API call failed with RateExceededError with all retries
   */
  @Test
  public void testFailWithRateExceededError() throws ApiException, RemoteException {
    thrown.expect(RuntimeException.class);
    thrown.expectCause(isA(ApiException.class));
    long startTime = System.currentTimeMillis();

    AwapiRateLimiter.getInstance(AwapiRateLimiter.RateLimitBucket.OTHERS)
        .run(new AwapiCall<Object>() {
          @Override
          public Object invoke() throws ApiException {
            throw rateExceededException;
          }
        }, TEST_CID);
    
    long endTime = System.currentTimeMillis();
    long seconds = (endTime - startTime) / SECOND_TO_MILLIS;
    assertTrue(seconds > WAIT_AFTER_SECONDS * MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR);
  }
  
  /**
   * Test that AdWords API call failed with RateExceededError with all retries
   *
   * @throws InterruptedException
   */
  @Test
  public void testTimeoutWithRateExceededError()
      throws ApiException, RemoteException, InterruptedException {
    thrown.expect(RuntimeException.class);
    thrown.expectCause(isA(ApiException.class));
    
    long startTime = System.currentTimeMillis();
    try {
      AwapiRateLimiter.getInstance(AwapiRateLimiter.RateLimitBucket.OTHERS)
          .run(new AwapiCall<Object>() {
            @Override
            public Object invoke() throws ApiException {
              throw rateExceededExceptionLong;
            }
          }, TEST_CID);
    } catch (RuntimeException e) {
      long endTime = System.currentTimeMillis();
      long seconds = (endTime - startTime) / SECOND_TO_MILLIS;
      assertTrue(seconds < LONG_WAIT_AFTER_SECONDS);  // Because it exits immediately.
      
      // Wait enough time so it doesn't affect other test cases.
      Thread.sleep(LONG_WAIT_AFTER_SECONDS * SECOND_TO_MILLIS);
      throw e;
    }
  }
  
  /**
   * Test that AdWords API call failed with other ApiException
   */
  @Test
  public void testFailWithOtherApiException() throws ApiException, RemoteException {
    thrown.expect(ApiException.class);

    long startTime = System.currentTimeMillis();
    AwapiRateLimiter.getInstance(AwapiRateLimiter.RateLimitBucket.OTHERS)
        .run(new AwapiCall<Object>() {
          @Override
          public Object invoke() throws ApiException {
            throw otherApiException;
          }
        }, TEST_CID);
    
    long endTime = System.currentTimeMillis();
    long seconds = (endTime - startTime) / SECOND_TO_MILLIS;
    assertTrue(seconds < LONG_WAIT_AFTER_SECONDS);  // Because it exits immediately.
  }

  /**
   * Test that AdWords API call failed with RemoteException
   */
  @Test
  public void testFailWithRemoteException() throws ApiException, RemoteException {
    thrown.expect(RemoteException.class);

    long startTime = System.currentTimeMillis();
    AwapiRateLimiter.getInstance(AwapiRateLimiter.RateLimitBucket.OTHERS)
        .run(new AwapiCall<Object>() {
          @Override
          public Object invoke() throws RemoteException {
            throw remoteException;
          }
        }, TEST_CID);
    
    long endTime = System.currentTimeMillis();
    long seconds = (endTime - startTime) / SECOND_TO_MILLIS;
    assertTrue(seconds < LONG_WAIT_AFTER_SECONDS);  // Because it exits immediately.
  }
}
