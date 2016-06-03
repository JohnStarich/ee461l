package com.johnstarich.moviematcher.routes;

/**
 * Created by johnstarich on 5/28/16.
 */
@FunctionalInterface
public interface ResponseHandler {
	void handle(HttpResponseWrapper response);
}
