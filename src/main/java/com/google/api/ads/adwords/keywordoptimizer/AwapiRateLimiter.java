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
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.api.ads.adwords.axis.v201607.cm.ApiError;
import com.google.api.ads.adwords.axis.v201607.cm.ApiException;
import com.google.api.ads.adwords.axis.v201607.cm.RateExceededError;
import com.google.common.util.concurrent.AtomicLongMap;
import com.google.common.util.concurrent.Uninterruptibles;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A rate limiter implementation that handles RateExceededError for AdWords API applications. It has
 * a singleton for each category of services (only reporting service and other services for now),
 * with thread-safe lazy initialization in order to support custom configurations (e.g., number of
 * attempts, timeout seconds).
 *
 * <p>
 * To change the default configuration, set the system properties
 * {@link #MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR_PROPERTY} and
 * {@link #TIMEOUT_ON_RATE_EXCEEDED_ERROR_PROPERTY} <em>before</em> calling
 * {@link #getInstance(AwapiRateLimiter.RateLimitBucket)} for the first time.
 *
 * <p>
 * The features include:
 * <ul>
 * <li>Differentiates between categories of services, so one service encountering rate limit won't
 * stop calling other services
 * <li>Ensures thread safety: it supports the application to invoke AdWords API from multiple
 * threads
 * <li>Comprehends info in RateExceededError, especially rateScope and retryAfterSeconds
 * <li>Handles retry logic for the thread encountering RateExceededError automatically
 * <li>Differentiates AdWords API calls v.s. other code logic
 * <li>Easy to configure, and easy to plug into custom code
 * </ul>
 */
public final class AwapiRateLimiter {
  private static final Logger logger = LoggerFactory.getLogger(AwapiRateLimiter.class);

  /**
   * The rate limit bucket, so that when one bucket encounters rate limiting, other buckets may
   * still run.
   *
   * <p>
   * Currently it only differentiates reporting service and "others".
   */
  public enum RateLimitBucket {
    REPORTING("reporting service"),
    OTHERS("other services");

    private final String displayName;

    private RateLimitBucket(String displayName) {
      this.displayName = displayName;
    }

    @Override
    public String toString() {
      return displayName;
    }
  }

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

  /**
   * Number of min / max randomization of extra waiting time, in milliseconds.
   */
  public static final int MIN_RANDOM_EXTRA_MILLIS = 100;
  public static final int MAX_RANDOM_EXTRA_MILLIS = 500;
  // Randomizer object.
  private static final Random rand = new Random();

  // Number of attempts on RateExceededError, 0 means infinite attempts.
  private final int maxAttemptsOnRateExceededError;
  // Number of seconds for timeout the waiting, 0 means no timeout.
  private final int timeoutOnRateExceededError;
  // The rate limit bucket.
  private final RateLimitBucket bucket;

  // Wait until time (in millis of DateTime) for token scope.
  private final AtomicLong tokenWaitUntil = new AtomicLong();
  // Wait until time (in millis of DateTime) for account scope.
  private final AtomicLongMap<Long> accountWaitUntil = AtomicLongMap.create();

  // Map that holds rate limiter instances.
  private static ConcurrentHashMap<RateLimitBucket, AwapiRateLimiter> rateLimiters =
      new ConcurrentHashMap<RateLimitBucket, AwapiRateLimiter>();

  private AwapiRateLimiter(
      int maxAttemptsOnRateExceededError, int timeoutOnRateExceededError, RateLimitBucket bucket) {
    this.maxAttemptsOnRateExceededError = maxAttemptsOnRateExceededError;
    this.timeoutOnRateExceededError = timeoutOnRateExceededError;
    this.bucket = bucket;
  }

  /**
   * Get the only rate limiter instance (with initialize-on-demand) of the specified bucket.
   */
  public static AwapiRateLimiter getInstance(RateLimitBucket bucket) {
    AwapiRateLimiter retval = rateLimiters.get(bucket);
    if (retval == null) {
      final AwapiRateLimiter rateLimiter = new AwapiRateLimiter(
          RateLimiterConfig.MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR,
          RateLimiterConfig.TIMEOUT_ON_RATE_EXCEEDED_ERROR,
          bucket);
      retval = rateLimiters.putIfAbsent(bucket, rateLimiter);
      if (retval == null) {
        retval = rateLimiter;
      }
    }

    return retval;
  }

  /**
   * Update the wait time for TOKEN scope.
   *
   * @param waitForMills the wait time in milliseconds
   */
  private void updateTokenWaitTime(long waitForMills) {
    final long newTime = DateTime.now().getMillis() + waitForMills;

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
   *
   * @param clientCustomerId the client customer ID
   * @param waitForMills the wait time in milliseconds
   */
  private void updateAccountWaitTime(Long clientCustomerId, long waitForMills) {
    final long newTime = DateTime.now().getMillis() + waitForMills;

    boolean done = true;
    do {
      long oldTime = accountWaitUntil.get(clientCustomerId);
      // If the new wait until time exceeds current one, update it; otherwise
      // just skip the loop.
      if (oldTime < newTime) {
        done = (oldTime == accountWaitUntil.getAndAdd(clientCustomerId, newTime - oldTime));
      } else {
        done = true;
      }
    } while (!done);
  }

  /**
   * Wait and update the wait until time accordingly.
   *
   * @param clientCustomerId the client customer ID for the AdWords API invocation
   * @param lastApiException the last {@link ApiException} for the request
   */
  private void wait(Long clientCustomerId, @Nullable ApiException lastApiException) {
    long nowInMillis = DateTime.now().getMillis();

    long waitForMillis = 0L;
    waitForMillis = Math.max(waitForMillis, tokenWaitUntil.get() - nowInMillis);

    // clientCustomerId could be null, e.g., for ReportDefinitionService invocation.
    if (clientCustomerId != null) {
      waitForMillis = Math.max(waitForMillis, accountWaitUntil.get(clientCustomerId) - nowInMillis);
    }

    if (waitForMillis > 0) {
      if (timeoutOnRateExceededError > 0
          && waitForMillis > MILLISECONDS.convert(timeoutOnRateExceededError, SECONDS)) {
        throw new RuntimeException(
            "Need to wait too much time (more than " + timeoutOnRateExceededError + " seconds).",
            lastApiException);
      }

      logger.info("Thread \"{}\" sleeping for {} millis due to rate limit on {}.",
          Thread.currentThread().getName(), waitForMillis, bucket);
      Uninterruptibles.sleepUninterruptibly(waitForMillis, MILLISECONDS);
    }
  }

  /**
   * The core method for invoking AdWords API call and handling RateExceededError.
   *
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
            logger.info("Encountered RateExceededError: scope={}, seconds={} on {}.",
                scope, rateExceeded.getRetryAfterSeconds(), bucket);

            Integer retryAfterSeconds = rateExceeded.getRetryAfterSeconds();
            if (retryAfterSeconds == null) {
              continue;
            }

            long waitForMills =
                MILLISECONDS.convert(retryAfterSeconds, SECONDS) + getRandomExtraMillis();

            if ("DEVELOPER".equals(scope)) {
              updateTokenWaitTime(waitForMills);
            } else if ("ACCOUNT".equals(scope)) {
              updateAccountWaitTime(clientCustomerId, waitForMills);
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
      throw new RuntimeException(
          "Failed to get a valid result with AdWords API invocation after "
              + maxAttemptsOnRateExceededError
              + " attempts on RateExceededError.",
          lastApiException);
    }

    return result;
  }

  /**
   * Calculate random extra time in milliseconds.
   */
  private static int getRandomExtraMillis() {
    return rand.nextInt(MAX_RANDOM_EXTRA_MILLIS - MIN_RANDOM_EXTRA_MILLIS)
        + MIN_RANDOM_EXTRA_MILLIS;
  }

  /**
   * Thread-safe helper that holds {@link AwapiRateLimiter} configuration.
   */
  private static final class RateLimiterConfig {
    private static final int MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR =
        getIntConfigValue(
            MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR_PROPERTY,
            MAX_ATTEMPTS_ON_RATE_EXCEEDED_ERROR_DEFAULT);

    private static final int TIMEOUT_ON_RATE_EXCEEDED_ERROR =
        getIntConfigValue(
            TIMEOUT_ON_RATE_EXCEEDED_ERROR_PROPERTY, TIMEOUT_ON_RATE_EXCEEDED_ERROR_DEFAULT);

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
