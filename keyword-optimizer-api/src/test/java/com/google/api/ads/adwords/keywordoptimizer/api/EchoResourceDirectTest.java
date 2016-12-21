package com.google.api.ads.adwords.keywordoptimizer.api;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Basic smoke test for the echo resource, testing it locally. */
@RunWith(JUnit4.class)
public class EchoResourceDirectTest {

  private static EchoResource resource;

  @BeforeClass
  public static void setUp() throws Exception {
    resource = new EchoResource();
  }

  @Test
  public void testDirectEcho() {
    assertDirectEchoEquals("test");
    assertDirectEchoEquals("www.google.com");
  }
  
  private void assertDirectEchoEquals(String request) {
    String response = resource.handleGet(request);
    assertEquals(request, response);
  }
}
