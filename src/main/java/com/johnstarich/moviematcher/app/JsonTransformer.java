package com.johnstarich.moviematcher.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ResponseTransformer;

/**
 * Transforms objects into JSON strings
 * Created by johnstarich on 2/23/16.
 */
public class JsonTransformer implements ResponseTransformer {
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Override
	public String render(Object model) throws Exception {
		return gson.toJson(model);
	}
}
