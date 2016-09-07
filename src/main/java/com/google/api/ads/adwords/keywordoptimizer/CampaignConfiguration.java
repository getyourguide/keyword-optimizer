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
import com.google.api.ads.adwords.axis.v201607.cm.Language;
import com.google.api.ads.adwords.axis.v201607.cm.Location;
import com.google.api.ads.adwords.axis.v201607.cm.Money;
import com.google.api.ads.adwords.axis.v201607.o.TrafficEstimatorService;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

/**
 * Campaign-level settings for keyword evaluation. Keywords are usually evaluated in a context
 * (maximum CPC and additional criteria like location, language, etc.) in order to obtain realistic
 * estimates. This class contains the settings that make up this context.
 */
public class CampaignConfiguration {
  private final ImmutableList<Criterion> additionalCriteria;
  private final Money maxCpc;
  
  /**
   * Creates a new configuration using the given maximum CPC and a list of additional criteria. This
   * should never be invoked from the outside, use the {@link CampaignConfiguration#builder}
   * instead.
   *
   * @param maxCpc the maximum cpc (cost per click) as a {@link Money} object
   * @param additionalCriteria additional criteria (location, language, ...) used to refine the
   * traffic estimates / evaluations
   */
  private CampaignConfiguration(Money maxCpc, List<Criterion> additionalCriteria) {
    this.additionalCriteria = ImmutableList.copyOf(additionalCriteria);
    this.maxCpc = maxCpc;
  }

  /**
   * Returns the list of additional criteria (location, language, ...) used to refine the traffic
   * estimates / evaluations.
   */
  public ImmutableList<Criterion> getAdditionalCriteria() {
    return additionalCriteria;
  }

  /**
   * Returns the maximum cpc (as a {@link Money} object) used to evaluate traffic estimates.
   */
  public Money getMaxCpc() {
    return maxCpc;
  }
  
  /**
   * Creates a new builder for {@link CampaignConfiguration}.
   */
  public static CampaignConfigurationBuilder builder() {
    return new CampaignConfigurationBuilder();
  }
  
  /**
   * Builder for {@link CampaignConfiguration}.
   */
  public static class CampaignConfigurationBuilder {
    private final List<Criterion> additionalCriteria;
    private Money maxCpc;
    
    /**
     * Builder for {@link CampaignConfiguration}.
     */
    private CampaignConfigurationBuilder() {
      additionalCriteria = new ArrayList<Criterion>();
      maxCpc = null;
    }

    /**
     * Adds an additional {@link Criterion} to refine the traffic estimates / evaluations (see
     * <a href="https://goo.gl/x1wpFS">list of AdWords API criteria</a>). For example, a
     * {@link Location} limits the estimated statistics to a specific geographic area. These
     * criteria are used when querying the {@link TrafficEstimatorService}.
     *
     * @param criterion an additional criterion
     */
    public CampaignConfigurationBuilder withCriterion(Criterion criterion) {
      additionalCriteria.add(criterion);
      return this;
    }
    
    /**
     * Adds a given location to the list of an additional criteria (convenience method).
     * 
     * @param locationId the ID of the location (e.g. 1023191 = New York)
     */
    public CampaignConfigurationBuilder withLocation(long locationId) {
      Location location = new Location();
      location.setId(locationId);

      return withCriterion(location);
    }
    
    /**
     * Adds a given language to the list of an additional criteria (convenience method)
     * 
     * @param languageId the ID of the language (e.g. 1000 = English)
     */
    public CampaignConfigurationBuilder withLanguage(long languageId) {
      Language language = new Language();
      language.setId(languageId);

      return withCriterion(language);
    }
    
    /**
     * Set the maximum cpc for retrieving accurate estimates (see
     * <a href="https://goo.gl/v61zXt">max. CPC definition</a>).
     *
     * @param maxCpc the maximum cpc (cost per click) as a {@link Money} object
     */
    public CampaignConfigurationBuilder withMaxCpc(Money maxCpc) {
      this.maxCpc = maxCpc;
      return this;
    }
    
    /**
     * Create the {@link CampaignConfiguration} object.
     */
    public CampaignConfiguration build() {
      return new CampaignConfiguration(maxCpc, additionalCriteria);
    }
  }
}
