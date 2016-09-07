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

import com.google.api.ads.adwords.axis.v201607.cm.Criterion;
import com.google.api.ads.adwords.axis.v201607.cm.Keyword;
import com.google.api.ads.adwords.axis.v201607.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201607.cm.Language;
import com.google.api.ads.adwords.axis.v201607.cm.Location;
import com.google.api.ads.adwords.axis.v201607.cm.Money;
import com.google.api.ads.adwords.axis.v201607.o.LanguageSearchParameter;
import com.google.api.ads.adwords.axis.v201607.o.LocationSearchParameter;
import com.google.api.ads.adwords.axis.v201607.o.SearchParameter;
import com.google.api.ads.adwords.axis.v201607.o.StatsEstimate;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions (math, strings, ...) for various other classes in this project.
 */
public class KeywordOptimizerUtil {
  private static final String PLACEHOLDER_NULL = "       ---";
  private static final String FORMAT_NUMBER = "%10.3f";
  private static final String FORMAT_MONEY = "%10.2f";
  private static final int MICRO_UNITS = 1000000;
  
  /**
   * Calculates the mean estimated statistics based on minimum and maximum values.
   *
   * @param min the minimum estimated statistics
   * @param max the maximum estimated statistics
   * @return the mean value for all given stats
   */
  public static StatsEstimate calculateMean(StatsEstimate min, StatsEstimate max) {
    Money meanAverageCpc = calculateMean(min.getAverageCpc(), max.getAverageCpc());
    Double meanAveragePosition = calculateMean(min.getAveragePosition(), max.getAveragePosition());
    Double meanClicks = calculateMean(min.getClicksPerDay(), max.getClicksPerDay());
    Double meanImpressions = calculateMean(min.getImpressionsPerDay(), max.getImpressionsPerDay());
    Double meanCtr = calculateMean(min.getClickThroughRate(), max.getClickThroughRate());
    Money meanTotalCost = calculateMean(min.getTotalCost(), max.getTotalCost());

    StatsEstimate mean = new StatsEstimate();

    if (meanAverageCpc != null) {
      mean.setAverageCpc(meanAverageCpc);
    }

    if (meanAveragePosition != null) {
      mean.setAveragePosition(meanAveragePosition);
    }

    if (meanClicks != null) {
      mean.setClicksPerDay(meanClicks.floatValue());
    }

    if (meanImpressions != null) {
      mean.setImpressionsPerDay(meanImpressions.floatValue());
    }

    if (meanCtr != null) {
      mean.setClickThroughRate(meanCtr);
    }

    if (meanTotalCost != null) {
      mean.setTotalCost(meanTotalCost);
    }

    return mean;
  }

  /**
   * Creates the sum / average values for a given list of {@link StatsEstimate}s.
   * 
   * @param estimates the list of stats
   * @return the mean value for all given stats
   */
  public static StatsEstimate getOverallEstimate(List<StatsEstimate> estimates) {
    float sumClicks = 0;
    float sumImpressions = 0;
    float sumTotalCost = 0;
    double sumAvgPosition = 0;
    int countAvgPosition = 0;
    double sumAvgCpc = 0;
    int countAvgCpc = 0;
    double sumAvgCtr = 0;
    int countAvgCtr = 0;

    for (StatsEstimate estimate : estimates) {
      if (estimate.getClicksPerDay() != null) {
        sumClicks += estimate.getClicksPerDay();
      }
      if (estimate.getImpressionsPerDay() != null) {
        sumImpressions += estimate.getImpressionsPerDay();
      }
      if (estimate.getTotalCost() != null) {
        sumTotalCost += estimate.getTotalCost().getMicroAmount();
      }
      if (estimate.getAveragePosition() != null) {
        sumAvgPosition += estimate.getAveragePosition();
        countAvgPosition++;
      }
      if (estimate.getAverageCpc() != null) {
        sumAvgCpc += estimate.getAverageCpc().getMicroAmount();
        countAvgCpc++;
      }
      if (estimate.getClickThroughRate() != null) {
        sumAvgCtr += estimate.getClickThroughRate();
        countAvgCtr++;
      }
    }

    double avgPosition = 0;
    if (countAvgPosition > 0) {
      avgPosition = sumAvgPosition / countAvgPosition;
    }

    double avgCtr = 0;
    if (countAvgCtr > 0) {
      avgCtr = sumAvgCtr / countAvgCtr;
    }

    Money avgCpc = null;
    if (countAvgCpc > 0) {
      avgCpc = new Money();
      avgCpc.setMicroAmount((long) (sumAvgCpc / countAvgCpc));
    }

    Money totalCost = new Money();
    totalCost.setMicroAmount((long) sumTotalCost);

    StatsEstimate stats = new StatsEstimate();
    stats.setAverageCpc(avgCpc);
    stats.setAveragePosition(avgPosition);
    stats.setClicksPerDay(sumClicks);
    stats.setClickThroughRate(avgCtr);
    stats.setImpressionsPerDay(sumImpressions);
    stats.setTotalCost(totalCost);

    return stats;
  }

  /**
   * Returns the mean of the two {@link Money} values if neither is null, else returns null.
   * 
   * @param value1 first value
   * @param value2 second value
   * @return the mean of the two {@link Money} values
   */
  public static Money calculateMean(Money value1, Money value2) {
    if (value1 == null || value2 == null) {
      return null;
    }

    Double meanAmount = calculateMean(value1.getMicroAmount(), value2.getMicroAmount());
    if (meanAmount == null) {
      return null;
    }

    Money mean = new Money();
    mean.setMicroAmount(meanAmount.longValue());

    return mean;
  }

  /**
   * Returns the mean of the two {@link Number} values if neither is null, else returns null.
   * 
   * @param value1 first value
   * @param value2 second value
   * @return the mean of the two {@link Money} values
   */
  public static Double calculateMean(Number value1, Number value2) {
    if (value1 == null || value2 == null) {
      return null;
    }
    return (value1.doubleValue() + value2.doubleValue()) / 2;
  }


  /**
   * Returns a string representation of the given {@link Keyword}. Please note, as not all classes
   * belong to the project itself, toString methods are bundled here.
   * 
   * @param keyword the keyword
   * @return a string representation of the keyword
   */
  public static String toString(Keyword keyword) {
    return keyword.getText() + "[" + keyword.getMatchType().getValue() + "]";
  }

  /**
   * Returns a string representation of the given {@link StatsEstimate}. Please note, as not all
   * classes belong to the project itself, toString methods are bundled here.
   * 
   * @param estimate the estimate
   * @return a string representation of the estimate
   */
  public static String toString(StatsEstimate estimate) {
    StringBuilder out = new StringBuilder();
    out.append("Imp: ").append(format(estimate.getImpressionsPerDay()));
    out.append(" Cli:  ").append(format(estimate.getClicksPerDay()));
    out.append(" Ctr:  ").append(format(estimate.getClickThroughRate()));
    out.append(" Pos:  ").append(format(estimate.getAveragePosition()));
    out.append(" Cpc:  ").append(format(estimate.getAverageCpc()));
    out.append(" Cos:  ").append(format(estimate.getTotalCost()));

    return out.toString();
  }

  /**
   * Convenience method for creating a new keyword.
   * 
   * @param text the keyword text
   * @param matchType the match type (BROAD, PHRASE, EXACT)
   * @return the newly created {@link Keyword}
   */
  public static Keyword createKeyword(String text, KeywordMatchType matchType) {
    Keyword keyword = new Keyword();
    keyword.setMatchType(matchType);
    keyword.setText(text);
    return keyword;
  }

  /**
   * Formats a given number in a default format (3 decimals, padded left to 10 characters).
   * 
   * @param nr a number
   * @return a string version of the number
   */
  public static String format(Float nr) {
    if (nr == null) {
      return PLACEHOLDER_NULL;
    }

    return String.format(FORMAT_NUMBER, nr);
  }

  /**
   * Formats a given number in a default format (3 decimals, padded left to 10 characters).
   * 
   * @param nr a number
   * @return a string version of the number
   */
  public static String format(Double nr) {
    if (nr == null) {
      return PLACEHOLDER_NULL;
    }

    return String.format(FORMAT_NUMBER, nr);
  }
  
  /**
   * Formats a given number for CSV output (effectively handles null values).
   * 
   * @param nr a number
   * @return a string version of the number
   */
  public static String formatCsv(Number nr) {
    if (nr == null) {
      return "";
    }

    return nr.toString();
  }

  /**
   * Formats a given monetary value in a default format (2 decimals, padded left to 10 characters).
   * 
   * @param money a monetary value
   * @return a string version of the monetary value
   */
  public static String format(Money money) {
    long microAmount = 0;
    if (money != null) {
      microAmount = money.getMicroAmount();
    } else {
      return PLACEHOLDER_NULL;
    }

    double amount = (double) microAmount / MICRO_UNITS;
    return String.format(FORMAT_MONEY, amount);
  }
  
  /**
   * Returns all objects in the given list that are instances of the given class.
   * 
   * @param input list of objects to look through
   * @param typeClass class of the objects to filter
   * @return an array of all objects in the given list that are instances
   * of the given class
   */
  @SuppressWarnings(value = "unchecked")
  public static <Type> Type[] getAllOfType(List<?> input, Class<Type> typeClass) {
    List<Type> allEntriesOfType = new ArrayList<Type>();

    for (Object o : input) {
      if (typeClass.isInstance(o)) {
        allEntriesOfType.add((Type) o);
      }
    }

    return allEntriesOfType.toArray((Type[]) Array.newInstance(typeClass, 0));
  }

  /**
   * Converts a list of given trigger criteria to according {@link SearchParameter}s for the
   * TargetingIdeaService.
   * 
   * @param criteria the criteria to be converted
   * @return a list of according {@link SearchParameter}s
   */
  public static List<SearchParameter> toSearchParameters(List<Criterion> criteria) {
    List<SearchParameter> parameters = new ArrayList<SearchParameter>();

    // Take all location criteria and add as one searchParameter.
    Location[] allLocations = KeywordOptimizerUtil.getAllOfType(criteria, Location.class);
    if (allLocations.length > 0) {
      LocationSearchParameter locationParameter = new LocationSearchParameter();
      locationParameter.setLocations(allLocations);
      parameters.add(locationParameter);
    }

    // Take all language criteria and add as one searchParameter.
    Language[] allLanguages = KeywordOptimizerUtil.getAllOfType(criteria, Language.class);
    if (allLanguages.length > 0) {
      LanguageSearchParameter languageParameter = new LanguageSearchParameter();
      languageParameter.setLanguages(allLanguages);
      parameters.add(languageParameter);
    }

    // Any others are not supported right now.
    
    return parameters;
  }
}
