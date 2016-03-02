package com.johnstarich.moviematcher;

import com.darrinholst.sass_java.Compiler;
import com.darrinholst.sass_java.SassCompilingFilter;
import com.johnstarich.moviematcher.app.MovieMatcherApplication;
import spark.Spark;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Runs an embedded Jetty server with the packaged webapp.
 * Created by johnstarich on 1/25/16.
 */
public class Main {
	public static void main(String args[]) throws Exception {
		Spark.port(8080);
		Spark.threadPool(10);
		new MovieMatcherApplication().init();
		sassInit();
	}

	public static void sassInit() throws ExceptionInInitializerError, URISyntaxException {
		URL sassConfigLocation = Main.class.getClassLoader().getResource("WEB-INF/sass/config.rb");
		if(sassConfigLocation == null) {
			throw new ExceptionInInitializerError("ERROR INITIALIZING SASS: Could not find config file in WEB-INF/sass/config.rb");
		}
		SassCompilingFilter filter = new SassCompilingFilter();
		Compiler c = new Compiler();
		c.setConfigLocation(new File(sassConfigLocation.toURI()));
		filter.setCompiler(c);

		Spark.before((request, response) -> {
			filter.doFilter(request.raw(), response.raw(), (req, res) -> {});
		});
	}
}
