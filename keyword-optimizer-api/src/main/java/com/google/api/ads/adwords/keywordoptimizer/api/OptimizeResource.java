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

package com.google.api.ads.adwords.keywordoptimizer.api;

import com.google.api.ads.adwords.axis.v201609.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201609.o.TargetingIdeaServiceInterface;
import com.google.api.ads.adwords.keywordoptimizer.AdWordsApiUtil;
import com.google.api.ads.adwords.keywordoptimizer.AlternativesFinder;
import com.google.api.ads.adwords.keywordoptimizer.CachedEstimator;
import com.google.api.ads.adwords.keywordoptimizer.CampaignConfiguration;
import com.google.api.ads.adwords.keywordoptimizer.CampaignConfiguration.CampaignConfigurationBuilder;
import com.google.api.ads.adwords.keywordoptimizer.EstimatorBasedEvaluator;
import com.google.api.ads.adwords.keywordoptimizer.Evaluator;
import com.google.api.ads.adwords.keywordoptimizer.KeywordCollection;
import com.google.api.ads.adwords.keywordoptimizer.KeywordOptimizer;
import com.google.api.ads.adwords.keywordoptimizer.KeywordOptimizerException;
import com.google.api.ads.adwords.keywordoptimizer.KeywordOptimizerProperty;
import com.google.api.ads.adwords.keywordoptimizer.KeywordOptimizerUtil;
import com.google.api.ads.adwords.keywordoptimizer.OptimizationContext;
import com.google.api.ads.adwords.keywordoptimizer.Optimizer;
import com.google.api.ads.adwords.keywordoptimizer.RoundStrategy;
import com.google.api.ads.adwords.keywordoptimizer.ScoreCalculator;
import com.google.api.ads.adwords.keywordoptimizer.SeedGenerator;
import com.google.api.ads.adwords.keywordoptimizer.SimpleSeedGenerator;
import com.google.api.ads.adwords.keywordoptimizer.TisCategorySeedGenerator;
import com.google.api.ads.adwords.keywordoptimizer.TisSearchTermsSeedGenerator;
import com.google.api.ads.adwords.keywordoptimizer.TisUrlSeedGenerator;
import com.google.api.ads.adwords.keywordoptimizer.TrafficEstimator;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.client.util.Strings;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main resource for keyword optimization. Requests to this resource can be made via the following
 * path:
 *
 * <p><code>[host/domain]/api/optimize/[method]?[param=value]</code>
 *
 * <ul>
 *   <li><code>[host/domain]</code> where your service is hosted
 *   <li><code>api/optimize</code> is a static path prefix for this resource
 *   <li><code>[method]</code> determines how the seed keywords are derived for the optimization
 *       process:
 *       <ul>
 *         <li><code>url</code> uses a URL to derive seed keywords
 *         <li><code>category</code> uses a business category
 *         <li><code>term</code> uses a search terms
 *         <li><code>keyword</code> uses the given keywords as a seed "as-is"
 *       </ul>
 *   <li><code>[param=value]</code> are additional parameters, such as max. CPC, language / location
 *       setting
 * </ul>
 *
 * As per Jersey lifecycle, this class instance is initiated for each request. Common request
 * parameters (CPC, languages, ...) are members and shared between different optimization methods
 * (url, category, ...).
 */
@Path("/optimize")
public class OptimizeResource {
  
  private static final String DEFAULT_PROPERTIES_PATH = "/keyword-optimizer.properties";
  private static final String DEFAULT_ADS_PROPERTIES_PATH = "/ads.properties";
  
  private static final Logger logger = LoggerFactory.getLogger(OptimizeResource.class);
  
  private static final String DEFAULT_CPC = "1.00"; // 1 USD.

  // The following members are parsed from the URL and injected by Jersey.
  @Context
  private UriInfo info;
  
  @Context
  private ServletConfig servletConfig;
  
  @DefaultValue(DEFAULT_CPC)
  @QueryParam("cpc") 
  private double maxCpc;
  
  @QueryParam("m") 
  private Set<String> matchTypes;
  
  @QueryParam("loc")
  private Set<Long> locations;
  
  @QueryParam("lang") 
  private Set<Long> languages;
  
  @DefaultValue("false")
  @QueryParam("pretty") 
  private boolean prettyPrint;
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String optimize() {
    return JsonUtil.convertToJson(new KeywordOptimizerException("No such method"), prettyPrint);
  }

  /**
   * Optimize keywords based on a seed URL.
   * 
   * @see TisUrlSeedGenerator
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("url")
  public String optimizeUrl(@QueryParam("url") String url) {
    logger.info("Request Path: " + info.getPath());
    
    try {
      requireParam("url", url);
      
      // Initialize common objects
      Set<KeywordMatchType> parsedMatchTypes = parseMatchTypes(matchTypes);
      CampaignConfiguration campaignConfiguration =
          createCampaignConfiguration(maxCpc, locations, languages);
      OptimizationContext context = createContext();
      
      // Create seed generator.
      TisUrlSeedGenerator seedGenerator = new TisUrlSeedGenerator(
          context.getAdwordsApiUtil().getService(TargetingIdeaServiceInterface.class),
          context.getAdwordsApiUtil().getClientCustomerId(),
          parsedMatchTypes,
          campaignConfiguration);
      seedGenerator.addUrl(url);

      // Find keywords.
      KeywordCollection bestKeywords = optimize(context, seedGenerator);
      return JsonUtil.convertToJson(bestKeywords, prettyPrint);
    } catch (Exception e) {
      return JsonUtil.convertToJson(e, prettyPrint);
    }
  }
  
  /**
   * Optimize keywords based on a seed category.
   * 
   * @see TisCategorySeedGenerator
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("category")
  public String optimizeCategory(@QueryParam("category") int category) {
    logger.info("Request Path: " + info.getPath());
    
    try {
      requireParam("category", category);
      
      // Initialize common objects
      Set<KeywordMatchType> parsedMatchTypes = parseMatchTypes(matchTypes);
      CampaignConfiguration campaignConfiguration =
          createCampaignConfiguration(maxCpc, locations, languages);
      OptimizationContext context = createContext();
      
      // Create seed generator.
      TisCategorySeedGenerator seedGenerator =
          new TisCategorySeedGenerator(
              context.getAdwordsApiUtil().getService(TargetingIdeaServiceInterface.class),
              context.getAdwordsApiUtil().getClientCustomerId(),
              category,
              parsedMatchTypes,
              campaignConfiguration);

      // Find keywords.
      KeywordCollection bestKeywords = optimize(context, seedGenerator);
      return JsonUtil.convertToJson(bestKeywords, prettyPrint);
    } catch (Exception e) {
      return JsonUtil.convertToJson(e, prettyPrint);
    }
  }
  
  /**
   * Optimize keywords based on a search terms.
   * 
   * @see TisSearchTermsSeedGenerator
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("term")
  public String optimizeSearchTerms(@QueryParam("term") Set<String> searchTerms) {
    logger.info("Request Path: " + info.getPath());
    
    try {
      requireParam("term", searchTerms);
      
      // Initialize common objects
      Set<KeywordMatchType> parsedMatchTypes = parseMatchTypes(matchTypes);
      CampaignConfiguration campaignConfiguration =
          createCampaignConfiguration(maxCpc, locations, languages);
      OptimizationContext context = createContext();
      
      // Create seed generator.
      TisSearchTermsSeedGenerator seedGenerator =
          new TisSearchTermsSeedGenerator(
              context.getAdwordsApiUtil().getService(TargetingIdeaServiceInterface.class),
              context.getAdwordsApiUtil().getClientCustomerId(),
              parsedMatchTypes,
              campaignConfiguration);
      for (String term : searchTerms) {
        seedGenerator.addSearchTerm(term);
      }

      // Find keywords.
      KeywordCollection bestKeywords = optimize(context, seedGenerator);
      return JsonUtil.convertToJson(bestKeywords, prettyPrint);
    } catch (Exception e) {
      return JsonUtil.convertToJson(e, prettyPrint);
    }
  }
  
  /**
   * Optimize keywords based on a given set of seed keywords.
   * 
   * @see SimpleSeedGenerator
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("keyword")
  public String optimizeKeywords(@QueryParam("keyword") Set<String> keywords) {
    logger.info("Request Path: " + info.getPath());

    try {
      requireParam("term", keywords);

      // Initialize common objects
      Set<KeywordMatchType> parsedMatchTypes = parseMatchTypes(matchTypes);
      CampaignConfiguration campaignConfiguration =
          createCampaignConfiguration(maxCpc, locations, languages);
      OptimizationContext context = createContext();

      // Create seed generator.
      SimpleSeedGenerator seedGenerator =
          new SimpleSeedGenerator(parsedMatchTypes, campaignConfiguration);
      for (String keyword : keywords) {
        seedGenerator.addKeyword(keyword);
      }

      // Find keywords.
      KeywordCollection bestKeywords = optimize(context, seedGenerator);
      return JsonUtil.convertToJson(bestKeywords, prettyPrint);
    } catch (Exception e) {
      return JsonUtil.convertToJson(e, prettyPrint);
    }
  }
  
  /**
   * This method performs the actual round-based optimization, given a seed generator created by the
   * other resource methods.
   *
   * @return a collection returning an optimized set of keywords.
   */
  private KeywordCollection optimize(OptimizationContext context, SeedGenerator seedGenerator)
      throws KeywordOptimizerException {
    // Create strategy objects.
    AlternativesFinder alternativesFinder =
        KeywordOptimizer.createObjectBasedOnProperty(
            AlternativesFinder.class, KeywordOptimizerProperty.AlternativesFinderClass, context);
    TrafficEstimator estimator =
        KeywordOptimizer.createObjectBasedOnProperty(
            TrafficEstimator.class, KeywordOptimizerProperty.EstimatorClass, context);
    ScoreCalculator scoreCalculator =
        KeywordOptimizer.createObjectBasedOnProperty(
            ScoreCalculator.class, KeywordOptimizerProperty.ScoreCalculatorClass, context);

    Evaluator evaluator =
        new EstimatorBasedEvaluator(new CachedEstimator(estimator), scoreCalculator);

    RoundStrategy roundStrategy =
        KeywordOptimizer.createObjectBasedOnProperty(
            RoundStrategy.class, KeywordOptimizerProperty.RoundStrategyClass, context);

    // Create and run the optimizer.
    Optimizer optimizer =
        new Optimizer(seedGenerator, alternativesFinder, evaluator, roundStrategy);

    KeywordCollection bestKeywords = optimizer.optimize();
    return bestKeywords;
  }
  
  /**
   * Creates the context for the optimization process, holding properties and the AdWords API
   * session.
   */
  private OptimizationContext createContext() throws KeywordOptimizerException {
    try {
      String adsPropertiesPath =
          Strings.isNullOrEmpty(servletConfig.getInitParameter(ApiServer.INIT_PARAM_ADS_PROPERTIES))
              ? DEFAULT_ADS_PROPERTIES_PATH
              : servletConfig.getInitParameter(ApiServer.INIT_PARAM_ADS_PROPERTIES);

      String propertiesPath =
          Strings.isNullOrEmpty(servletConfig.getInitParameter(ApiServer.INIT_PARAM_PROPERTIES))
              ? DEFAULT_PROPERTIES_PATH
              : servletConfig.getInitParameter(ApiServer.INIT_PARAM_PROPERTIES);
      
      AdWordsApiUtil util = new AdWordsApiUtil(adsPropertiesPath);
      
      PropertiesConfiguration properties = new PropertiesConfiguration();
      InputStream is = null;
      
      // Check first if properties files exists and load properties from there. Otherwise attempt
      // to load it from the classpath. 
      File propertiesFile = new File(propertiesPath);
      if (propertiesFile.exists()) {
        is = new BufferedInputStream(new FileInputStream(propertiesFile));
      } else {
        is = OptimizeResource.class.getResourceAsStream(propertiesPath);
      }
      
      if (is == null) {
        throw new KeywordOptimizerException("Properties not found in " + propertiesPath);
      }
      
      properties.load(is);
      is.close();
      
      return new OptimizationContext(properties, util);
    } catch (ConfigurationLoadException e) {
      throw new KeywordOptimizerException("Error loading the ads properties file", e);
    } catch (ValidationException e) {
      throw new KeywordOptimizerException("Missing refresh token", e);
    } catch (OAuthException e) {
      throw new KeywordOptimizerException("Authentication error", e);
    } catch (IOException e) {
      throw new KeywordOptimizerException("Error loading the configuration file", e);
    } catch (ConfigurationException e) {
      throw new KeywordOptimizerException("Error parsing the configuration file", e);
    }
  }
  
  /**
   * Creates the campaign configuration for the optimization process, holding campaign-level
   * properties such as max. cpc and additional language / location criteria.
   */
  private static CampaignConfiguration createCampaignConfiguration(
      double maxCpc, Collection<Long> locations, Collection<Long> languages) {
    CampaignConfigurationBuilder builder = CampaignConfiguration.builder();

    builder.withMaxCpc(KeywordOptimizerUtil.createMoney(maxCpc));
    for (long locationId : locations) {
      builder.withLocation(locationId);
    }
    for (long languageId : languages) {
      builder.withLanguage(languageId);
    }

    return builder.build();
  }
  
  /**
   * Tests if a URL parameter is empty and throws an exception if that's the case. Empty here means
   * either <code>null</code>, an empty string or and empty list.
   */
  private static void requireParam(String name, Object value) throws KeywordOptimizerException {
    if (value == null
        || (value instanceof Collection<?> && ((Collection<?>) value).isEmpty())
        || (value instanceof String && Strings.isNullOrEmpty((String) value))) {
      throw new KeywordOptimizerException("Parameter '" + name + "' is required");
    }
  }
  
  /**
   * Converts a collection of keyword match types defined as a string to actual {@link
   * KeywordMatchType} objects.
   */
  private static Set<KeywordMatchType> parseMatchTypes(Collection<String> matchTypes) {
    Set<KeywordMatchType> parsedMatchTypes = new HashSet<KeywordMatchType>();

    for (String matchType : matchTypes) {
      parsedMatchTypes.add(KeywordMatchType.fromString(matchType));
    }

    return parsedMatchTypes;
  }
}
