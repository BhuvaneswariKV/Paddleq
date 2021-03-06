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

package com.gopaddle.singlesignon;


public class OAuth2ClientCredentials {

  /** Value of the "API Key". */
  public static final String API_KEY = "933144605699-jj5cv9u698vrng80656oknor4eh281e4.apps.googleusercontent.com";

  /** Value of the "API Secret". */
  public static final String API_SECRET = "rMkXB6ZdyrYILKM9CE-KLqeb";

  /** Port in the "Callback URL". */
  public static final int PORT = 8081;

  /** Domain name in the "Callback URL". */
  public static final String DOMAIN = "127.0.0.1";
  //if client email id and secret key entered as invalid it shown the error Message in console.
  public static void errorIfNotSpecified() {
    if (API_KEY.startsWith("Enter ") || API_SECRET.startsWith("Enter ")) {
      System.out.println(
          "Enter API Key and API Secret from http://www.dailymotion.com/profile/developer"
          + " into API_KEY and API_SECRET in " + OAuth2ClientCredentials.class);
      System.exit(1);
    }
  }
}
