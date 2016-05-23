package com.johnstarich.moviematcher.controllers;

import com.johnstarich.moviematcher.utils.HttpException;
import com.johnstarich.moviematcher.utils.HttpStatus;
import com.johnstarich.moviematcher.models.AbstractModel;
import com.johnstarich.moviematcher.models.User;
import com.johnstarich.moviematcher.routes.AuthenticatedRoute;
import org.bson.types.ObjectId;
import spark.Route;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Register friend services, like friend search and friend ID lookup
 * Created by johnstarich on 5/22/16.
 */
public class FriendService extends JsonService {
	@Override
	public String resource() {
		return "friends";
	}

	@Override
	public void initService() {
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

		jget("search/:search_query", friendSearchRoute);
		jget("search/", friendSearchRoute);
		jget("search", friendSearchRoute);

		Route friendRoute = (request, response) -> {
			String userId = request.params("id");
			System.out.println("Looked up user with ID: "+userId);
			Optional<User> user = new User(new ObjectId(userId)).load();
			if(! user.isPresent()) throw new HttpException(HttpStatus.NOT_FOUND, "User not found");
			return user.get().noPassword();
		};

		jget(":id", friendRoute);
		jget(":id/*", friendRoute);

		AuthenticatedRoute displayFriends = (request, response, user) -> {
			/* return the user's friends */
			if(user.friends == null ) return Collections.EMPTY_LIST;
			return user.friends
				.parallelStream()
				.map(User::noPassword)
				.collect(Collectors.toList());
		};

		jget(displayFriends);
		jget("/", displayFriends);

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
		jpost(addFriend);
		jpost("/", addFriend);

		AuthenticatedRoute removeFriend = (request, response, user) -> {
			String userId = request.params("id");
			Optional<User> removeUser = new User(new ObjectId(userId)).load();
			if(! removeUser.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No user with that id found");
			if(user.id.equals(new ObjectId(userId))) throw new HttpException(HttpStatus.BAD_REQUEST, "Can't delete yourself, sorry bud.");
			user.removeFriend(removeUser.get()).removeFriendFromGroups(removeUser.get()).save();
			return "Success, you are no longer friends with " + removeUser.get().username;
		};

		/* delete some friends */
		jdelete(":id", removeFriend);
		jdelete(":id/", removeFriend);
	}
}
