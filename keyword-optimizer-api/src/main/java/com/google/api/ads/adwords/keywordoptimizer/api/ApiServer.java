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

import com.google.api.ads.adwords.keywordoptimizer.KeywordOptimizerException;
import com.google.api.client.util.Strings;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import javax.annotation.Nullable;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for starting the keyword optimizer as a REST/HTTP API service. This class is a wrapper
 * around a standard Jetty server that is pre-configured to use the Jersey servlet with the 
 * keyword optimizer-specific resources. 
 */
@SuppressWarnings("deprecation")
public class ApiServer {
  
  public static final int DEFAULT_PORT = 8080;
  public static final String DEFAULT_CONTEXT_PATH = "/keyword-optimizer";
  private static final String INIT_PARAM_PACKAGES = "com.sun.jersey.config.property.packages";
  public static final String INIT_PARAM_ADS_PROPERTIES =
      "com.google.api.ads.adwords.keywordoptimizer.api.ads_properties";
  public static final String INIT_PARAM_PROPERTIES =
      "com.google.api.ads.adwords.keywordoptimizer.api.properties";
  
  private static final String HOST = "localhost";
  private static final String SERVLET_NAME = "Jersey REST Service";
  private static final String SERVLET_PATH = "/api/*";
  
  private static final Logger logger = LoggerFactory.getLogger(ApiServer.class);
  
  private final int port;
  private final String contextPath;
  
  private final String propertiesPath;
  private final String adsPropertiesPath;
  
  private Server server;
  
  /** Creates a new REST API server. */
  public ApiServer(
      int port,
      String contextPath,
      @Nullable String propertiesPath,
      @Nullable String adsPropertiesPath) {
    this.port = port;
    this.contextPath = contextPath;
    this.propertiesPath = propertiesPath;
    this.adsPropertiesPath = adsPropertiesPath;
  }
  
  /**
   * Starts the server, which will listen to the given path going forward.
   * @throws Exception
   */
  public void start() throws Exception {
    logger.info("Starting API server on {}:{}{}", HOST, port, contextPath);
    
    server = new Server();
    server.addListener(String.format("%s:%d", HOST, port));
    
    ServletHttpContext context = (ServletHttpContext) server.getContext(contextPath);
    
    context.addServlet(SERVLET_NAME, SERVLET_PATH, ServletContainer.class.getName());
    context.getServletHandler().getServletHolder(SERVLET_NAME)
        .setInitParameter(INIT_PARAM_PACKAGES, getClass().getPackage().getName());
    
    if (!Strings.isNullOrEmpty(adsPropertiesPath)) {
      context.getServletHandler().getServletHolder(SERVLET_NAME)
          .setInitParameter(INIT_PARAM_ADS_PROPERTIES, adsPropertiesPath);
    }
    if (!Strings.isNullOrEmpty(propertiesPath)) {
      context.getServletHandler().getServletHolder(SERVLET_NAME)
          .setInitParameter(INIT_PARAM_PROPERTIES, propertiesPath);
    }
    
    server.start();
  }
  
  /**
   * Stops the server (if running) and suppresses all exceptions during the process.
   */
  public void stop() {
    // If the server is not running, simply return.
    if (server == null) {
      return;
    }
    
    try {
      server.stop(true);
    } catch (Exception e) {
      logger.error("Error stopping API server", e);
    } finally {
      server = null;
    }
  }
  
  /**
   * Creates the command line structure / options.
   *
   * @return the command line {@link Options}
   */
  private static Options createCommandLineOptions() {
    Options options = new Options();
    
    OptionBuilder.withLongOpt("keyword-properties");
    OptionBuilder.withDescription("Location of the keyword-optimizer.properties file.");
    OptionBuilder.hasArg(true);
    OptionBuilder.withArgName("file");
    options.addOption(OptionBuilder.create("kp"));

    OptionBuilder.withLongOpt("ads-properties");
    OptionBuilder.withDescription("Location of the ads.properties file.");
    OptionBuilder.hasArg(true);
    OptionBuilder.withArgName("file");
    options.addOption(OptionBuilder.create("ap"));

    OptionBuilder.withLongOpt("port");
    OptionBuilder.withDescription("Port for the REST HTTP server.");
    OptionBuilder.hasArg(true);
    OptionBuilder.withArgName("port");
    options.addOption(OptionBuilder.create("p"));
    
    OptionBuilder.withLongOpt("context-path");
    OptionBuilder.withDescription("Context path for the REST HTTP server.");
    OptionBuilder.hasArg(true);
    OptionBuilder.withArgName("path");
    options.addOption(OptionBuilder.create("cp"));
    
    return options;
  }
  
  public static void main(String[] args) throws Exception {
    Options options = createCommandLineOptions();
    
    CommandLineParser parser = new BasicParser();
    CommandLine cmdLine = null;
    try {
      cmdLine = parser.parse(options, args);
    } catch (ParseException e) {
      throw new KeywordOptimizerException("Error parsing command line parameters", e);
    }

    int port = Integer.parseInt(cmdLine.getOptionValue("port", DEFAULT_PORT + ""));
    String contextPath = cmdLine.getOptionValue("context-path", DEFAULT_CONTEXT_PATH);
    
    ApiServer server = new ApiServer(port, contextPath, 
        cmdLine.getOptionValue("kp", ""), cmdLine.getOptionValue("ap", ""));
    server.start();
  }
  
}
