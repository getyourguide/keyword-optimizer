# KeywordOptimizer (Beta)

## Special note

If you are using this project, please follow the [API anouncements](https://groups.google.com/forum/#!forum/adwordsapi-announcements)
and [API version sunsets](https://developers.google.com/adwords/api/docs/sunset-dates).
The AdWords API changes frequently; current versions are being sunset about
6-8 months after their introduction. Please make sure to upgrade your project
around that timeframe.

Please keep in mind: This tool uses the [TargetingIdeaService](https://developers.google.com/adwords/api/docs/guides/targeting-idea-service)
and [TrafficEstimatorService](https://developers.google.com/adwords/api/docs/guides/traffic-estimator-service)
of the AdWords API. If you expose this tool directly to your clients, [the
complete set of required minimum functionality might apply to your application](https://developers.google.com/adwords/api/docs/requirements#requirements-for-api-clients-providing-targeting-idea-service-or-trafficestimation-service).

This tool provides estimations only, the real performance of your ads might
differ substantially. Please make sure you understand the [limitations of our
keyword planning tools](https://support.google.com/adwords/answer/3022575).

Please let us know if you run into issues in the [project's issue tracker](https://github.com/googleads/keyword-optimizer/issues).
This beta release may not fit your needs for production environments. We are
constantly working on improving the project; your feedback is very important
to us.

## Overview
KeywordOptimizer is an open-source tool for finding optimized sets of keywords
for AdWords, implemented in Java. The goals of this project are:

* Provide education and guidance on using the AdWords API services for [keyword suggestion](https://developers.google.com/adwords/api/docs/guides/targeting-idea-service)
and [traffic estimation](https://developers.google.com/adwords/api/docs/guides/traffic-estimator-service).
* Allow non-technical users to run searches for optimized sets of keywords from
the command line.
* Provide an open-source framework for keyword optimization for advanced users,
which can be extended with custom implementations.

The tool implements an evolutionary process for keyword optimization, inspired
by the theory of [Darwinism](https://en.wikipedia.org/wiki/Darwinism):

1. Create a set of seed keywords using a variety of parameters (such as URL,
business category, location).
2. Estimate traffic and derive a score for each keyword.
3. Use the keywords with the highest score to generate similar or alternative
keywords. Remove the keywords with the lowest scores from the set.
4. Repeat this process for a number of steps or until there is no further
improvement. By repeatedly "reproducing" the best and removing the worst
keywords, the overall quality of the keyword set is likely to increase.

**Note:** This tool uses the [TargetingIdeaService](https://developers.google.com/adwords/api/docs/reference/latest/TargetingIdeaService)
/ [TrafficEstimatorService](https://developers.google.com/adwords/api/docs/reference/latest/TrafficEstimatorService)
of the AdWords API. As these services [return dummy data for test accounts](https://developers.google.com/adwords/api/docs/test-accounts#differences_between_test_accounts_and_production_accounts),
this tool will not work as intended for test accounts either.

## Structure
The code is organized in three separate Maven projects:
* **keyword-optimizer-core** contains the core library and code necessary to
run the tool from the command line. Previously, all code was bundled here.
* **keyword-optimizer-api** contains a REST API on top of the core project. The
intention of this API is to allow developers to use this tool with programming
languages other than Java.
* **keyword-optimizer** is the parent project that encompasses both projects
above as modules.

## Quick start

### Prerequisites

You will need Java and Maven installed before configuring the project.

### Build the project using Maven

```$ git clone https://github.com/googleads/keyword-optimizer```

```$ mvn clean install eclipse:eclipse```

```$ mvn compile dependency:copy-dependencies package```

### Import the project into Eclipse (optional)

To import the project into Eclipse, perform the following steps.

> File -> Import -> Maven -> Existing Maven Projects -> (select the project
folder).

### Configure KeywordOptimizer

The tool needs 2 configuration files (their names can be configured):

* `ads.properties`: Parameters for interaction with the AdWords API.
* `keyword-optimizer.properties`: Parameters for the optimization process.

#### ads.properties

This tool simply passes the [ads.properties](https://github.com/googleads/googleads-java-lib/blob/master/examples/adwords_axis/src/main/resources/ads.properties)
file to the AdWords API Java Client library. If you're already using the AdWords
API, you can reuse your existing configuration. If you're new to the API, you
can [follow our guide](https://developers.google.com/adwords/api/docs/guides/start)
to get started.

#### keyword-optimizer.properties

The tool-specific configuration file specifies which classes ([strategy pattern](https://en.wikipedia.org/wiki/Strategy_pattern))
and parameters are used during various stages of the process. If you're using
this tool off-the-shelf, you probably don't need to worry about it, and can use
the defaults we provide. If you are extending this project, you can plug in your
own implementations here.

This is our default configuration:

    # Properties for KeywordOptimizer

    # Class used for finding keyword alternatives, has to implement com.google.api.ads.adwords.keywordoptimizer.AlternativesFinder.
    optimizer.alternativesFinder = com.google.api.ads.adwords.keywordoptimizer.TisAlternativesFinder

    # Class used for estimating keyword traffic, has to implement com.google.api.ads.adwords.keywordoptimizer.TrafficEstimator.
    optimizer.estimator = com.google.api.ads.adwords.keywordoptimizer.TesEstimator

    # Class used for calculating keyword scores, has to implement com.google.api.ads.adwords.keywordoptimizer.ScoreCalculator.
    optimizer.scoreCalculator = com.google.api.ads.adwords.keywordoptimizer.ClicksScoreCalculator

    # Class used for defining the round-based strategy, has to implement com.google.api.ads.adwords.keywordoptimizer.RoundStrategy.
    optimizer.roundStrategy = com.google.api.ads.adwords.keywordoptimizer.DefaultRoundStrategy

    # Maximum number of rounds
    optimizer.roundStrategy.maxSteps = 3
    # Minimum average score improvement per round (0 for no restriction)
    optimizer.roundStrategy.minImprovement = 0
    # Maximum size for the keyword population
    optimizer.roundStrategy.maxPopulation = 100
    # Number of best keywords to use for replication in each round
    optimizer.roundStrategy.replicateBest = 5

### Run KeywordOptimizer

You can run the tool using the following command. Be sure to specify the path
to the properties files above using the `-ap` and `-kp` parameters.

```
$ java -jar target/keyword-optimizer.jar -ap src/main/resources/ads.properties \
-kp src/main/resources/keyword-optimizer.properties
```

#### Running with Maven
Alternatively, you can run the tool using Maven as follows.

```
$ mvn exec:java -Dexec.mainClass="com.google.api.ads.adwords.keywordoptimizer.KeywordOptimizer" \
-Dexec.args="-ap keyword-optimizer-core/src/main/resources/ads.properties -kp keyword-optimizer-core/src/main/resources/keyword-optimizer.properties"
```

#### Running in Eclipse
You can also run the tool from Eclipse by starting the main class
`com.google.api.ads.adwords.keywordoptimizer.KeywordOptimizer`.

#### Running the REST API server
Maven is configured to bundle the components needed for the API server into a
war file (`keyword-optimizer-api/target/keyword-optimizer-api.war`). You can
deploy this war file on all common Java application servers to run the API.
Don't forget to customize the property files before running the build process
(see above). 

Alternatively, we included a standalone server based on
[Jetty](https://en.wikipedia.org/wiki/Jetty), which you can run by starting
`com.google.api.ads.adwords.keywordoptimizer.api.ApiServer`. It expects the same
arguments as the command-line version for the properties files (see below). In
addition, you can optionally specify a port and context path for the HTTP
server.

If you use the default parameters, you can use the REST API via the following
URLs:
* **http://localhost:8080/keyword-optimizer/api/optimize/url?url=<url>:**
Uses a given URL to generate seed keywords (replace **<url>** with the actual
URL). Equivalent to the `-su` command-line parameter.
* **http://localhost:8080/keyword-optimizer/api/optimize/category?category=<category>:**
Uses a given URL to generate seed keywords (replace **<category>** with the
actual category ID). Equivalent to  the `-sc` command-line parameter.
* **http://localhost:8080/keyword-optimizer/api/optimize/term?term=<term>:**
Uses a set of search terms to generate seed keywords (replace **<term>** with
the search term / query. There can be multiple **<term>** parameters). 
Equivalent to  the `-st` command-line parameter.
* **http://localhost:8080/keyword-optimizer/api/optimize/keyword?keyword=<keyword>:**
Uses a set of keywords directly as a seed (replace **<keyword>** with
the search term / query. There can be multiple **<keyword>** parameters).
Equivalent to  the `-sk` command-line parameter.

In addition, use the following URL parameters to specify further options:
* **cpc:** Max. CPC setting, equivalent to the `-cpc` command-line parameter.
* **m:** Keyword match type, equivalent to the `-m` command-line parameter.
* **lang:** Language, equivalent to the `-lang` command-line parameter.
* **loc:** Location, equivalent to the `-loc` command-line parameter.

The results of each call are returned as JSON objects.

### Command line options

You can specify the following command line options when running the tool.
Running the tool without parameters will print this help screen.
```
usage: keyword-optimizer
 -h,--help                          Shows this help screen.
 -ap,--ads-properties <file>        Location of the ads.properties file.
 -cpc,--max-cpc <double>            Use the given maximum CPC (in USD, e.g., 5.0
                                    for $5).
 -kp,--keyword-properties <file>    Location of the keyword-optimizer.properties
                                    file.
 -lang,--languages <ids>            Use the given locations IDs (ID as defined @
                                    https://goo.gl/WWzifs) for language-targeted
                                    results.
 -loc,--locations <ids>             Use the given locations IDs (ID as defined @
                                    https://goo.gl/TA5E81) for geo-targeted
                                    results.
 -m,--match-types <types>           Use the given keyword match types (EXACT,
                                    BROAD, PHRASE).
 -o,--output <mode>                 Mode for outputting results (CONSOLE / CSV)
                                    Note: If set to CSV, then option -of also
                                    has to be specified.
 -of,--output-file <file>           File to for writing output data (only needed
                                    if option -o is specified).
 -sc,--seed-category <id>           Use the given category (ID as defined @
                                    https://goo.gl/xUEr6s) to get keywords as a
                                    seed for the optimization.
                                    Note: Only one seed-* option is allowed.
 -sk,--seed-keywords <keywords>     Use the given keywords (separated by spaces)
                                    as a seed for the optimization.
                                    Note: Only one seed-* option is allowed.
 -skf,--seed-keywords-file <file>   Use the keywords from the given file (one
                                    keyword per row) as a seed for the
                                    optimization.
                                    Note: Only one seed-* option is allowed.
 -st,--seed-terms <terms>           Use the given search terms (separated by
                                    spaces) as a seed for the optimization.
                                    Note: Only one seed-* option is allowed.
 -stf,--seed-terms-file <file>      Use the search terms from the given file
                                    (one keyword per row) as a seed for the
                                    optimization.
                                    Note: Only one seed-* option is allowed.
 -su,--seed-urls <urls>             Use the given urls (separated by spaces) to
                                    extract keywords as a seed for the
                                    optimization.
                                    Note: Only one seed-* option is allowed.
 -suf,--seed-urls-file <file>       Use the urls from the given file (one url
                                    per row) to extract keywords as a seed for
                                    the optimization.
                                    Note: Only one seed-* option is allowed.
```

## Fine print
Pull requests are very much appreciated. Please sign the [Google Individual Contributor License Agreement](http://code.google.com/legal/individual-cla-v1.0.html)
(there is a convenient online form) before submitting.

<dl>
  <dt>Authors</dt><dd><a href="https://plus.google.com/116541501935961193199">Timo Bozsolik (Google Inc.)</a>
  <dt>Copyright</dt><dd>Copyright © 2016 Google, Inc.</dd>
  <dt>License</dt><dd>Apache 2.0</dd>
  <dt>Limitations</dt><dd>This is example software, use with caution at your own risk.</dd>
</dl>

