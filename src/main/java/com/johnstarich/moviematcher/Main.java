package com.johnstarich.moviematcher;

import com.johnstarich.moviematcher.app.MovieMatcherApplication;
import spark.Spark;

/**
 * Runs an embedded Jetty server with the packaged webapp.
 * Created by johnstarich on 1/25/16.
 */
public class Main {
	public static void main(String args[]) throws Exception {
		Spark.port(8080);
		Spark.threadPool(10);
		new MovieMatcherApplication().init();
	}
}
