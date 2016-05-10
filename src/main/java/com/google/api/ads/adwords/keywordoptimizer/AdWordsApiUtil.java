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

import com.google.api.ads.adwords.axis.factory.AdWordsServices;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.auth.GoogleClientSecretsBuilder;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * This is a basic utility class for helping with AdWords API communication. It handles the
 * authentication by itself and provides method for returning the AdWords session and allows to 
 * create new service objects.
 * 
 * Note on thread safety: This class is not threadsafe due to presence of the enclosed 
 * AdWordsSession. See https://github.com/googleads/googleads-java-lib/wiki/Thread-Safety#sessions.
 */
@NotThreadSafe
public class AdWordsApiUtil {
  // The OAuth2 scope for the AdWords API.
  private static final String SCOPE = "https://www.googleapis.com/auth/adwords";

  // This callback URL will allow you to copy the token from the success screen.
  private static final String CALLBACK_URL = "urn:ietf:wg:oauth:2.0:oob";
  
  // Name of the user agent property in the configuration file. 
  private static final String PROPERTY_USER_AGENT = "api.adwords.userAgent";
  
  // Prefix that will be added to the user-specified user agent. 
  private static final String PREFIX_USER_AGENT = "KeywordOptimizer-";
  
  // Default user agent property value.
  private static final String DEFAULT_USER_AGENT = "INSERT_USERAGENT_HERE";

  private static final Logger logger = LoggerFactory.getLogger(AdWordsApiUtil.class);

  private AdWordsSession session;
  private AdWordsServices services;
  private String configPath;

  /**
   * Creates a new {@link AdWordsApiUtil} object based on the given properties.
   * 
   * @param configPath path of the ads.properties config file
   * @throws OAuthException in case of an authentication error
   * @throws ValidationException in case there is no refresh token
   * @throws ConfigurationLoadException in case of an error loading the config file
   */
  public AdWordsApiUtil(String configPath)
      throws OAuthException, ValidationException, ConfigurationLoadException {
    this.configPath = configPath;
    init();
  }

  /**
   * Creates a new oauth2 credential based on the given client secrets.
   * 
   * @param clientSecrets the client secrets (see developer console)
   * @return the newly created credential
   * @throws IOException in case of an error reading the configuration files
   */
  private static Credential getOAuth2Credential(GoogleClientSecrets clientSecrets)
      throws IOException {
    GoogleAuthorizationCodeFlow authorizationFlow =
        new GoogleAuthorizationCodeFlow
            .Builder(
                new NetHttpTransport(),
                new JacksonFactory(),
                clientSecrets,
                Lists.newArrayList(SCOPE))
            // Set the access type to offline so that the token can be refreshed. By default, the
            // library will automatically refresh tokens when it can, but this can be turned off by 
            // setting api.adwords.refreshOAuth2Token=false in your ads.properties file.
            .setAccessType("offline")
            .build();

    String authorizeUrl =
        authorizationFlow.newAuthorizationUrl().setRedirectUri(CALLBACK_URL).build();
    System.out.println("Paste this url in your browser: \n" + authorizeUrl + '\n');

    // Wait for the authorization code.
    System.out.println("Type the code you received here: ");
    String authorizationCode = new BufferedReader(new InputStreamReader(System.in)).readLine();

    // Authorize the OAuth2 token.
    GoogleAuthorizationCodeTokenRequest tokenRequest =
        authorizationFlow.newTokenRequest(authorizationCode);
    tokenRequest.setRedirectUri(CALLBACK_URL);
    GoogleTokenResponse tokenResponse = tokenRequest.execute();

    // Create the OAuth2 credential.
    GoogleCredential credential =
        new GoogleCredential.Builder()
            .setTransport(new NetHttpTransport())
            .setJsonFactory(new JacksonFactory())
            .setClientSecrets(clientSecrets)
            .build();

    // Set authorized credentials.
    credential.setFromTokenResponse(tokenResponse);

    return credential;
  }
  
  /**
   * Reads and returns the user agent setting from the properties file.
   * 
   * @throws ConfigurationLoadException in case of an error reading the configuration file
   * @throws ValidationException in case the user agent is not specified correctly
   */
  private String getUserAgentFromConfig() throws ConfigurationLoadException, ValidationException {
    InputStream inputStream = null;

    try {
      inputStream = new BufferedInputStream(new FileInputStream(configPath));

      Properties properties = new Properties();
      properties.load(inputStream);
      String userAgent = properties.getProperty(PROPERTY_USER_AGENT, "");
      
      // Check that user agent is not empty or the default.
      if (Strings.isNullOrEmpty(userAgent)
          || userAgent.contains(DEFAULT_USER_AGENT)) {
        throw new ValidationException(String.format(
            "User agent must be set and not be the default [%s]", DEFAULT_USER_AGENT),
            "userAgent");
      }      
      return userAgent;
    } catch (IOException e) {
      throw new ConfigurationLoadException("Problem reading configuration from " + configPath, e);
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          logger.warn("Error closing input stream", e);
        }
      }
    }
  }

  /**
   * Initializes this utility (credentials, sessions, ...).
   * 
   * @throws OAuthException in case of an authentication error
   * @throws ValidationException in case there is no refresh token
   * @throws ConfigurationLoadException in case of an error loading the config file
   */
  private void init() throws OAuthException, ValidationException, ConfigurationLoadException {
    logger.info("Initializing session and services");
    
    try {
      // Generate a refreshable OAuth2 credential similar to a ClientLogin token
      // and can be used in place of a service account.
      Credential oAuth2Credential = new OfflineCredentials.Builder()
              .forApi(Api.ADWORDS)
              .fromFile(configPath)
              .build()
              .generateCredential();
      
      // Construct an AdWordsSession.
      String userAgentFromConfig = getUserAgentFromConfig();
      session = new AdWordsSession.Builder()
          .fromFile(configPath)
          .withUserAgent(PREFIX_USER_AGENT + userAgentFromConfig)
          .withOAuth2Credential(oAuth2Credential)
          .build();

      // Create the services object.
      services = new AdWordsServices();
    } catch (ValidationException e) {
      if ("refreshToken".equalsIgnoreCase(e.getTrigger())) {
        retrieveRefreshToken();
      } else {
        logger.error("General exception", e);
      }

      throw new OAuthException("Please add your refreshToken to your ads.properties file", e);
    }
  }

  /**
   * Retrieves a new refresh token (semi-manual process using the command line).
   * @throws ValidationException in case there is no refresh token
   * @throws ConfigurationLoadException in case of an error loading the config file
   */
  private void retrieveRefreshToken() throws ValidationException, ConfigurationLoadException {
    logger.info("Retrieving a new refresh token");

    
    GoogleClientSecrets clientSecrets = null;
    try {
      clientSecrets = new GoogleClientSecretsBuilder()
              .forApi(com.google.api.ads.common.lib.auth.GoogleClientSecretsBuilder.Api.ADWORDS)
              .fromFile(configPath)
              .build();
    } catch (ValidationException e) {
      logger.error(
          "Please input your client ID and secret into your ads.properties file. If you do not "
          + "have a client ID or secret, please create one in "
          + "the API console: https://code.google.com/apis/console#access", e);
      throw e;
    }
    
    try {
      // Get the OAuth2 credential.
      Credential credential = getOAuth2Credential(clientSecrets);

      System.out.printf("Your refresh token is: %s\n", credential.getRefreshToken());

      // Enter the refresh token into your ads.properties file.
      System.out.printf(
          "In your properties file, modify:\n\napi.adwords.refreshToken=%s\n",
          credential.getRefreshToken());

    } catch (IOException e) {
      logger.error("Exception retrieving the credential", e);
    }
  }

  /**
   * Returns the current AdWords API session.
   */
  public AdWordsSession getSession() {
    return session;
  }

  /**
   * Creates a new specific service using the {@link AdWordsServices}.
   * 
   * @param interfaceClass the interface of the service
   * @return the newly created service
   */
  public <Type> Type getService(Class<Type> interfaceClass) {
    return services.get(session, interfaceClass);
  }

  /**
   * Get client customer ID from the adwords session, and convert it to Long type.
   */
  public Long getClientCustomerId() {
    String accountIdStr = session.getClientCustomerId();
    return accountIdStr == null ? null : Long.valueOf(accountIdStr.replace("-", ""));
  }
}
