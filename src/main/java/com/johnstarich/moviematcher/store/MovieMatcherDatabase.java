package com.johnstarich.moviematcher.store;

import de.caluga.morphium.Morphium;
import de.caluga.morphium.MorphiumConfig;

import java.net.UnknownHostException;

/**
 * Created by johnstarich on 3/22/16.
 */
public class MovieMatcherDatabase {
	public static final Morphium morphium;

	private static final int GLOBAL_CACHE_VALID_TIME = 5000;
	private static final int WRITE_CACHE_TIMEOUT = 100;

	static {
		MorphiumConfig config = new MorphiumConfig();
		String mongoHost = ConfigManager.getProperty("MONGO_HOST"); //gets an environment variable (this is where the db is located)
		try {
			config.addHost(mongoHost);
		}
		catch(UnknownHostException e) {
			throw new RuntimeException("Could not connect to MongoDB host: " + mongoHost, e);
		}
		config.setDatabase("moviematcher");
		config.setGlobalCacheValidTime(GLOBAL_CACHE_VALID_TIME);
		config.setWriteCacheTimeout(WRITE_CACHE_TIMEOUT);
		morphium = new Morphium(config);
	}
}
