package com.johnstarich.moviematcher.routes;

import com.google.gson.JsonSyntaxException;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.http.HttpField;
import org.json.simple.parser.ParseException;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Wrapper for HTTP responses using the Jetty HTTP client.
 * Created by johnstarich on 5/28/16.
 */
public class HttpResponseWrapper {
	public final String body, type, encoding;
	public final int status;
	public final Map<String, String> headers;
	public final HttpContentResponse raw;

	public HttpResponseWrapper(HttpContentResponse response) {
		this.raw = response;
		this.body = response.getContentAsString();
		this.status = response.getStatus();
		this.headers = StreamSupport.stream(response.getHeaders().spliterator(), true)
			.collect(Collectors.toMap(HttpField::getName, HttpField::getValue));
		this.type = response.getMediaType();
		this.encoding = response.getEncoding();
	}

	public String header(String name) {
		return headers.get(name);
	}

	public String reason() {
		return raw.getReason();
	}

	public <T> T json(Class<T> clazz) {
		try {
			return new JsonTransformer().parse(body, clazz);
		}
		catch (JsonSyntaxException | ParseException e) {
			throw new AssertionError("Could not parse into " + clazz.getSimpleName() + ".class with data: " + body, e);
		}
	}
}
