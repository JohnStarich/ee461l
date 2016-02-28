package com.johnstarich.moviematcher.app;

import spark.servlet.SparkApplication;

import static spark.Spark.*;

/**
 * A base template for a simple service.
 * Created by johnstarich on 2/25/16.
 */
public abstract class BasicApplication implements SparkApplication {
	public abstract String prefix();
	public final String PREFIX = prefix();

	/** Initialize any routes for your JSON app here. */
	public abstract void app();

	@Override
	public void init() {
		staticFileLocation("WEB-INF");

		before(PREFIX+"/*", "text/html", (request, response) -> {
			// Enable gzip compression if client supports it
			if(request.headers("Content-Encoding") != null && request.headers("Content-Encoding").contains("gzip")) {
				response.header("Content-Encoding", "gzip");
			}
		});

		before("/404", (request, response) -> {
			throw new HTTPException(404);
		});

		exception(HTTPException.class, (e, request, response) -> {
			int statusCode = ((HTTPException)e).getStatusCode();
			response.type("text/html");
			response.status(statusCode);
			response.body(String.format("<h1>%d %s</h1>", statusCode, e.getMessage()));
		});

		exception(Exception.class, (e, request, response) -> {
			response.type("text/html");
			response.status(500);
			response.body("<h1>500 Internal Server Error</h1>");
		});

		this.app();
	}

	@Override
	public void destroy() {
	}
}
