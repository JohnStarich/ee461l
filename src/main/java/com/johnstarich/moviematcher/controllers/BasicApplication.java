package com.johnstarich.moviematcher.controllers;

import com.johnstarich.moviematcher.app.HttpException;
import com.johnstarich.moviematcher.app.HttpStatus;
import spark.servlet.SparkApplication;

import java.util.Optional;

import static spark.Spark.*;

/**
 * A base template for a simple service.
 * Created by johnstarich on 2/25/16.
 */
public abstract class BasicApplication implements SparkApplication {
	/**
	 * The route prefix for all routes defined in this application.
	 * This prefix should not change between calls.
	 * (Not automatically prepended to Spark routes)
	 * @return the route prefix
	 */
	public abstract String prefix();

	/** An immutable copy of the prefix. */
	public final String PREFIX = prefix();

	/** Initialize any routes for your JSON app here. */
	public abstract void app();

	@Override
	public void init() {
		staticFileLocation("static");

		before(PREFIX+"/*", "text/html", (request, response) -> {
			// Enable gzip compression if client supports it
			if(request.headers("Content-Encoding") != null && request.headers("Content-Encoding").contains("gzip")) {
				response.header("Content-Encoding", "gzip");
			}
		});

		before("/404", (request, response) -> {
			throw new HttpException(HttpStatus.NOT_FOUND);
		});

		exception(HttpException.class, (e, request, response) -> {
			int statusCode = ((HttpException)e).getStatusCode();
			response.status(statusCode);
			response.body(String.format("%d %s", statusCode, e.getMessage()));
			System.err.println(String.format("[%s] ERROR: %d %s", request.uri(), statusCode, e.getMessage()));
			if(e.getCause() != null)
				e.getCause().printStackTrace();
		});

		exception(Exception.class, (e, request, response) -> {
			response.status(HttpStatus.SERVER_ERROR.code);
			response.body("500 Internal Server Error");
			e.printStackTrace();
		});

		this.app();
	}

	@Override
	public void destroy() {
	}

	public Optional<Integer> asIntOpt(String param) {
		try {
			return Optional.of(Integer.parseInt(param));
		}
		catch(NumberFormatException e) {
			return Optional.empty();
		}
	}
}
