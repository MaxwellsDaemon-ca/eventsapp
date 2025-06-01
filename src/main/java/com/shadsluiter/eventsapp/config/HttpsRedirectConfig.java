package com.shadsluiter.eventsapp.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable both HTTP and HTTPS connectors.
 * 
 * This class adds an additional HTTP connector (port 8080) alongside the default HTTPS connector.
 * HTTP traffic will be allowed to reach the application, which can then be redirected to HTTPS
 * using Spring Security's requiresSecure() configuration.
 * 
 * NOTE: This configuration alone does not enforce HTTPS redirect — it simply allows HTTP access.
 * Proper redirect logic should be handled separately in Spring Security.
 */
@Configuration
public class HttpsRedirectConfig {

    /**
     * Creates the embedded Tomcat servlet container with an additional HTTP connector.
     * 
     * @return customized TomcatServletWebServerFactory with dual connector support.
     */
    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(httpToHttpsRedirectConnector());
        return tomcat;
    }

    /**
     * Defines the additional HTTP connector on port 8080.
     * 
     * The redirectPort parameter is required for Tomcat compatibility, but actual redirect
     * enforcement must be handled by Spring Security configuration.
     * 
     * @return HTTP connector configuration.
     */
    private Connector httpToHttpsRedirectConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }
}
