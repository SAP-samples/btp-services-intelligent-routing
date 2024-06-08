package com.sap.region.manager.config;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>CommonConfig class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
@Configuration
public class CommonConfig {

    // Fetching the CI ConfigurationProperties from application.yaml
    @Bean
    @ConfigurationProperties(prefix = "spring.cloudintegration.primary")
    public Properties ciPrimaryProperties() {
        return new Properties();
    }
    
    @Bean
    @ConfigurationProperties(prefix = "spring.cloudintegration.secondary")
    public Properties ciSecondaryProperties() {
        return new Properties();
    }  
    @Bean
    @ConfigurationProperties(prefix = "spring.aem.primary")
    public Properties aemPrimaryProperties() {
        return new Properties();
    }
    
    @Bean
    @ConfigurationProperties(prefix = "spring.aem.secondary")
    public Properties aemSecondaryProperties() {
        return new Properties();
    } 
    @Bean
    @ConfigurationProperties(prefix = "spring.azure")
    public Properties azureProperties() {
        return new Properties();
    }  
    
    @Bean
    @ConfigurationProperties(prefix = "spring.regions")
    public Properties regionProperties() {
        return new Properties();
    } 
   
    @Bean
    public Map<String, Properties> ciPropertiesMap(@Qualifier("ciPrimaryProperties") Properties ciPrimaryProperties, @Qualifier("ciSecondaryProperties") Properties ciSecondaryProperties) {
        Map<String, Properties> propertiesMap = new ConcurrentHashMap<>();
        propertiesMap.put("primary",ciPrimaryProperties);
        propertiesMap.put("secondary",ciSecondaryProperties);
        return propertiesMap;       
    }   

    @Bean
    public Map<String, Properties> aemPropertiesMap(@Qualifier("aemPrimaryProperties") Properties aemPrimaryProperties, @Qualifier("aemSecondaryProperties") Properties aemSecondaryProperties) {
        Map<String, Properties> propertiesMap = new ConcurrentHashMap<>();
        propertiesMap.put("primary",aemPrimaryProperties);
        propertiesMap.put("secondary",aemSecondaryProperties);
        return propertiesMap;       
    }
     
}
