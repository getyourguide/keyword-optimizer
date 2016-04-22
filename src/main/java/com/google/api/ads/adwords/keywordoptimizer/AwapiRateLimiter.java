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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.api.ads.adwords.axis.v201603.cm.ApiError;
import com.google.api.ads.adwords.axis.v201603.cm.ApiException;
import com.google.api.ads.adwords.axis.v201603.cm.RateExceededError;
import com.google.common.util.concurrent.AtomicLongMap;
import com.google.common.util.concurrent.Uninterruptibles;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;

/**
 * A rate limiter implementation that handles RateExceededError for AdWords API applications. It is
 * a singleton, with thread-safe lazy initialization in order to support custom configurations
 * (e.g., number of attempts, timeout seconds). To change the default configuration, set the system
 * properties {@link #MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR_PROPERTY} and
 * {@link #TIMEOUT_ON_RATE_EXCEEDED_ERROR_PROPERTY} <em>before</em> calling {@link #getInstance()}
 * for the first time.
 *
 * <p>
 * The features include:
 * <ul>
 * <li>Thread safe: it supports the application to invoke AdWords API from multiple threads
 * <li>Comprehends info in RateExceededError, especially rateScope and retryAfterSeconds
 * <li>Handles retry logic for the thread encountering RateExceededError automatically
 * <li>Differentiates AdWords API calls v.s. other code logic
 * <li>Easy to configure, and easy to plug into custom code
 * </ul>
 */
public final class AwapiRateLimiter {
  private static final Logger logger = LoggerFactory.getLogger(AwapiRateLimiter.class);
  private static final long SECOND_TO_MILLIS = 1000L;

  /**
   * Name of the property that specifies the maximum number of request attempts for a
   * {@code RateExceededError}.
   */
  public static final String MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR_PROPERTY =
      "com.google.api.ads.adwords.keywordoptimizer.AwapiRateLimiter.maxAttemptsOnRateExceededError";
  public static final int MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR_DEFAULT = 5;
  
  /**
   * Name of the property that specifies the maximum timeout (in seconds) to wait before retrying
   * after a {@code RateExceededError}.
   */
  public static final String TIMEOUT_ON_RATE_EXCEEDED_ERROR_PROPERTY = 
      "com.google.api.ads.adwords.keywordoptimizer.AwapiRateLimiter.timeoutOnRateExceededError";
  public static final int TIMEOUT_ON_RATE_EXCEEDED_ERROR_DEFAULT = 86400; 
  
  // Number of attempts on RateExceededError, 0 means infinite attempts
  private final int maxAttemptsOnRateExceededError;
  // Number of seconds for timeout the waiting, 0 means no timeout
  private final int timeoutOnRateExceededError;

  // Wait until time (in millis of DateTime) for token scope.
  private final AtomicLong tokenWaitUntil = new AtomicLong();
  // Wait until time (in millis of DateTime) for account scope.
  private final AtomicLongMap<Long> accountWaitUntil = AtomicLongMap.create();

  private AwapiRateLimiter(int maxAttemptsOnRateExceededError, int timeoutOnRateExceededError) {
    this.maxAttemptsOnRateExceededError = maxAttemptsOnRateExceededError;
    this.timeoutOnRateExceededError = timeoutOnRateExceededError;
  }

  /**
   * Get the only instance.
   */
  public static AwapiRateLimiter getInstance() {
    return RateLimiterSingletonHolder.rateLimiterSingleton;
  }

  /**
   * Update the wait time for TOKEN scope.
   * @param waitSeconds the wait time in millis
   */
  private void updateTokenWaitTime(int waitSeconds) {
    final long newTime = DateTime.now().getMillis() + waitSeconds * SECOND_TO_MILLIS;
    
    boolean done = true;
    do {
      long oldTime = tokenWaitUntil.get();
      // If the new wait until time exceeds current one, update it; otherwise just skip the loop.
      if (oldTime < newTime) {
        done = tokenWaitUntil.compareAndSet(oldTime, newTime);
      } else {
        done = true;
      }
    } while (!done);
  }

  /**
   * Update the wait time for ACCOUNT scope.
   * @param clientCustomerId the client customer ID
   * @param waitSeconds the wait time in millis
   */
  private void updateAccountWaitTime(Long clientCustomerId, int waitSeconds) {
    final long newTime = DateTime.now().getMillis() + waitSeconds * SECOND_TO_MILLIS;
    
    boolean done = true;
    do {
      long oldTime = accountWaitUntil.get(clientCustomerId);
      // If the new wait until time exceeds current one, update it; otherwise just skip the loop.
      if (oldTime < newTime) {
        done = (oldTime == accountWaitUntil.getAndAdd(clientCustomerId, newTime - oldTime));
      } else {
        done = true;
      }
    } while (!done);
  }

  /**
   * Wait and update the wait until time accordingly.
   * @param clientCustomerId the client customer ID for the AdWords API invocation
   * @param lastApiException the last {@link ApiException} for the request
   */
  private void wait(Long clientCustomerId, @Nullable ApiException lastApiException) {
    long nowInMillis = DateTime.now().getMillis();

    long waitForMillis = 0L;
    waitForMillis = Math.max(waitForMillis, tokenWaitUntil.get() - nowInMillis);
    
    // clientCustomerId could be null, e.g., for ReportDefinitionService invocation
    if (clientCustomerId != null) {
      waitForMillis = Math.max(waitForMillis, accountWaitUntil.get(clientCustomerId) - nowInMillis);
    }

    if (waitForMillis > 0) {
      if (timeoutOnRateExceededError > 0
          && waitForMillis > timeoutOnRateExceededError * SECOND_TO_MILLIS) {
        throw new RuntimeException(
            "Need to wait too much time (more than " + timeoutOnRateExceededError + " seconds).",
            lastApiException);
      }

      logger.info("Thread \"{}\" sleeping for {} millis due to rate limit.",
          Thread.currentThread().getName(), waitForMillis);
      Uninterruptibles.sleepUninterruptibly(waitForMillis, MILLISECONDS);
    }
  }

  /**
   * The core method for invoking AdWords API call and handling RateExceededError.
   * @param call the AdWords API call
   * @param clientCustomerId the client customer ID for invoking the call
   * @return <T> the type of return value of the AdWords API call
   */
  public <T> T run(AwapiCall<T> call, Long clientCustomerId) throws ApiException, RemoteException {
    T result = null;
    int count = 0;
    ApiException lastApiException = null;
    
    while (maxAttemptsOnRateExceededError == 0 || count++ < maxAttemptsOnRateExceededError) {
      wait(clientCustomerId, lastApiException);

      try {
        result = call.invoke();
        break;
      } catch (ApiException e) {
        boolean hasRateExceededError = false;
        lastApiException = e;

        for (ApiError error : e.getErrors()) {
          if (error instanceof RateExceededError) {
            hasRateExceededError = true;
            RateExceededError rateExceeded = (RateExceededError) error;
            String scope = rateExceeded.getRateScope();
            logger.info("Encountered RateExceededError: scope={}, seconds={}.", scope,
                rateExceeded.getRetryAfterSeconds());

            // Add an extra second for prudence.
            // TODO(zhuoc): Can getRetryAfterSeconds() return null?
            int seconds = rateExceeded.getRetryAfterSeconds().intValue() + 1;

            if ("DEVELOPER".equals(scope)) {
              updateTokenWaitTime(seconds);
            } else if ("ACCOUNT".equals(scope)) {
              updateAccountWaitTime(clientCustomerId, seconds);
            } else {
              throw new RuntimeException("Unknown RateExceededError scope: " + scope, e);
            }
          }
        }

        // Rethrow exception if it does not contain RateExceededError.
        if (!hasRateExceededError) {
          throw e;
        }
      }
    }

    if (result == null) {
      throw new RuntimeException("Failed to get a valid result with AdWords API invocation after "
          + maxAttemptsOnRateExceededError + " attempts on RateExceededError.", lastApiException);
    }

    return result;
  }
  
  /**
   * Thread-safe helper that holds a singleton {@link AwapiRateLimiter}. This follows the lazy
   * initialization holder class idiom.
   */
  private static final class RateLimiterSingletonHolder {
    private static final AwapiRateLimiter rateLimiterSingleton =
        new AwapiRateLimiter(
            getIntConfigValue(
                MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR_PROPERTY,
                MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR_DEFAULT),
            getIntConfigValue(
                TIMEOUT_ON_RATE_EXCEEDED_ERROR_PROPERTY, TIMEOUT_ON_RATE_EXCEEDED_ERROR_DEFAULT));

    /**
     * Gets the specified system property's value as an integer. If the value is missing, cannot be
     * parsed as an integer or is negative, returns {@code defaultValue}.
     *
     * @param propertyName the name of the system property.
     * @param defaultValue the default value for the system property.
     * @return the value of the system property as an int, if it's available and valid, else the
     * {@code defaultValue}
     */
    private static int getIntConfigValue(String propertyName, int defaultValue) {
      String propertyValueStr = System.getProperty(propertyName, String.valueOf(defaultValue));
      int propertyValue = defaultValue;
      try {
        propertyValue = Integer.parseInt(propertyValueStr);
      } catch (NumberFormatException e) {
        // Just swallow it, and propertyValue is still default.
      }
      
      return propertyValue >= 0 ? propertyValue : defaultValue;
    }
  }
}