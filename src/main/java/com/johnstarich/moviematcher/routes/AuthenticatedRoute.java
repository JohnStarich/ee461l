package com.johnstarich.moviematcher.routes;

import com.johnstarich.moviematcher.models.User;
import spark.Request;
import spark.Response;

/**
 * Routes provided with the current authenticated user.
 * Created by johnstarich on 5/16/16.
 */
@FunctionalInterface
public interface AuthenticatedRoute {
	Object handle(Request request, Response response, User user) throws Exception;
}
