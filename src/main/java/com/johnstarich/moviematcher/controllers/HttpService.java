package com.johnstarich.moviematcher.controllers;

/**
 * Represents a service for incoming requests
 * The mount point is where this service resides (such as "/v1")
 * The resource is the name of the object this service deals with (such as "user" or "session")
 * This provides the ability to set up and tear down the service
 * Created by johnstarich on 5/22/16.
 */
public interface HttpService {
	/**
	 * Where this service resides
	 * This must not change between calls or rely on the current object's fields
	 * For example: <pre>return "v1";</pre> or <pre>return "v1/moviematcher";</pre>
	 */
	String mountPoint();

	/**
	 * The route prefix for this service.
	 * This prefix should not change between calls.
	 * (Not automatically prepended to Spark.* routes)
	 */
	String resource();

	/** Set up this service */
	void init();

	/** Tear down this service */
	void destroy();
}
