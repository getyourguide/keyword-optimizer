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

import com.google.api.ads.adwords.axis.v201509.cm.Criterion;
import com.google.api.ads.adwords.axis.v201509.cm.Language;
import com.google.api.ads.adwords.axis.v201509.cm.Location;
import com.google.api.ads.adwords.axis.v201509.cm.Money;
import com.google.api.ads.adwords.axis.v201509.o.TrafficEstimatorService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Base class for holding additional information for evaluating keywords. This contains a list of
 * additional criteria (location, language, ...) as well as a maximum cpc.
 */
public class AdditionalInfoHolder {
  private final List<Criterion> additionalCriteria;
  private Money maxCpc;

  /**
   * Creates a new information holder using the given max cpc.
   * 
   * @param maxCpc the maximum cpc (cost per click) as a {@link Money} object
   */
  public AdditionalInfoHolder(@Nullable Money maxCpc) {
    additionalCriteria = new ArrayList<Criterion>();
    this.maxCpc = maxCpc;
  }

  /**
   * Creates a new information holder based on the settings of a given one. This clones all
   * "additional" settings (additional criteria, max cpc), but not the ones defined in any 
   * subclasses.
   * 
   * @param other the prototype info holder to take the data from
   */
  public AdditionalInfoHolder(AdditionalInfoHolder other) {
    List<Criterion> otherCriteria = other.getAdditionalCriteria();
    Money otherMaxCpc = other.getMaxCpc();
    
    additionalCriteria = new ArrayList<Criterion>(otherCriteria.size());
    addAdditionalCriteria(otherCriteria);
    maxCpc =
        otherMaxCpc == null
            ? null : new Money(otherMaxCpc.getComparableValueType(), otherMaxCpc.getMicroAmount());
  }

  /**
   * Returns the list of additional criteria (location, language, ...) used to refine the traffic
   * estimates / evaluations.
   */
  public List<Criterion> getAdditionalCriteria() {
    return Collections.unmodifiableList(additionalCriteria);
  }

  /**
   * Adds an additional {@link Criterion} to refine the traffic estimates / evaluations. For
   * example, a {@link Location} limits the estimated statistics to a specific geographic area.
   * These criteria are used when querying the {@link TrafficEstimatorService}.
   *
   * @param criterion an additional criterion
   */
  public void addAdditionalCriterion(Criterion criterion) {
    additionalCriteria.add(criterion);
  }

  /**
   * Adds additional criteria to refine the traffic estimates / evaluations. For example, a {@link
   * Location} limits the estimated statistics to a specific geographic area. These criteria are 
   * used when querying the {@link TrafficEstimatorService}.
   * 
   * @param criteria a list of additional criteria
   */
  public void addAdditionalCriteria(Collection<Criterion> criteria) {
    additionalCriteria.addAll(criteria);
  }

  /**
   * Adds a given location to the list of an additional criteria (convenience method).
   * 
   * @param locationId the ID of the location (e.g. 1023191 = New York)
   */
  public void addAdditionalLocation(long locationId) {
    Location location = new Location();
    location.setId(locationId);

    addAdditionalCriterion(location);
  }

  /**
   * Adds a given language to the list of an additional criteria (convenience method)
   * 
   * @param languageId the ID of the language (e.g. 1000 = English)
   */
  public void addAdditionalLanguage(long languageId) {
    Language language = new Language();
    language.setId(languageId);

    addAdditionalCriterion(language);
  }

  /**
   * Set the max cpc for retrieving accurate estimates.
   * 
   * @param maxCpc the maximum cpc (cost per click) as a {@link Money} object
   */
  public void setMaxCpc(Money maxCpc) {
    this.maxCpc = maxCpc;
  }

  /**
   * Returns the maximum cpc (as a {@link Money} object) used to evaluate traffic estimates.
   */
  public Money getMaxCpc() {
    return maxCpc;
  }
}
