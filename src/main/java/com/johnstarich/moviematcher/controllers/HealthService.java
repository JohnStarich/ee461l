package com.johnstarich.moviematcher.controllers;

import com.johnstarich.moviematcher.models.Status;
import spark.Route;

/**
 * Created by johnstarich on 5/22/16.
 */
public class HealthService extends JsonService {
	@Override
	public String resource() {
		return "";
	}

	@Override
	public void initService() {
		Route statusRoute = (request, response) -> new Status("1.0.0");
		jget(statusRoute);
		jget("/", statusRoute);
	}
}
