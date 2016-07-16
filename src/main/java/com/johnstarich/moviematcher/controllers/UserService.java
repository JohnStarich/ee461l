package com.johnstarich.moviematcher.controllers;

import com.johnstarich.moviematcher.utils.HttpException;
import com.johnstarich.moviematcher.utils.HttpStatus;
import com.johnstarich.moviematcher.models.Movie;
import com.johnstarich.moviematcher.models.Rating;
import com.johnstarich.moviematcher.models.Session;
import com.johnstarich.moviematcher.models.User;
import org.bson.types.ObjectId;
import spark.Spark;

import java.util.*;

/**
 * Register login services, like registration and user login API,
 * as well as authentication services.
 * Created by johnstarich on 5/22/16.
 */
public class UserService extends JsonService {
	@Override
	public String resource() {
		return "users";
	}

	private final Set<String> WHITELIST_AUTH_ROUTES = new HashSet<>(Arrays.asList(
		PREFIX, // i.e. /v1/users
		mountPoint(), // i.e. /v1
		"/robots.txt"
	));

	private final Set<String> WHITELIST_AUTH_PREFIXES = new HashSet<>(Arrays.asList(
		PREFIX, // i.e. /v1/users/*
		"/login",
		"/register",
		"/assets/",
		"/fonts/",
		"/favicon"
	));

	@Override
	public void initService() {
		jpost("login", (request, response) -> {
			Optional<String> username = bodyParam(request, "username");
			Optional<String> password = bodyParam(request, "password");
			if(! username.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No username provided");
			if(! password.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No password provided");

			return User.login(username.get(), password.get());
		});

		jpost((request, response) -> {
			Optional<String> firstName = bodyParam(request, "first_name");
			Optional<String> lastName = bodyParam(request, "last_name");
			Optional<String> username = bodyParam(request, "username");
			Optional<String> password = bodyParam(request, "password");

			if(! firstName.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No first_name provided");
			if(! lastName.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No last_name provided");
			if(! username.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No username provided");
			if(! password.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No password provided");

			User newUser = new User(
				new ObjectId(),
				username.get(),
				firstName.get(),
				lastName.get()
			);
			return newUser.register(password.get()).noPassword();
		});

		jget((request, response) -> {
			Optional<String> session_id = Optional.ofNullable(request.headers("Authorization"));
			if(! session_id.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");

			Optional<Session> session = new Session(new ObjectId(session_id.get()), null).load(Session.class);
			if(! session.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");

			return session.get().user.noPassword();
		});

		jpatch((request, response) -> {
			Optional<String> session_id = Optional.ofNullable(request.headers("Authorization"));
			if(! session_id.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");

			Optional<Session> session = new Session(new ObjectId(session_id.get()), null).load(Session.class);
			if(! session.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");

			Optional<Map<String, Object>> userFieldsOpt = bodyParam(request, "user");
			if(! userFieldsOpt.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "Invalid user parameters");

			Map<String, Object> userFields = userFieldsOpt.get();
			String firstName = (String)userFields.get("first_name");
			String lastName = (String)userFields.get("last_name");
			String password = (String)userFields.get("password");
			String oldPassword = (String) userFields.get("old_password");

			if((password == null && oldPassword != null) || (password != null && oldPassword == null)) {
				throw new HttpException(HttpStatus.BAD_REQUEST, "New password and old password must be provided together.");
			}

			User patch = new User(session.get().user.id, null, firstName, lastName);
			patch.update();

			Optional<User> user = patch.load(User.class);

			if(password != null && user.isPresent()) {
				user.get().resetPassword(oldPassword, password);
			}
			return "success";
		});

		jpost("ratings", (request, response) -> {
			Optional<String> session_id = Optional.ofNullable(request.headers("Authorization"));
			if(! session_id.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");

			Optional<Session> session = new Session(new ObjectId(session_id.get()), null).load(Session.class);
			if(! session.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");

			Optional<Map<String, Object>> userFieldsOpt = bodyParam(request, "user");
			if(! userFieldsOpt.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "Invalid user parameters");

			Map<String, Object> userFields = userFieldsOpt.get();
			String movieId = (String) userFields.get("id");
			double rating = (double) userFields.get("rating");

			Optional<Movie> m =  new Movie(new ObjectId(movieId)).load(Movie.class);

			if(m.isPresent()) {
				User u = session.get().user;
				Optional<Rating> r = Rating.loadRatingByUser(u.id, m.get().id);


				if(r.isPresent()) {
					Rating patch = new Rating(r.get().id, u.id, m.get().id, null, (int) rating).update();
					patch.update();
				}
				else {
					new Rating(new ObjectId(), u.id, m.get().id, "", (int) rating).save();
				}
			}

			return "rating saved!";

		});

		Spark.before("/*", (request, response) -> {
			String path = request.pathInfo();
			if(WHITELIST_AUTH_ROUTES.contains(path) ||
				WHITELIST_AUTH_PREFIXES.parallelStream()
					.anyMatch(path::startsWith)
				) return;

			Optional<String> authorization = Optional.ofNullable(request.headers("Authorization"));
			if(! authorization.isPresent()) {
				if(request.headers("Accept") != null && request.headers("Accept").contains("text/html")){
					// requesting HTML page, allow the UI to handle the errors for other requests
					return;
				}
				throw new HttpException(HttpStatus.UNAUTHORIZED, "No authorization provided.");
			}

			Optional<Session> session = new Session(new ObjectId(authorization.get().trim()), null).load(Session.class);
			if(! session.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session.");
			if(! Session.isValid(session.get())) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session.");

			request.attribute("user", session.get().user);
		});
	}
}
