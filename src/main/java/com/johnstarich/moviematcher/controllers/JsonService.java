package com.johnstarich.moviematcher.controllers;

import com.google.gson.JsonSyntaxException;
import com.johnstarich.moviematcher.models.User;
import com.johnstarich.moviematcher.routes.AuthenticatedRoute;
import com.johnstarich.moviematcher.routes.JsonTransformer;
import org.json.simple.parser.ParseException;
import spark.Filter;
import spark.Request;
import spark.Route;

import java.util.Map;
import java.util.Optional;

import static spark.Spark.*;

/**
 * A base template for services that always return JSON results.
 * Created by johnstarich on 2/23/16.
 */
public abstract class JsonService extends AbstractService {
	private static final JsonTransformer json = new JsonTransformer();

	@Override
	public final String mountPoint() {
		return "/v1";
	}

	@Override
	public final void init() {
		super.init();
		Filter jsonAttribute = (req, resp) -> {
			resp.type("application/json");
			Map jsonMap = null;
			if(req.contentType() != null && req.contentType().contains("application/json")) {
				try {
					jsonMap = json.parse(req.body(), Map.class);
				}
				catch (JsonSyntaxException | ParseException e) {
					// ignore syntax errors: any unknown fields, if used, will be empty Optionals
				}
			}
			req.attribute("json", Optional.ofNullable(jsonMap));
		};
		before(PREFIX+"/*", jsonAttribute);
		before(PREFIX, jsonAttribute);
		this.initService();
	}

	protected abstract void initService();

	public <T> Optional<T> bodyParam(Request request, String key) {
		Optional<Map<String, T>> mapOptional = request.attribute("json");
		if(! mapOptional.isPresent()) return Optional.empty();
		return Optional.ofNullable(mapOptional.get().get(key));
	}

	private Route convertAuthenticatedRoute(AuthenticatedRoute route) {
		return (request, response) -> {
			User user = request.attribute("user");
			return route.handle(request, response, user);
		};
	}

	private String checkPath(String path) {
		if(! path.startsWith("/")) return '/' + path;
		return path;
	}

	/** JSON transformed DELETE handler. */
	protected final void jdelete(Route route) { delete(PREFIX, "application/json", route, json); }

	/** JSON transformed DELETE handler. */
	protected final void jdelete(String path, Route route) { delete(PREFIX + checkPath(path), "application/json", route, json); }

	/** JSON transformed DELETE handler. */
	protected final void jdelete(AuthenticatedRoute route) { delete(PREFIX, "application/json", convertAuthenticatedRoute(route), json); }

	/** JSON transformed DELETE handler. */
	protected final void jdelete(String path, AuthenticatedRoute route) { delete(PREFIX + checkPath(path), "application/json", convertAuthenticatedRoute(route), json); }

	/** JSON transformed GET handler. */
	protected final void jget(Route route) { get(PREFIX, "application/json", route, json); }

	/** JSON transformed GET handler. */
	protected final void jget(String path, Route route) { get(PREFIX + checkPath(path), "application/json", route, json); }

	/** JSON transformed GET handler. */
	protected final void jget(AuthenticatedRoute route) { get(PREFIX, "application/json", convertAuthenticatedRoute(route), json); }

	/** JSON transformed GET handler. */
	protected final void jget(String path, AuthenticatedRoute route) { get(PREFIX + checkPath(path), "application/json", convertAuthenticatedRoute(route), json); }

	/** JSON transformed PATCH handler. */
	protected final void jpatch(Route route) { patch(PREFIX, "application/json", route, json); }

	/** JSON transformed PATCH handler. */
	protected final void jpatch(String path, Route route) { patch(PREFIX + checkPath(path), "application/json", route, json); }

	/** JSON transformed PATCH handler. */
	protected final void jpatch(AuthenticatedRoute route) { patch(PREFIX, "application/json", convertAuthenticatedRoute(route), json); }

	/** JSON transformed PATCH handler. */
	protected final void jpatch(String path, AuthenticatedRoute route) { patch(PREFIX + checkPath(path), "application/json", convertAuthenticatedRoute(route), json); }

	/** JSON transformed POST handler. */
	protected final void jpost(Route route) { post(PREFIX, "application/json", route, json); }

	/** JSON transformed POST handler. */
	protected final void jpost(String path, Route route) { post(PREFIX + checkPath(path), "application/json", route, json); }

	/** JSON transformed POST handler. */
	protected final void jpost(AuthenticatedRoute route) { post(PREFIX, "application/json", convertAuthenticatedRoute(route), json); }

	/** JSON transformed POST handler. */
	protected final void jpost(String path, AuthenticatedRoute route) { post(PREFIX + checkPath(path), "application/json", convertAuthenticatedRoute(route), json); }

	/** JSON transformed PUT handler. */
	protected final void jput(Route route) { put(PREFIX, "application/json", route, json); }

	/** JSON transformed PUT handler. */
	protected final void jput(String path, Route route) { put(PREFIX + checkPath(path), "application/json", route, json); }

	/** JSON transformed PUT handler. */
	protected final void jput(AuthenticatedRoute route) { put(PREFIX, "application/json", convertAuthenticatedRoute(route), json); }

	/** JSON transformed PUT handler. */
	protected final void jput(String path, AuthenticatedRoute route) { put(PREFIX + checkPath(path), "application/json", convertAuthenticatedRoute(route), json); }

	/** JSON transformed TRACE handler. */
	protected final void jtrace(Route route) { trace(PREFIX, "application/json", route, json); }

	/** JSON transformed TRACE handler. */
	protected final void jtrace(String path, Route route) { trace(PREFIX + checkPath(path), "application/json", route, json); }

	/** JSON transformed TRACE handler. */
	protected final void jtrace(AuthenticatedRoute route) { trace(PREFIX, "application/json", convertAuthenticatedRoute(route), json); }

	/** JSON transformed TRACE handler. */
	protected final void jtrace(String path, AuthenticatedRoute route) { trace(PREFIX + checkPath(path), "application/json", convertAuthenticatedRoute(route), json); }
}
