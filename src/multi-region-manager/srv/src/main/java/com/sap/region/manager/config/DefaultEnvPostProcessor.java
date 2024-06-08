package com.sap.region.manager.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.StringUtils;

/**
 * <p>DefaultEnvPostProcessor class.</p>
 *
 * @author shanthakumar.krishnaswamy@sap.com
 */
public class DefaultEnvPostProcessor
		implements EnvironmentPostProcessor, Ordered, ApplicationListener<ApplicationPreparedEvent> {
	private static final String VCAP_SERVICES = "VCAP_SERVICES";

	private final Log logger;

	private final boolean switchableLogger;

	/**
	 * Create a new {@link DefaultEnvPostProcessor} instance.
	 *
	 * @deprecated since 2.4.0 in favor of
	 *             {@link #VcapEnvironmentPostProcessor(Log)}
	 */
	@Deprecated
	public DefaultEnvPostProcessor() {
		this.logger = new DeferredLog();
		this.switchableLogger = true;
	}

	/**
	 * Create a new {@link DefaultEnvPostProcessor} instance.
	 *
	 * @param logger the logger to use
	 */
	public DefaultEnvPostProcessor(Log logger) {
		this.logger = logger;
		this.switchableLogger = false;
	}

	@Override
	public int getOrder() {
		return 1;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		// InputStream stream =
		// Thread.currentThread().getContextClassLoader().getResourceAsStream("default-env.json");
		try {

			File defaultEnvFile = new File("default-env.json");
			if (defaultEnvFile.exists()) {
				InputStream defaultEnvStream = new FileInputStream(defaultEnvFile);
				String defaultEnvString = null;
				if (defaultEnvStream != null) {
					try (Scanner scanner = new Scanner(defaultEnvStream, StandardCharsets.UTF_8.name())) {
						defaultEnvString = scanner.useDelimiter("\\A").next();
					}
				}

				Properties properties = new Properties();
				JsonParser jsonParser = JsonParserFactory.getJsonParser();
				addWithPrefix(properties, getPropertiesFromServices(environment, jsonParser, defaultEnvString),
						"vcap.services.");
				MutablePropertySources propertySources = environment.getPropertySources();
				if (propertySources.contains(CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME)) {
					propertySources.addAfter(CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME,
							new PropertiesPropertySource("vcap", properties));
				} else {
					propertySources.addFirst(new PropertiesPropertySource("vcap", properties));
				}
			}
		} catch (Exception ex) {

		}
	}

	/**
	 * Event listener used to switch logging.
	 *
	 * @deprecated since 2.4.0 in favor of only using
	 *             {@link EnvironmentPostProcessor} callbacks
	 */
	@Deprecated
	@Override
	public void onApplicationEvent(ApplicationPreparedEvent event) {
		if (this.switchableLogger) {
			((DeferredLog) this.logger).switchTo(DefaultEnvPostProcessor.class);
		}
	}

	private void addWithPrefix(Properties properties, Properties other, String prefix) {
		for (String key : other.stringPropertyNames()) {
			String prefixed = prefix + key;
			properties.setProperty(prefixed, other.getProperty(key));
		}
	}

	private Properties getPropertiesFromServices(Environment environment, JsonParser parser, String defaultEnvString) {
		Properties properties = new Properties();
		try {
			Map<String, Object> map = parser.parseMap(defaultEnvString);
			extractPropertiesFromServices(properties, map);
		} catch (Exception ex) {
			this.logger.error("Could not parse VCAP_SERVICES", ex);
		}
		return properties;
	}

	private void extractPropertiesFromServices(Properties properties, Map<String, Object> map) {
		if (map != null) {
			for (Object servicesObjMap : map.values()) {
				@SuppressWarnings("unchecked")
				Map<String, Object> servicesMap = (Map<String, Object>) servicesObjMap;
				for (Object servicesObj : servicesMap.values()) {
					@SuppressWarnings("unchecked")
					List<Object> list = (List<Object>) servicesObj;
					for (Object object : list) {
						@SuppressWarnings("unchecked")
						Map<String, Object> service = (Map<String, Object>) object;
						String key = (String) service.get("name");
						if (key == null) {
							key = (String) service.get("label");
						}
						flatten(properties, service, key);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void flatten(Properties properties, Map<String, Object> input, String path) {
		input.forEach((key, value) -> {
			String name = getPropertyName(path, key);
			if (value instanceof Map) {
				// Need a compound key
				flatten(properties, (Map<String, Object>) value, name);
			} else if (value instanceof Collection) {
				// Need a compound key
				Collection<Object> collection = (Collection<Object>) value;
				properties.put(name, StringUtils.collectionToCommaDelimitedString(collection));
				int count = 0;
				for (Object item : collection) {
					String itemKey = "[" + (count++) + "]";
					flatten(properties, Collections.singletonMap(itemKey, item), name);
				}
			} else if (value instanceof String) {
				properties.put(name, value);
			} else if (value instanceof Number) {
				properties.put(name, value.toString());
			} else if (value instanceof Boolean) {
				properties.put(name, value.toString());
			} else {
				properties.put(name, (value != null) ? value : "");
			}
		});
	}

	private String getPropertyName(String path, String key) {
		if (!StringUtils.hasText(path)) {
			return key;
		}
		if (key.startsWith("[")) {
			return path + key;
		}
		return path + "." + key;
	}
}