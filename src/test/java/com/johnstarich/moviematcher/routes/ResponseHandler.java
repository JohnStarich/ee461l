package com.johnstarich.moviematcher.routes;

/**
 * Functional interface for responses used in AbstractHttpClientTest cases
 * Created by johnstarich on 5/28/16.
 */
@FunctionalInterface
public interface ResponseHandler {
	void handle(HttpResponseWrapper response);
}
