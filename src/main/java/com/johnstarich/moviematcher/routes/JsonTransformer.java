package com.johnstarich.moviematcher.routes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.johnstarich.moviematcher.models.AbstractModel;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import org.bson.types.ObjectId;
import org.json.simple.parser.ParseException;
import spark.ResponseTransformer;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Transforms objects into JSON strings
 * Created by johnstarich on 2/23/16.
 */
public class JsonTransformer implements ResponseTransformer {
	private final Gson gson = new GsonBuilder()
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
		else if(model instanceof Collection) {
			return ((Collection<AbstractModel>) model)
				.parallelStream()
				.map(MovieMatcherDatabase.morphium::toJsonString)
				.collect(Collectors.toList()).toString();
		}
		else return gson.toJson(model);
	}

	private static final Pattern OBJECT_ID = Pattern.compile("\\{\\s*\"\\$oid\"\\s*:\\s*(?<id>\"[0-9a-f]+\")\\s*\\}");

	public <T> T parse(String model, Class<T> clazz) throws JsonSyntaxException, ParseException {
		if (AbstractModel.class.isAssignableFrom(clazz)) {
			model = OBJECT_ID.matcher(model).replaceAll("$1");
			return MovieMatcherDatabase.morphium.getMapper().unmarshall(clazz, model);
		}
		return gson.fromJson(model, clazz);
	}
}
