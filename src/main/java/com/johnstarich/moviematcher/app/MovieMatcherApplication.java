package com.johnstarich.moviematcher.app;

import com.johnstarich.moviematcher.models.*;
import org.bson.types.ObjectId;

import spark.Route;
import spark.Spark;
import java.util.ArrayList;
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
		Spark.get("/*", new ServeStaticFileRoute("/index.html"));
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
					path.equals("/robots.txt") ||
					path.startsWith("/favicon")
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

	/**
	 * Register movie services, like movie search and movie ID lookup
	 */
	public void moviesService() {
		AuthenticatedRoute searchRoute = (request, response, user) -> {
			Optional<String> queryParam = Optional.ofNullable(request.params("search_query"));
			String searchQuery = queryParam.orElse("").replaceAll("\\+", " ").trim();
			System.out.println("Searched for \""+searchQuery+"\"");
			int results = asIntOpt(request.queryParams("results")).orElse(20);
			int page = asIntOpt(request.queryParams("page")).orElse(1);
			if(searchQuery.equals("") || results == 0)
				return Collections.EMPTY_LIST;

			List<Movie> movies = AbstractModel.search(Movie.class, searchQuery, results, page);
			Map<ObjectId, Integer> ratingsMap = new HashMap<>();

			for(Movie m: movies) {
				Optional<Rating> r = Rating.loadRatingByUser(user.id, m.id);
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
		AuthenticatedRoute friendSearchRoute = (request, response, user) -> {
			Optional<String> queryParam = Optional.ofNullable(request.params("search_query"));
			String searchQuery = queryParam.orElse("").replaceAll("\\+"," ").trim();
			System.out.println("Searched for \"" + searchQuery + "\"");
			int results = asIntOpt(request.queryParams("results")).orElse(20);
			int page = asIntOpt(request.queryParams("page")).orElse(1);
			return AbstractModel.search(User.class, searchQuery, results, page)
				.parallelStream()
				.filter(resultUser ->
					! user.username.equals(resultUser.username) &&
					user.friends
						.parallelStream()
						.noneMatch(friend -> friend.username.equals(resultUser.username))
				)
				.map(User::noPassword)
				.collect(Collectors.toList());
		};

		jget("/friends/search/:search_query", friendSearchRoute);
		jget("/friends/search/", friendSearchRoute);
		jget("/friends/search", friendSearchRoute);

		Route friendRoute = (request, response) -> {
			String userId = request.params("id");
			System.out.println("Looked up user with ID: "+userId);
			Optional<User> user = new User(new ObjectId(userId)).load();
			if(! user.isPresent()) throw new HttpException(HttpStatus.NOT_FOUND, "User not found");
			return user.get().noPassword();
		};

		jget("/friends/:id", friendRoute);
		jget("/friends/:id/*", friendRoute);

		AuthenticatedRoute displayFriends = (request, response, user) -> {
			/* return the user's friends */
			if(user.friends == null ) return Collections.EMPTY_LIST;
			return user.friends
				.parallelStream()
				.map(User::noPassword)
				.collect(Collectors.toList());
		};

		jget("/friends", displayFriends);
		jget("/friends/", displayFriends);

		AuthenticatedRoute addFriend = (request, response, user) -> {
			Optional<String> potentialFriend = bodyParam(request, "newFriend_id");
			if(! potentialFriend.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No potential friend provided.");
			Optional<User> newFriendOpt = new User(new ObjectId(potentialFriend.get())).load();
			if(! newFriendOpt.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No potential friend user name provided.");
			User newFriend = newFriendOpt.get();
			if(user.equals(newFriend)) throw new HttpException(HttpStatus.BAD_REQUEST, "Sorry, can't add yourself as friend.");
			if(user.friends != null && user.friends.parallelStream().anyMatch(Predicate.isEqual(newFriend))) {
				throw new HttpException(HttpStatus.BAD_REQUEST, "Already friends.");
			}
			user.addFriend(newFriend).save();
			return "Congrats! You are now friends with " + newFriend.username;
		};

		/* let us add some friends :) */
		jpost("/friends", addFriend);
		jpost("/friends/", addFriend);

		AuthenticatedRoute removeFriend = (request, response, user) -> {
			String userId = request.params("id");
			Optional<User> removeUser = new User(new ObjectId(userId)).load();
			if(! removeUser.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No user with that id found");
			if(user.id.equals(new ObjectId(userId))) throw new HttpException(HttpStatus.BAD_REQUEST, "Can't delete yourself, sorry bud.");
			user.removeFriend(removeUser.get()).removeFriendFromGroups(removeUser.get()).save();
			return "Success, you are no longer friends with " + removeUser.get().username;
		};

		/* delete some friends */
		jdelete("/friends/:id", removeFriend);
		jdelete("/friends/:id/", removeFriend);
	}

	/**
	 * Register group services, like group search and group ID lookup
	 */
	public void groupsService() {

		AuthenticatedRoute addGroup = (request, response, user) ->{
			Optional<String> group_name = bodyParam(request, "group_name");
			if(! group_name.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No group name provided");
			String groupName = group_name.get();
			if(user.groups == null) {
				Group g = new Group(null, groupName).save();
				user.addGroup(g).save();
				return g.id;
			}
			for(Group existingGroup : user.groups){
				// does group already exist
				if(existingGroup.name.equals(groupName)){
					throw new HttpException(HttpStatus.BAD_REQUEST, groupName + " already exists");
				}
			}
			// create new group, save group, add to user, and save changes to user
			Group g = new Group(null, groupName).save();
			user.addGroup(g).save();
			return g.id;
		};
		jpost("/groups", addGroup);
		jpost("/groups/", addGroup);

		jget("/groups/search/:search_query", (request, response) -> {
			String searchQuery = request.params("search_query").replaceAll("\\+", " ");
			System.out.println("Searched for \""+searchQuery+"\"");
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		});

		Route idRoute = (request, response) -> {
			String group_id = request.params("id");
			System.out.println("Looked up group with ID: "+group_id);
			Optional<Group> result = new Group(new ObjectId(group_id)).load();
			if(! result.isPresent()) throw new HttpException(HttpStatus.NOT_FOUND, "Group not found with ID: "+group_id);

			return result.get().noPasswords();
		};
		jget("/groups/:id", idRoute);

		AuthenticatedRoute userGroups = (request, response, user) -> {
			if(request==null) { return Collections.EMPTY_LIST; }

			Map<String, List<User>> groupsMap = new HashMap<>();
			Map<String, Object> ret = new HashMap<>();

			if(user.groups == null) {
				groupsMap.put("", new ArrayList<>(0));
				ret.put("groups", new ArrayList<>(0));
			} else {
				for (Group g : user.groups) {
					groupsMap.put(g.name, g.members);
				}
				ret.put("groups", user.groups);
			}
			ret.put("members", groupsMap);
			return ret;
		};
		jget("/groups", userGroups);
		jget("/groups/", userGroups);

		AuthenticatedRoute userSubGroup = (request, response, user) -> {
			String group_name = request.params("group_name");
			return user.getFriendsToAdd(group_name);
		};
		jget("/groups/:group_name/user", userSubGroup);
		jget("/groups/:group_name/user/", userSubGroup);

		AuthenticatedRoute addUserToGroup = (request, response, user) -> {
			// friend to add to group
			Optional<String> username = bodyParam(request, "username");
			Optional<User> friend = User.loadByUsername(username.get());
			if(! friend.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, username.get() + " does not exist");

			// don't add self to group
			if(user.username.equals(friend.get().username)) throw new HttpException(HttpStatus.BAD_REQUEST, "Cannot add self to group");

			// group to add to
			String groupName = request.params("group_name");
			if(groupName == null) throw new HttpException(HttpStatus.BAD_REQUEST, "No group name provided");

			// attempt to add to group
			user = user.addFriendToGroup(groupName, friend.get()).save();

			return user.groups.parallelStream().filter(group -> group.name.equals(groupName)).findFirst().get().id;
		};

		jpost("/groups/:group_name/user", addUserToGroup);
		jpost("/groups/:group_name/user/", addUserToGroup);

		jdelete("/groups/:group_id", (request, response, user) -> {
			String groupId = request.params("group_id");
			Optional<Group> groupToRemoveOpt = new Group(new ObjectId(groupId)).load();
			if(! groupToRemoveOpt.isPresent()) throw new HttpException(HttpStatus.NOT_FOUND, "Group not found with ID: "+groupId);
			user.removeGroup(groupToRemoveOpt.get()).save();
			return "Success! Group successfully deleted.";
		});

		jdelete("/groups/:group_id/:member_id", (request, response, user) -> {
			String groupId = request.params("group_id");
			String memberId = request.params("member_id");
			Optional<Group> groupToRemoveMemberOpt = new Group(new ObjectId(groupId)).load();
			if(! groupToRemoveMemberOpt.isPresent()) throw new HttpException(HttpStatus.NOT_FOUND, "Group not found with ID: "+groupId);
			groupToRemoveMemberOpt.get().removeFriendWithId(new ObjectId(memberId)).save();
			return "Success! Group member successfully removed.";
		});

		AuthenticatedRoute recommendationList = (request, response, user) -> {
			// group to generate list for
			String groupName = request.params("group_name");
			if(groupName == null) throw new HttpException(HttpStatus.BAD_REQUEST, "No group name provided");
			Optional<Group> g = user.findGroup(groupName);
			if(! g.isPresent()) { throw new HttpException(HttpStatus.BAD_REQUEST, "Could not find "+groupName); }
			return g.get().suggestMovies(user);
		};

		jget("/groups/:group_name/recommendations", recommendationList);
		jget("/groups/:group_name/recommendations/", recommendationList);
	}
}
