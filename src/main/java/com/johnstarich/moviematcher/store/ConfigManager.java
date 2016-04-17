package com.johnstarich.moviematcher.store;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by johnstarich on 4/17/16.
 */
public class ConfigManager {
	private static final Map<String, String> config = new HashMap<>(System.getenv());

	static {
		if (! config.containsKey("MONGO_HOST")) {
			System.out.println("DEVELOPMENT MODE ---- USING LOCALHOST FOR MONGO_HOST");
			config.put("MONGO_HOST", "localhost");
		}
	}

	public static String getProperty(String property) throws ExceptionInInitializerError {
		String value = config.get(property);
		if(value == null)
			throw new ExceptionInInitializerError("No property found in config with name "+property);
		return value;
	}

	public static String getPropertyOrDefault(String property, String defaultValue) {
		return config.getOrDefault(property, defaultValue);
	}
}
