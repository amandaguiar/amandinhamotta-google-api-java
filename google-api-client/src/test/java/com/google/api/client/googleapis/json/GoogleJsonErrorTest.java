/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis.json;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.TestCase;

import java.io.IOException;


/**
 * Tests {@link GoogleJsonError}.
 *
 * @author Yaniv Inbar
 */
public class GoogleJsonErrorTest extends TestCase {

  static final JacksonFactory FACTORY = new JacksonFactory();
  static final String ERROR =
      "{" + "\"code\":403," + "\"errors\":[{" + "\"domain\":\"usageLimits\","
          + "\"message\":\"Access Not Configured\"," + "\"reason\":\"accessNotConfigured\"" + "}],"
          + "\"message\":\"Access Not Configured\"}";
  static final String ERROR_RESPONSE = "{\"error\":" + ERROR + "}";

  public void test_json() throws IOException {
    JsonParser parser = FACTORY.createJsonParser(ERROR);
    parser.nextToken();
    GoogleJsonError e = parser.parse(GoogleJsonError.class, null);
    assertEquals(ERROR, FACTORY.toString(e));
  }

  static class ErrorTransport extends MockHttpTransport {
    final String content;
    final String contentType;

    ErrorTransport() {
      this(ERROR_RESPONSE, Json.CONTENT_TYPE);
    }

    ErrorTransport(String content, String contentType) {
      this.content = content;
      this.contentType = contentType;
    }

    @Override
    public LowLevelHttpRequest buildGetRequest(String url) {
      return new MockLowLevelHttpRequest() {
        @Override
        public LowLevelHttpResponse execute() {
          MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
          if (content != null) {
            result.setContent(content);
          }
          if (contentType != null) {
            result.setContentType(contentType);
          }
          result.setStatusCode(HttpStatusCodes.STATUS_CODE_FORBIDDEN);
          return result;
        }
      };
    }
  }

  public void testParse() throws IOException {
    HttpTransport transport = new ErrorTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    try {
      request.execute();
      fail();
    } catch (HttpResponseException e) {
      GoogleJsonError errorResponse = GoogleJsonError.parse(FACTORY, e.getResponse());
      assertEquals(ERROR, FACTORY.toString(errorResponse));
    }
  }
}
