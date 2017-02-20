// Copyright 2017 Google Inc. All Rights Reserved.
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

package com.google.api.ads.adwords.keywordoptimizer.formula;

import com.google.api.ads.adwords.axis.v201609.cm.Money;
import com.google.api.ads.adwords.axis.v201609.o.StatsEstimate;
import com.google.api.ads.adwords.keywordoptimizer.TrafficEstimate;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * This class holds the context for evaluating a users-specified formula. It contains the values
 * of variables that have been filled in previous steps from traffic estimates.
 */
public class FormulaContext {

  // This map holds the values of variables.
  private final Map<String, Double> values;
  
  /**
   * Creates a new context for formula evaluation. If a {@link TrafficEstimate} is provided, then
   * the context is initialized with variables holding statistics for the given estimate.
   */
  public FormulaContext(@Nullable TrafficEstimate estimate) {
    values = new HashMap<String, Double>();

    if (estimate != null) {
      init(estimate);
    }
  }

  /** 
   * Returns the current value of a specified variable or NaN in case no value has been set.
   */
  public double getValue(String identifier) {
    if (values.containsKey(identifier)) {
      return values.get(identifier);
    }

    return Double.NaN;
  }

  /**
   * Checks whether a specified variable has been set.
   */
  public boolean hasValue(String name) {
    return values.containsKey(name);
  }
  
  /**
   * Sets the value of a variable.
   */
  public void setValue(String name, double value) {
    values.put(name, value);
  }
  
  /**
   * Initializes variables and their values based on a given {@link TrafficEstimate}. 
   */
  private void init(TrafficEstimate estimate) {
    addValues("min", estimate.getMin());
    addValues("mean", estimate.getMean());
    addValues("max", estimate.getMax());
  }

  /**
   * Initializes variables and their values based on a given {@link StatsEstimate}. 
   */
  private void addValues(String prefix, StatsEstimate stats) {
    values.put(prefix + ".averageCpc", toDoubleOrNaN(stats.getAverageCpc()));
    values.put(prefix + ".averagePosition", toDoubleOrNaN(stats.getAveragePosition()));
    values.put(prefix + ".clickThroughRate", toDoubleOrNaN(stats.getClickThroughRate()));
    values.put(prefix + ".clicksPerDay", toDoubleOrNaN(stats.getClicksPerDay()));
    values.put(prefix + ".impressionsPerDay", toDoubleOrNaN(stats.getImpressionsPerDay()));
    values.put(prefix + ".totalCost", toDoubleOrNaN(stats.getTotalCost()));
  }

  /**
   * Converts a given {@link Money} object to a number (or NaN if null).
   */
  private static double toDoubleOrNaN(Money value) {
    if (value == null || value.getMicroAmount() == null) {
      return Double.NaN;
    }
    return value.getMicroAmount().doubleValue() / 1000000;
  }

  /**
   * Converts a given {@link Double} object to a number (or NaN if null).
   */
  private static double toDoubleOrNaN(Double value) {
    if (value == null) {
      return Double.NaN;
    }
    return value;
  }

  /**
   * Converts a given {@link Float} object to a number (or NaN if null).
   */
  private static double toDoubleOrNaN(Float value) {
    if (value == null) {
      return Double.NaN;
    }
    return value.doubleValue();
  }
}
