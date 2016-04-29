package com.johnstarich.moviematcher.app;

import spark.Request;
import spark.Route;

import java.util.Map;
import java.util.Optional;

import static spark.Spark.*;

/**
 * A base template for services that always return JSON results.
 * Created by johnstarich on 2/23/16.
 */
public abstract class JsonApplication extends BasicApplication {
	private final JsonTransformer json = new JsonTransformer();

	/** JSON transformed DELETE handler. */
	protected final void jdelete(String path, String acceptType, Route route) { delete(PREFIX+path, acceptType, route, json); }

	/** JSON transformed DELETE handler. */
	protected final void jdelete(String path, Route route) { delete(PREFIX+path, "application/json", route, json); }

	/** JSON transformed GET handler. */
	protected final void jget(String path, String acceptType, Route route) { get(PREFIX+path, acceptType, route, json); }

	/** JSON transformed GET handler. */
	protected final void jget(String path, Route route) { get(PREFIX+path, "application/json", route, json); }

	/** JSON transformed PATCH handler. */
	protected final void jpatch(String path, String acceptType, Route route) { patch(PREFIX+path, acceptType, route, json); }

	/** JSON transformed PATCH handler. */
	protected final void jpatch(String path, Route route) { patch(PREFIX+path, "application/json", route, json); }

	/** JSON transformed POST handler. */
	protected final void jpost(String path, String acceptType, Route route) { post(PREFIX+path, acceptType, route, json); }

	/** JSON transformed POST handler. */
	protected final void jpost(String path, Route route) { post(PREFIX+path, "application/json", route, json); }

	/** JSON transformed PUT handler. */
	protected final void jput(String path, String acceptType, Route route) { put(PREFIX+path, acceptType, route, json); }

	/** JSON transformed PUT handler. */
	protected final void jput(String path, Route route) { put(PREFIX+path, "application/json", route, json); }

	/** JSON transformed TRACE handler. */
	protected final void jtrace(String path, String acceptType, Route route) { trace(PREFIX+path, acceptType, route, json); }

	/** JSON transformed TRACE handler. */
	protected final void jtrace(String path, Route route) { trace(PREFIX+path, "application/json", route, json); }

	@Override
	public final void init() {
		super.init();
		before(PREFIX+"/*", (req, resp) -> {
			resp.type("application/json");
			Map jsonMap = null;
			if(req.contentType() != null && req.contentType().contains("application/json")) {
				jsonMap = json.parse(req.body(), Map.class);
			}
			req.attribute("json", Optional.ofNullable(jsonMap));
		});
	}

	@Override
	public final void destroy() {
	}

	public <T> Optional<T> bodyParam(Request request, String key) {
		Optional<Map<String, T>> mapOptional =  request.attribute("json");
		if(! mapOptional.isPresent()) return Optional.empty();
		return Optional.ofNullable(mapOptional.get().get(key));
	}
}
