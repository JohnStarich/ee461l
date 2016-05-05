package com.johnstarich.moviematcher.app;

import com.johnstarich.moviematcher.models.*;
import org.bson.types.ObjectId;

import spark.Route;
import spark.Spark;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
			return newUser.register(password.get()).noPassword();
		});

		jget("/users", (request, response) -> {
			Optional<String> session_id = Optional.ofNullable(request.headers("Authorization"));
			if(! session_id.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");

			Optional<Session> session = new Session(new ObjectId(session_id.get()), null).load(Session.class);
			if(! session.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");

			return session.get().user.noPassword();
		});

		jpatch("/users", (request, response) -> {
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

		jpost("/users/ratings", (request, response) -> {
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

			Optional<Session> session = new Session(new ObjectId(authorization.get().trim()), null).load(Session.class);
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

			User u = request.attribute("user");
			List<Movie> movies = AbstractModel.search(Movie.class, searchQuery, results, page);
			Map<ObjectId, Integer> ratingsMap = new HashMap<>();

			for(Movie m: movies) {
				Optional<Rating> r = Rating.loadRatingByUser(u.id, m.id);
				if(r.isPresent()) {
					ratingsMap.put(m.id, r.get().numeric_rating);
				}
				else {
					ratingsMap.put(m.id, null);
				}
			}

			Map<String, Object> ret = new HashMap<>();
			ret.put("movies", movies);
			ret.put("ratings", ratingsMap);
			return ret;
		};

		jget("/movies/search/:search_query", searchRoute);
		jget("/movies/search/", searchRoute);
		jget("/movies/search", searchRoute);

		Route movieRoute = (request, response) -> {
			String movieId = request.params("id");
			System.out.println("Looked up movie with ID: "+movieId);
			return new Movie(new ObjectId(movieId)).load(Movie.class);
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
			User u = request.attribute("user");
			Optional<User> user = u.load(User.class);
			if(! user.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid Session");
			if(searchQuery.equals("") || results == 0 || searchQuery.equals(user.get().username)
				|| searchQuery.equals(user.get().first_name) || searchQuery.equals(user.get().last_name))
				return Collections.EMPTY_LIST;
			return AbstractModel.search(User.class, searchQuery, results, page)
				.parallelStream()
				.map(User::noPassword)
				.collect(Collectors.toList());
		};

		jget("/friends/search/:search_query", friendSearchRoute);
		jget("/friends/search/", friendSearchRoute);
		jget("/friends/search", friendSearchRoute);

		Route friendRoute = (request, response) -> {
			String userId = request.params("id");
			System.out.println("Looked up user with ID: "+userId);
			Optional<User> user = new User(new ObjectId(userId)).load(User.class);
			if(! user.isPresent()) throw new HttpException(HttpStatus.NOT_FOUND, "User not found");
			return user.get().noPassword();
		};

		jget("/friends/:id", friendRoute);
		jget("/friends/:id/*", friendRoute);


		Route displayFriends = (request, response) -> {
			/* return the user's friends */
			User u = request.attribute("user");
			if(u==null) return Collections.EMPTY_LIST;
			Optional<User> user = u.load(User.class);
			if(user.isPresent()) {
				if(user.get().friends == null ) return Collections.EMPTY_LIST;
				return user.get().friends
					.parallelStream()
					.map(User::noPassword)
					.collect(Collectors.toList());
			}
			return Collections.EMPTY_LIST;
		};

		jget("/friends", displayFriends);
		jget("/friends/", displayFriends);

		Route unimplemented = (request, response) -> {
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		};

		Route addFriend = (request, response) -> {
				Optional<String> potentialFriend = bodyParam(request, "newFriend_id");
				if(! potentialFriend.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No potential friend provided.");
				Optional<User> newFriend = new User(new ObjectId(potentialFriend.get())).load(User.class);
				if(! newFriend.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No potential friend user name provided.");
				User u = request.attribute("user");
				Optional<User> user = u.load(User.class);
				if(! user.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session.");
				User currentuser = user.get().noPassword();
				User nf = newFriend.get();
				if(currentuser.friends == null) {
					currentuser = currentuser.addFriend(nf).save();
					return "Congrats! You are now friends with " + nf.username;
				} else {
					if(currentuser.friends.parallelStream().anyMatch(Predicate.isEqual(nf)))
						throw new HttpException(HttpStatus.BAD_REQUEST, "Already friends.");
					currentuser = currentuser.addFriend(nf).save();
					return "Congrats! You are now friends with " + nf.username;
				}
			};

		/* let us add some friends :) */
		jpost("/friends", addFriend);
		jpost("/friends/", addFriend);

		Route removeFriend = (request, response) -> {
			String userId = request.params("id");
			Optional<User> removeUser = new User(new ObjectId(userId)).load(User.class);
			if(! removeUser.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No user with that id found");
			User u = request.attribute("user");
			Optional<User> user = u.load(User.class);
			if(! user.isPresent()) throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid Session");
			if(user.get().id.equals(new ObjectId(userId))) throw new HttpException(HttpStatus.BAD_REQUEST, "Can't delete yourself, sorry bud.");
			user.get().removeFriend(removeUser.get()).removeFriendFromGroups(removeUser.get()).save();
			return "Succes, you are no longer friends with " + removeUser.get().username;
		};

		/* delete some friends */
		jdelete("/friends/:id", removeFriend);
		jdelete("/friends/:id/", removeFriend);
	}

	/**
	 * Register group services, like group search and group ID lookup
	 */
	public void groupsService() {

		Route addGroup = (request, response) ->{
			Optional<String> group_name = bodyParam(request, "group_name");
			if(! group_name.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No group name provided");
			String groupName = group_name.get();
			User u = request.attribute("user");
			Optional<User> user = u.load(User.class);
			if(user.isPresent()) {
				if(user.get().groups == null) {
					Group g = new Group(null, groupName).save();
					user.get().addGroup(g).save();
					return g.id;
				}
				for(Group existingGroup : user.get().groups){
					// does group already exist
					if(existingGroup.name.equals(groupName)){
						throw new HttpException(HttpStatus.BAD_REQUEST, groupName + " already exists");
					}
				}
				// create new group, save group, add to user, and save changes to user
				Group g = new Group(null, groupName).save();
				user.get().addGroup(g).save();
				return g.id;
			}
			throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");
		};
		jpost("/groups", addGroup);
		jpost("/groups/", addGroup);

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

		Route idRoute = (request, response) -> {
			String group_id = request.params("id");
			System.out.println("Looked up group with ID: "+group_id);
			return new Group(new ObjectId(group_id)).load(Group.class);
		};
		jget("/groups/:id", idRoute);

		Route userGroups = (request, response) -> {
			if(request==null) { return Collections.EMPTY_LIST; }
			User u = request.attribute("user");
			Optional<User> user = u.load(User.class);

			if(user.isPresent()) {
				Map<String, List<User>> groupsMap = new HashMap<>();
				for(Group g : user.get().groups){
					groupsMap.put(g.name, g.members);
				}
				Map<String, Object> ret = new HashMap<>();
				ret.put("groups", user.get().groups);
				ret.put("members", groupsMap);
				return ret;
			}
			return Collections.EMPTY_LIST;
		};
		jget("/groups", userGroups);
		jget("/groups/", userGroups);

		Route userSubGroup = (request, response) -> {
			String group_name = request.params("group_name");
			User u = request.attribute("user");
			Optional<User> user = u.load(User.class);
			if(user.isPresent()){
				return user.get().getFriendsToAdd(group_name);
			}
			throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session");
		};
		jget("/groups/:group_name/user", userSubGroup);
		jget("/groups/:group_name/user/", userSubGroup);

		Route addUserToGroup = (request, response) -> {
			// self
			User u = request.attribute("user");
			Optional<User> self = u.load(User.class);
			if(! self.isPresent()){throw new HttpException(HttpStatus.UNAUTHORIZED, "Invalid session"); }

			// friend to add to group
			Optional<String> username = bodyParam(request, "username");
			Optional<User> friend = User.loadByUsername(username.get());
			if(! friend.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, username.get() + " does not exist");

			// don't add self to group
			if(self.get().username.equals(friend.get().username)) throw new HttpException(HttpStatus.BAD_REQUEST, "Cannot add self to group");

			// group to add to
			String groupName = request.params("group_name");
			if(groupName == null) throw new HttpException(HttpStatus.BAD_REQUEST, "No group name provided");

			// attempt to add to group
			u = self.get().addFriendToGroup(groupName, friend.get()).save();

			return u.groups.parallelStream().filter(group -> group.name.equals(groupName)).findFirst().get().id;
		};

		jpost("/groups/:group_name/user", addUserToGroup);
		jpost("/groups/:group_name/user/", addUserToGroup);
	}
}
