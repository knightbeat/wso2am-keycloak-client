/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * @author ck
 */
@Component
@ConfigurationProperties("wso2-apim")
public class WSO2APIManager {
    
    private String host;
    private String oAuthAppClientKey;
    private String oAuthAppClientSecret;
    private String adminPort;
    private String servicePort;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getoAuthAppClientKey() {
        return oAuthAppClientKey;
    }

    public void setoAuthAppClientKey(String oAuthAppClientKey) {
        this.oAuthAppClientKey = oAuthAppClientKey;
    }

    public String getoAuthAppClientSecret() {
        return oAuthAppClientSecret;
    }

    public void setoAuthAppClientSecret(String oAuthAppClientSecret) {
        this.oAuthAppClientSecret = oAuthAppClientSecret;
    }   

    public String getAdminPort() {
        return adminPort;
    }

    public void setAdminPort(String adminPort) {
        this.adminPort = adminPort;
    }

    public String getServicePort() {
        return servicePort;
    }

    public void setServicePort(String servicePort) {
        this.servicePort = servicePort;
    }
    
    public String getTokenEndpoint(){
        return "https://" + this.getHost() + ":" + this.getAdminPort() +"/oauth2/token";
    }
    
    public String getGatewayEndpointForRESTResource(String resource){
        return "https://" + this.getHost() + ":" + this.getServicePort()+resource;
    }
}
