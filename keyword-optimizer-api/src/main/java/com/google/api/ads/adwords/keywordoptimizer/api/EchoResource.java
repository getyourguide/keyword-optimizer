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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * This resource is for smoke testing only.
 */
@Path("/echo")
public class EchoResource {
  
  /**
   * This method takes a parameter and simply returns the same string back as an echo.
   *
   * @param param a URL parameter
   * @return the given parameter as an echo
   */
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("{param : .+}")
  public String handleGet(@PathParam("param") String param) {
    return param;
  }
}
