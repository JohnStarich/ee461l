package com.johnstarich.moviematcher.app;

import com.johnstarich.moviematcher.models.*;
import org.bson.types.ObjectId;
import spark.Route;
import spark.Spark;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Movie Matcher API is defined here. These routes make up the Movie Matcher services.
 * Created by johnstarich on 2/25/16.
 */
public class MovieMatcherApplication extends JsonApplication {
	@Override
	public String prefix() { return "/v1"; }

	@Override
	public void app() {
		Route statusRoute = (request, response) -> new Status("1.0.0");
		jget("", statusRoute);
		jget("/", statusRoute);

		usersService();
		moviesService();
		friendsService();
		groupsService();

		htmlService();
	}

	public void htmlService() {
		Spark.get("/favicon.ico", new ServeStaticFileRoute());
		Spark.get("/fonts/*", new ServeStaticFileRoute());
		Spark.get("/tests/*", new ServeStaticFileRoute());
		Spark.get("/assets/*", new ServeStaticFileRoute());
		Spark.get("/*", "text/html", new ServeStaticFileRoute("/index.html"));
	}

	/**
	 * Register login services, like registration and user login API,
	 * as well as authentication services.
	 */
	public void usersService() {
		jpost("/users/login", (request, response) -> {
			Optional<String> username = bodyParam(request, "username");
			Optional<String> password = bodyParam(request, "password");
			if(! username.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No username provided");
			if(! password.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No password provided");

			return User.login(username.get(), password.get());
		});

		jpost("/users", (request, response) -> {
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
			return newUser.register(password.get());
		});

		jget("/users", (request, response) -> {
			Optional<String> session_id = Optional.ofNullable(request.headers("Authorization"));
			if(! session_id.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");

			Optional<Session> session = new Session(new ObjectId(session_id.get()), null).load();
			if(! session.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");

			return session.get().user;
		});

		jpatch("/users", (request, response) -> {
			Optional<String> session_id = Optional.ofNullable(request.headers("Authorization"));
			if(! session_id.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");

			Optional<Session> session = new Session(new ObjectId(session_id.get()), null).load();
			if(! session.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");

			Optional<Map<String, Object>> userFieldsOpt = bodyParam(request, "user");
			if(! userFieldsOpt.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "Invalid user parameters");

			Map<String, Object> userFields = userFieldsOpt.get();
			String firstName = (String)userFields.get("first_name");
			String lastName = (String)userFields.get("last_name");
			String password = (String)userFields.get("password");
			String oldPassword = (String)userFields.get("old_password");

			if((password == null && oldPassword != null) || (password != null && oldPassword == null)) {
				throw new HttpException(HttpStatus.BAD_REQUEST, "New password and old password must be provided together.");
			}

			User patch = new User(session.get().user.id, null, firstName, lastName);
			patch.update();

			Optional<User> user = patch.load();

			if(password != null && user.isPresent()) {
				user.get().resetPassword(oldPassword, password);
			}
			return "true";
		});

		jget("/login/session", (request, response) -> {
			User u = User.loadByUsername("berlg");
			System.out.println(u.first_name);
			return u;
		});

		Spark.before("/*", (request, response) -> {
			String path = request.pathInfo();
			if(path.equals(PREFIX) ||
					path.equals(PREFIX+"/") ||
					path.startsWith("/login") ||
					path.startsWith("/register") ||
					path.startsWith(PREFIX+"/users") ||
					path.startsWith("/assets") ||
					path.startsWith("/fonts") ||
					path.equals("robots.txt")
				)
				return;

			Optional<String> authorization = Optional.ofNullable(request.headers("Authorization"));
			if(! authorization.isPresent()) {
				if(request.headers("Accept") != null && request.headers("Accept").contains("text/html")){
					// requesting HTML page, allow the UI to handle the errors for other requests
					return;
				}
				throw new HttpException(HttpStatus.UNAUTHORIZED, "No authorization provided.");
			}

			Optional<Session> session = new Session(new ObjectId(authorization.get().trim()), null).load();
			if(! session.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session.");
			if(! Session.isValid(session.get())) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session.");

			request.attribute("user", session.get().user);
		});
	}

	/**
	 * Register movie services, like movie search and movie ID lookup
	 */
	public void moviesService() {
		Route searchRoute = (request, response) -> {
			Optional<String> queryParam = Optional.ofNullable(request.params("search_query"));
			String searchQuery = queryParam.orElse("").replaceAll("\\+", " ").trim();
			System.out.println("Searched for \""+searchQuery+"\"");
			int results = asIntOpt(request.queryParams("results")).orElse(20);
			int page = asIntOpt(request.queryParams("page")).orElse(1);
			if(searchQuery.equals("") || results == 0)
				return Collections.EMPTY_LIST;
			return AbstractModel.search(Movie.class, searchQuery, results, page);
		};
		jget("/movies/search/:search_query", searchRoute);
		jget("/movies/search/", searchRoute);
		jget("/movies/search", searchRoute);


		Route movieRoute = (request, response) -> {
			String movieId = request.params("id");
			System.out.println("Looked up movie with ID: "+movieId);
			return new Movie(new ObjectId(movieId)).load();
		};

		jget("/movies/:id", movieRoute);
		jget("/movies/:id/*", movieRoute);

		Route unimplemented = (request, response) -> {
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		};
		jget("/movies", unimplemented);
		jget("/movies/*", unimplemented);
	}

	/**
	 * Register friend services, like friend search and friend ID lookup
	 */
	public void friendsService() {
		Route friendSearchRoute = (request,response) -> {
			Optional<String> queryParam = Optional.ofNullable(request.params("search_query"));
			String searchQuery = queryParam.orElse("").replaceAll("\\+"," ").trim();
			System.out.println("Searched for \"" + searchQuery + "\"");
			int results = asIntOpt(request.queryParams("results")).orElse(20);
			int page = asIntOpt(request.queryParams("page")).orElse(1);
			if(searchQuery.equals("") || results == 0)
				return Collections.EMPTY_LIST;
			return AbstractModel.search(User.class, searchQuery, results, page);
		};

		jget("/friends/search/:search_query", friendSearchRoute);
		jget("/friends/search/", friendSearchRoute);
		jget("/friends/search", friendSearchRoute);

		Route friendRoute = (request, response) -> {
			String userId = request.params("id");
			System.out.println("Looked up user with ID: "+userId);
			return new User(new ObjectId(userId)).load();
		};

		jget("/friends/:id", friendRoute);
		jget("/friends/:id/*", friendRoute);



		jget("/friends", (request, response) -> {
			/** return the user's friends */
			User u = request.attribute("user");
			if(u==null) return Collections.EMPTY_LIST;
			Optional<User> user = u.load(User.class);
			if(user.isPresent()) {
				return user.get().friends;
			}
			return Collections.EMPTY_LIST;
		});

		Route unimplemented = (request, response) -> {
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		};

		/* let us add some friends :) */
		jpost("/friends", unimplemented);

		/* delete some friends */
		jdelete("/friends/:id", unimplemented);
	}

	/**
	 * Register group services, like group search and group ID lookup
	 */
	public void groupsService() {
		jget("/groups/search/:search_query", (request, response) -> {
			String searchQuery = request.params("search_query").replaceAll("\\+", " ");
			System.out.println("Searched for \""+searchQuery+"\"");
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		});

		Route groupsRoute = (request, response) -> {
			String movieId = request.params("id");
			System.out.println("Looked up movie with ID: "+movieId);
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		};
		jget("/groups/:id", groupsRoute);
		jget("/groups/:id/*", groupsRoute);

		Route unimplemented = (request, response) -> {
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		};
		Route userGroups = (request, response) -> {
			if(request==null) { return Collections.EMPTY_LIST; }
			User u = request.attribute("user");
			Optional<User> user = u.load(User.class);
			if(user.isPresent()){
				return user.get().groups;
			}
			return Collections.EMPTY_LIST;
		};

		jget("/groups", userGroups);
		jget("/groups/", userGroups);
		jget("/groups/*", userGroups);
	}
}
