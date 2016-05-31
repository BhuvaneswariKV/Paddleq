package com.gopaddle.singlesignon;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.gopaddle.utils.*;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;


public class GoogleAuthorizationSignIn {
	
	static Logger log=Logger.getLogger(GoogleAuthorizationSignIn.class);
	//Invoke the authorize() method to get the Token
	public GoogleAuthorizationSignIn(){
		try {
			authorize();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("",e);
		}
	}
	static ConfigReader con=ConfigReader.getConfig();
 
  /** Global instance of the HTTP transport. */
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final String SCOPE = "https://www.googleapis.com/auth/compute https://www.googleapis.com/auth/cloud-platform https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/compute.readonly https://www.googleapis.com/auth/userinfo.email";

  /** Global instance of the JSON factory. */
  static final JsonFactory JSON_FACTORY = new JacksonFactory();

  private static final String TOKEN_SERVER_URL = "https://accounts.google.com/o/oauth2/token";
  private static final String AUTHORIZATION_SERVER_URL =
      "https://accounts.google.com/o/oauth2/auth";

  /** Authorizes the installed application to access user's protected data. */
  private static void authorize() throws Exception {
    OAuth2ClientCredentials.errorIfNotSpecified();
    AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(BearerToken
        .authorizationHeaderAccessMethod(),
        HTTP_TRANSPORT,
        JSON_FACTORY,
        new GenericUrl(TOKEN_SERVER_URL),
        new ClientParametersAuthentication(
            OAuth2ClientCredentials.API_KEY, OAuth2ClientCredentials.API_SECRET),
        OAuth2ClientCredentials.API_KEY,
        AUTHORIZATION_SERVER_URL).setScopes(Arrays.asList(SCOPE))
        .build();
   
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost(
        OAuth2ClientCredentials.DOMAIN).setPort(OAuth2ClientCredentials.PORT).build();
   
    // open in browser
    String redirectUri = receiver.getRedirectUri();
    AuthorizationCodeRequestUrl authorizationUrl =
    flow.newAuthorizationUrl().setRedirectUri(redirectUri);
    
    // receive authorization code and exchange it for an access token
    new AuthorizationCodeInstalledApp(flow, receiver);
    // Ask user to open in their browser using copy-paste 
    log.info("Please open the following address in your browser:"); 
    log.info("Attempting to open that address in the default browser now..."); 
    
    //genertes the code
     new AuthorizationCodeInstalledApp(flow, receiver);
    AuthorizationCodeInstalledApp.browse(authorizationUrl.build());
    String code = receiver.waitForCode();
    con.Put("##AUTHCODE", code);
   TokenAccess tokenAccess=new TokenAccess();
   tokenAccess.getToken();
   //stop the embedded Jetty Server
   receiver.stop();
      }

public static void main(String[] args) {
    try {
    	authorize();
    } catch (IOException e) {
    	log.error("IO Exception : ",e);
    } catch (Throwable t) {
     log.error("",t);
    }
 }
}
