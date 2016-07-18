package com.johnstarich.moviematcher.controllers;

import com.johnstarich.moviematcher.utils.ClientFacingHttpException;
import com.johnstarich.moviematcher.utils.HttpException;
import com.johnstarich.moviematcher.utils.HttpStatus;

import java.util.Optional;

import static spark.Spark.*;

/**
 * A base template for a simple service.
 * Created by johnstarich on 2/25/16.
 */
public abstract class AbstractService implements HttpService {
	/** Constructs a prefix for possible use in a custom route handler */
	private String prefix() {
		StringBuilder completePrefix = new StringBuilder();
		String mountPoint = mountPoint();
		String resource = resource();

		completePrefix.append(mountPoint);
		if(resource.isEmpty()) return completePrefix.toString();
		if(! mountPoint.endsWith("/")) completePrefix.append('/');

		if(resource.startsWith("/")) completePrefix.append(resource.substring(1));
		else completePrefix.append(resource);

		if(resource.endsWith("/")) return completePrefix.substring(0, completePrefix.length()-1);
		return completePrefix.toString();
	}

	/** An immutable copy of prefix() */
	public final String PREFIX = prefix();

	/** Set up static file serving via Spark's API */
	public static void initStaticResourceHandlers() {
		staticFileLocation("static");
	}

	/** Set up gzip compression for browsers that support it */
	public static void initCompressionHandlers() {
		before("/*", "text/html", (request, response) -> {
			// Enable gzip compression if client supports it
			if(request.headers("Content-Encoding") != null && request.headers("Content-Encoding").contains("gzip")) {
				response.header("Content-Encoding", "gzip");
			}
		});
	}

	/** Set up exception handling and error message obscurity; sets up a /404 route */
	public static void initErrorHandlers() {
		before("/404", (request, response) -> {
			throw new HttpException(HttpStatus.NOT_FOUND);
		});

		exception(ClientFacingHttpException.class, (e, request, response) -> {
			HttpStatus statusCode = ((HttpException)e).getStatusCode();
			response.status(statusCode.code);
			response.body(String.format("%d %s", statusCode.code, e.getMessage()));
			System.err.printf("[%s] ERROR: %d %s", request.uri(), statusCode.code, ((ClientFacingHttpException)e).getHiddenMessage());
			if(e.getCause() != null)
				e.getCause().printStackTrace();
		});

		exception(HttpException.class, (e, request, response) -> {
			HttpStatus statusCode = ((HttpException)e).getStatusCode();
			response.status(statusCode.code);
			response.body(String.format("%d %s", statusCode.code, e.getMessage()));
			System.err.println(String.format("[%s] ERROR: %d %s", request.uri(), statusCode.code, e.getMessage()));
			if(e.getCause() != null)
				e.getCause().printStackTrace();
		});

		exception(Exception.class, (e, request, response) -> {
			response.status(HttpStatus.SERVER_ERROR.code);
			response.body("500 Internal Server Error");
			e.printStackTrace();
		});
	}

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
	}

	/**
	 * Try to parse param as an integer and wrapping it in Optional
	 * @param param the number string to parse
	 * @return Optional of the parsed string, empty optional otherwise
	 */
	public Optional<Integer> asIntOpt(String param) {
		try {
			return Optional.of(Integer.parseInt(param));
		}
		catch(NumberFormatException e) {
			return Optional.empty();
		}
	}
}
