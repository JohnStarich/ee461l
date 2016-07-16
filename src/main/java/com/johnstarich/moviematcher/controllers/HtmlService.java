package com.johnstarich.moviematcher.controllers;

import com.johnstarich.moviematcher.routes.ServeStaticFileRoute;
import spark.Spark;

/**
 * Serves static HTML on /* with a default index.html served for files that cannot be found
 * Created by johnstarich on 5/22/16.
 */
public class HtmlService implements HttpService {
	@Override
	public String mountPoint() {
		return "/";
	}

	@Override
	public String resource() {
		return "";
	}

	@Override
	public void init() {
		// accept any type except JSON
		Spark.get("/*", "*/*;q=1.0, application/json;q=0.0", new ServeStaticFileRoute("/index.html"));
	}

	@Override
	public void destroy() {}
}
