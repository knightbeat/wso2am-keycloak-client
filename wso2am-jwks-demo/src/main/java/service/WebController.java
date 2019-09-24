/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Base64;
import javax.servlet.http.HttpSession;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class WebController {
    
    @Autowired
    WSO2APIManager wso2apim;

    String CONTENT_TYPE = "application/x-www-form-urlencoded";
    String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    String BACKEND_API = "/pet-store/1.0.2/pet/8";

    static {
        //System.setProperty("javax.net.ssl.trustStore", "/Users/ck/demos/wso2am-2.6.0/repository/resources/security/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStore", "/Users/ck/demos/keycloak-eval/cert-work/springclient.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
    }

    @GetMapping(path = "/")
    public String index(Principal principal, Model model) {
        model.addAttribute("clientKey", wso2apim.getoAuthAppClientKey());
        model.addAttribute("clientSecret", wso2apim.getoAuthAppClientSecret());
        model.addAttribute("tokenEndpoint", wso2apim.getTokenEndpoint());
        model.addAttribute("invokesAPI", wso2apim.getGatewayEndpointForRESTResource(BACKEND_API));
        return "index";
    }

    @GetMapping(path = "/logout")
    public String logout(HttpSession session, Principal principal, Model model) {
        session.invalidate();
        model.addAttribute("clientKey", wso2apim.getoAuthAppClientKey());
        model.addAttribute("clientSecret", wso2apim.getoAuthAppClientSecret());
        model.addAttribute("tokenEndpoint", wso2apim.getTokenEndpoint());
        model.addAttribute("invokesAPI", wso2apim.getGatewayEndpointForRESTResource(BACKEND_API));
        return "index";
    }

    @GetMapping(path = "/home")
    public String home(Principal principal, Model model) throws URISyntaxException, IOException {

        System.out.println("Redirection --> /home");
        String authorizationHeader = wso2apim.getoAuthAppClientKey() + ":" + wso2apim.getoAuthAppClientSecret();
        String encodedAuthorizationHeader = Base64.getEncoder().encodeToString(authorizationHeader.getBytes());

        KeycloakAuthenticationToken principalKeyCloak = (KeycloakAuthenticationToken) principal;
        String idTokenString = principalKeyCloak.getAccount().getKeycloakSecurityContext().getIdTokenString();
        System.out.println("--------------ID TOKEN START-----------------");
        System.out.println(idTokenString);
        System.out.println("--------------ID TOKEN END-------------------");
        
        TokenEndpointResponseObject tokenEndpointResponseObject = invokeTokenAPI(idTokenString,encodedAuthorizationHeader);
        String cURLCommandTokenAPI = getCURLCommandTokenAPI(idTokenString, encodedAuthorizationHeader);

        String oauthToken = tokenEndpointResponseObject.getAccess_token();

        String finalResponse = invokeBackendAPI(oauthToken);

        model.addAttribute("username", principal.getName());
        model.addAttribute("id_token", idTokenString);
        model.addAttribute("cURLCommandTokenAPI", cURLCommandTokenAPI);
        model.addAttribute("oauth_token", oauthToken);
        model.addAttribute("cURLBackendAPI", getCURLBackendAPI(oauthToken));
        model.addAttribute("finalResponse", finalResponse);

        return "home";
    }

    private String getCURLCommandTokenAPI(String jwToken, String encodedAuthorizationHeader) {

        String cURLCommand = "curl -k -d 'grant_type=" + GRANT_TYPE + "&assertion=" + jwToken + "&scope=openid' -H 'Authorization: Basic " + encodedAuthorizationHeader + "' -H 'Content-Type:" + CONTENT_TYPE + "' " + wso2apim.getTokenEndpoint();
        return cURLCommand;
    }

    private String getCURLBackendAPI(String oauthToken) {

        String cURLCommand = "curl -X GET --header 'Accept: application/json' --header 'Authorization: Bearer " + oauthToken + "' '" + wso2apim.getGatewayEndpointForRESTResource(BACKEND_API) + "' -k";
        return cURLCommand;
    }
    
    private TokenEndpointResponseObject invokeTokenAPI(String idTokenString, String encodedAuthorizationHeader) throws IOException{
        
        String baseUrl = wso2apim.getTokenEndpoint() + "?grant_type=" + GRANT_TYPE + "&assertion=" + idTokenString + "&scope=openid";

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(baseUrl);

        httpPost.addHeader("Content-Type", CONTENT_TYPE);
        httpPost.addHeader("Authorization", "Basic " + encodedAuthorizationHeader);
        HttpResponse response = httpclient.execute(httpPost);

        org.apache.http.HttpEntity entity = response.getEntity();
        String tokenResponse = EntityUtils.toString(entity, "UTF-8");
        System.out.println("--------------OAUTH TOKEN START-----------------");
        System.out.println(tokenResponse);
        System.out.println("--------------OAUTH TOKEN END-------------------");
        ObjectMapper objectMapper = new ObjectMapper();
        TokenEndpointResponseObject tokenEndpointResponseObject = objectMapper.readValue(tokenResponse, TokenEndpointResponseObject.class);
        
        return tokenEndpointResponseObject;
    }

    private String invokeBackendAPI(String oauthToken) throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet(wso2apim.getGatewayEndpointForRESTResource(BACKEND_API));

        httpGet.addHeader("Accept", "application/json");
        httpGet.addHeader("Authorization", "Bearer " + oauthToken);

        HttpResponse response = httpclient.execute(httpGet);

        org.apache.http.HttpEntity entity = response.getEntity();
        String backendAPIesponse = EntityUtils.toString(entity, "UTF-8");

        return backendAPIesponse;
    }

}
