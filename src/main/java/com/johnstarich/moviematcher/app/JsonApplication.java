package com.johnstarich.moviematcher.app;

import spark.Route;

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
	protected final void jdelete(String path, Route route) { delete(PREFIX+path, route, json); }

	/** JSON transformed GET handler. */
	protected final void jget(String path, String acceptType, Route route) { get(PREFIX+path, acceptType, route, json); }

	/** JSON transformed GET handler. */
	protected final void jget(String path, Route route) { get(PREFIX+path, route, json); }

	/** JSON transformed PATCH handler. */
	protected final void jpatch(String path, String acceptType, Route route) { patch(PREFIX+path, acceptType, route, json); }

	/** JSON transformed PATCH handler. */
	protected final void jpatch(String path, Route route) { patch(PREFIX+path, route, json); }

	/** JSON transformed POST handler. */
	protected final void jpost(String path, String acceptType, Route route) { post(PREFIX+path, acceptType, route, json); }

	/** JSON transformed POST handler. */
	protected final void jpost(String path, Route route) { post(PREFIX+path, route, json); }

	/** JSON transformed PUT handler. */
	protected final void jput(String path, String acceptType, Route route) { put(PREFIX+path, acceptType, route, json); }

	/** JSON transformed PUT handler. */
	protected final void jput(String path, Route route) { put(PREFIX+path, route, json); }

	/** JSON transformed TRACE handler. */
	protected final void jtrace(String path, String acceptType, Route route) { trace(PREFIX+path, acceptType, route, json); }

	/** JSON transformed TRACE handler. */
	protected final void jtrace(String path, Route route) { trace(PREFIX+path, route, json); }

	@Override
	public final void init() {
		super.init();
		before(PREFIX+"/*", (req, resp) -> resp.type("application/json"));
	}

	@Override
	public final void destroy() {
	}
}
