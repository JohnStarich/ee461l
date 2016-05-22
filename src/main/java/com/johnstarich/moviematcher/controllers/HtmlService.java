package com.johnstarich.moviematcher.controllers;

import spark.Spark;

/**
 * Created by johnstarich on 5/22/16.
 */
public class HtmlService extends BasicController {
	@Override
	public String prefix() {
		return "/";
	}

	@Override
	public void initService() {
		Spark.get("/*", new ServeStaticFileRoute("/index.html"));
	}
}
