package com.sap.region.manager.steps.aem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Base64;
import java.util.Properties;

/**
 * <p>Helper class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
public class Helper {
    private static final Logger logger = LoggerFactory.getLogger(Helper.class);

    public static final String getBasicAuthenticationHeader(Properties properties) {
        String valueToEncode = properties.getProperty("username") + ":" + properties.getProperty("password");
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

}
