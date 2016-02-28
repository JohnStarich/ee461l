package com.johnstarich.moviematcher.app;

import com.google.gson.Gson;
import spark.ResponseTransformer;

/**
 * Created by johnstarich on 2/23/16.
 */
public class JsonTransformer implements ResponseTransformer {
	private Gson gson = new Gson();

	@Override
	public String render(Object model) throws Exception {
		return gson.toJson(model);
	}
}
