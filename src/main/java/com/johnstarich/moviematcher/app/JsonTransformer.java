package com.johnstarich.moviematcher.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.johnstarich.moviematcher.models.AbstractModel;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import org.bson.types.ObjectId;
import spark.ResponseTransformer;

import java.io.IOException;

/**
 * Transforms objects into JSON strings
 * Created by johnstarich on 2/23/16.
 */
public class JsonTransformer implements ResponseTransformer {
	private Gson gson = new GsonBuilder()
		.registerTypeAdapter(ObjectId.class, new TypeAdapter<ObjectId>() {
			@Override
			public void write(JsonWriter jsonWriter, ObjectId objectId) throws IOException {
				jsonWriter.value(objectId.toHexString());
			}

			@Override
			public ObjectId read(JsonReader jsonReader) throws IOException {
				return new ObjectId(jsonReader.nextString());
			}
		})
		.create();

	@Override
	public String render(Object model) throws Exception {
		if(model instanceof AbstractModel) return MovieMatcherDatabase.morphium.toJsonString(model);
		else return gson.toJson(model);
	}

	public <T> T parse(String model, Class<T> clazz) throws Exception {
		return gson.fromJson(model, clazz);
	}
}
