package com.johnstarich.moviematcher.controllers;

import com.johnstarich.moviematcher.utils.ClientFacingHttpException;
import com.johnstarich.moviematcher.utils.HttpException;
import com.johnstarich.moviematcher.utils.HttpStatus;

import java.util.Optional;

import spark.Request;
import spark.Response;

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
			HttpStatus statusCode = ((HttpException) e).getStatusCode();
			if(e.getCause() != null) e.getCause().printStackTrace();
			logError(request, response, e.getMessage(), ((ClientFacingHttpException) e).getHiddenMessage(), statusCode);
		});

		exception(HttpException.class, (e, request, response) -> {
			HttpStatus statusCode = ((HttpException) e).getStatusCode();
			if(e.getCause() != null) e.getCause().printStackTrace();
			logError(request, response, e.getMessage(), e.getMessage(), statusCode);
		});

		exception(Exception.class, (e, request, response) -> {
			e.printStackTrace();
			logError(request, response, "Internal Server Error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		});
	}

	private static void logError(Request request, Response response, String message, HttpStatus statusCode) {
		logError(request, response, message, message, statusCode);
	}

	private static void logError(Request request, Response response, String clientMessage, String consoleMessage, HttpStatus statusCode) {
		String logMessage = String.format("[%s] ERROR: %d %s\n", request.uri(), statusCode.code, consoleMessage);
		String clientFacingMessage = String.format("%d %s", statusCode.code, clientMessage);
		System.err.println(logMessage);
		System.out.println(logMessage);
		response.status(statusCode.code);
		response.body(clientFacingMessage);
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
