/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.googleapis.extensions.appengine.auth.oauth2;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * OAuth 2.0 credential in which a client Google App Engine application needs to access data that it
 * owns, based on <a href="https://developers.google.com/appengine/docs/java/appidentity/
 * #Java_Asserting_identity_to_Google_APIs">Asserting Identity to Google APIs</a>
 *
 * <p>
 * Intercepts the request by using the access token obtained from
 * {@link AppIdentityService#getAccessToken(Iterable)}.
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactory(
      HttpTransport transport, JsonFactory jsonFactory, TokenResponse tokenResponse) {
    return transport.createRequestFactory(
        new AppIdentityCredential("https://www.googleapis.com/auth/urlshortener"));
  }
 * </pre>
 *
 * <p>
 * Implementation is immutable and thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class AppIdentityCredential implements HttpRequestInitializer, HttpExecuteInterceptor {

  /** App Identity Service that provides the access token. */
  private final AppIdentityService appIdentityService;

  /** OAuth scopes (unmodifiable). */
  private final Collection<String> scopes;

  /**
   * @param scopes OAuth scopes
   * @since 1.15
   */
  public AppIdentityCredential(Collection<String> scopes) {
    this(new Builder(scopes));
  }

  /**
   * @param builder builder
   *
   * @since 1.14
   */
  protected AppIdentityCredential(Builder builder) {
    // Lazily retrieved rather than setting as the default value in order to not add runtime
    // dependencies on AppIdentityServiceFactory unless it is actually being used.
    appIdentityService = builder.appIdentityService == null
        ? AppIdentityServiceFactory.getAppIdentityService() : builder.appIdentityService;
    scopes = builder.scopes;
  }

  @Override
  public void initialize(HttpRequest request) throws IOException {
    request.setInterceptor(this);
  }

  @Override
  public void intercept(HttpRequest request) throws IOException {
    String accessToken = appIdentityService.getAccessToken(scopes).getAccessToken();
    BearerToken.authorizationHeaderAccessMethod().intercept(request, accessToken);
  }

  /**
   * Gets the App Identity Service that provides the access token.
   *
   * @since 1.12
   */
  public final AppIdentityService getAppIdentityService() {
    return appIdentityService;
  }

  /**
   * Gets the OAuth scopes (unmodifiable).
   *
   * @since 1.12
   */
  public final Collection<String> getScopes() {
    return scopes;
  }

  /**
   * Builder for {@link AppIdentityCredential}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.12
   */
  public static class Builder {

    /**
     * App Identity Service that provides the access token or {@code null} to use
     * {@link AppIdentityServiceFactory#getAppIdentityService()}.
     */
    AppIdentityService appIdentityService;

    /** OAuth scopes (unmodifiable). */
    final Collection<String> scopes;

    /**
     * Returns an instance of a new builder.
     *
     * @param scopes OAuth scopes
     * @since 1.15
     */
    public Builder(Collection<String> scopes) {
      this.scopes = Collections.unmodifiableCollection(scopes);
    }

    /**
     * Returns the App Identity Service that provides the access token or {@code null} to use
     * {@link AppIdentityServiceFactory#getAppIdentityService()}.
     *
     * @since 1.14
     */
    public final AppIdentityService getAppIdentityService() {
      return appIdentityService;
    }

    /**
     * Sets the App Identity Service that provides the access token or {@code null} to use
     * {@link AppIdentityServiceFactory#getAppIdentityService()}.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setAppIdentityService(AppIdentityService appIdentityService) {
      this.appIdentityService = appIdentityService;
      return this;
    }

    /**
     * Returns a new {@link AppIdentityCredential}.
     */
    public AppIdentityCredential build() {
      return new AppIdentityCredential(this);
    }

    /**
     * Returns the OAuth scopes (unmodifiable).
     *
     * @since 1.14
     */
    public final Collection<String> getScopes() {
      return scopes;
    }
  }
}
