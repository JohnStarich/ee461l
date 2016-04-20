package com.johnstarich.moviematcher.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.johnstarich.moviematcher.app.HttpException;
import com.johnstarich.moviematcher.app.HttpStatus;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Josue on 4/7/2016.
 */
public class User {
	private static class LazyUsersCollection {
		public static final MongoCollection<Document> usersCollection = MovieMatcherDatabase.getCollection("users");
	}
	private static MongoCollection<Document> getCollection() { return LazyUsersCollection.usersCollection; }

	/* need to decide how to store user preferences ... */

	private static Gson gson = new GsonBuilder().create();

	public final ObjectId _id;
	public final String email;
	public final String first_name;
	public final String last_name;
	public final List<User> friends;
	public final List<Group> groups;
	public final String password;

	public User(ObjectId _id) {
		this._id = _id;
		this.email = null;
		this.first_name = null;
		this.last_name = null;
		this.friends = new ArrayList<>(0);
		this.groups = new ArrayList<>(0);
		this.password = null;
	}

	private User(ObjectId _id, String email, String first_name, String last_name, List<User> friends, List groups, String password) {
		this._id = _id;
		this.email = email;
		this.first_name = first_name;
		this.last_name = last_name;
		this.friends = friends;
		this.groups = groups;
		this.password = password;
	}

	private User(ObjectId _id, String email, String first_name, String last_name, String password) {
		this._id = _id;
		this.email = email;
		this.first_name = first_name;
		this.last_name = last_name;
		this.friends = new ArrayList<>(0);
		this.groups = new ArrayList<>(0);
		this.password = password;
	}

	public User(ObjectId _id, String email, String first_name, String last_name) {
		this._id = _id;
		this.email = email;
		this.first_name = first_name;
		this.last_name = last_name;
		this.friends = new ArrayList<>(0);
		this.groups = new ArrayList<>(0);
		this.password = null;
	}

	@Override
	public boolean equals(Object o) {
		return o == this || o instanceof User && ((User) o)._id == _id && ((User) o).email == email;
	}

	@Override
	public int hashCode() {
		return _id.hashCode();
	}

	@Override
	public String toString() {
		return _id + " " + email + " " + first_name + " " + last_name;
	}

	public User load() throws HttpException {
		Document user = getCollection().find(eq("_id", _id)).first();
		if(user.isEmpty()) {
			throw new HttpException(HttpStatus.NOT_FOUND, "This User " + this.toString() + " has not been registered.");
		}
		return gson.fromJson(user.toJson(), User.class);
	}

	private boolean exists() {
		Document user = getCollection().find(eq("_id", _id)).first();
		return ! user.isEmpty();
	}

	/* not sure how to add friends or groups to mongoDB since they are lists*/
	/* will create the collection, once we insert our first user */
	private User insert() {
		getCollection().insertOne(
			new Document("_id", _id)
				.append("email" , email)
				.append("first_name" , first_name)
				.append("last_name", last_name)
				.append("password", password)
				.append("friends", friends)
				.append("groups", groups)
		);
		return this;
	}

	public User register(String password) throws HttpException {
		if(this.exists()) {
			throw new HttpException(HttpStatus.BAD_REQUEST);
		}

		User u = new User(_id, email, first_name, last_name, friends, groups, BCrypt.hashpw(password, BCrypt.gensalt()));
		return u.insert();
	}

	private User updatePassword() {
		getCollection().findOneAndUpdate(
			new Document("_id", _id),
			new Document("$set", new Document("password", password))
		);
		return this;
	}

	public User resetPassword(String oldPassword, String newPassword) throws HttpException {
		if(password == null) {
			throw new HttpException(HttpStatus.BAD_REQUEST);
		}
		if(! BCrypt.checkpw(oldPassword, password)) {
			throw new HttpException(HttpStatus.BAD_REQUEST);
		}

		User u = new User(_id, email, first_name, last_name, friends, groups, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
		return u.updatePassword();
	}


	private User updateFriends() {
		getCollection().findOneAndUpdate(
			new Document("_id", _id),
			new Document("$set", new Document("friends", friends))
		);
		return this;
	}

	private User updateGroups() {
		getCollection().findOneAndUpdate(
			new Document("_id", _id),
			new Document("$set", new Document("groups", groups))
		);
		return this;
	}

	public static List<User> search(String query) {
		return search(query, 20, 1);
	}

	public static List<User> search(String query, int results, int page) {
		AggregateIterable<Document> iterable = getCollection().aggregate(
			asList(
				new Document("$match", new Document("$text", new Document("$search", query))),
				new Document(
					"$project",
					new Document("email", true)
						.append("first_name", true)
						.append("last_name", true)
						.append("friends", true)
						.append("groups", true)
						.append("password", true)
						.append("score", new Document("$meta", "textScore"))
				),
				new Document("$sort", new Document("score",-1)),
				new Document("$limit", results),
				new Document("$skip", results * (page - 1))
			)
		);

		ArrayList<User> queryResults = new ArrayList<User>();

		iterable
			.map(document -> gson.fromJson(document.toJson(), User.class))
			.forEach((Block<User>) queryResults::add);

		return queryResults;
	}

	public static User loadByUsername(String email) {
		Document user = getCollection().find(eq("email", email)).first();
		return gson.fromJson(user.toJson(), User.class);
	}

	public User delete() throws HttpException {
		DeleteResult deleteResult = getCollection().deleteOne(new Document("_id", _id));
		if(deleteResult.getDeletedCount() == 1) {
			return this;
		}
		else {
			throw new HttpException(HttpStatus.NOT_FOUND);
		}
	}

	public User addFriend(User friend) {
		ArrayList<User> friends = new ArrayList<>(this.friends);
		friends.add(friend);
		return new User(_id, email, first_name, last_name, friends, groups, password);
	}

	public User addFriends(Collection<User> friends) {
		ArrayList<User> amigos = new ArrayList<>(this.friends);
		amigos.addAll(friends);
		return new User(_id, email, first_name, last_name, amigos, groups, password);
	}

	public User removeFriend(User oldFriend) {
		ArrayList<User> newFriends = new ArrayList<>(this.friends);
		newFriends.remove(oldFriend);
		return new User(_id, email, first_name, last_name, newFriends, groups, password);
	}

	public User removeFriends(Collection<User> oldFriends) {
		ArrayList<User> newFriends = new ArrayList<>(this.friends);
		newFriends.removeAll(oldFriends);
		return new User(_id, email, first_name, last_name, newFriends, groups, password);
	}

	public User addGroup(Group group) {
		ArrayList<Group> groups = new ArrayList<>(this.groups);
		groups.add(group);
		return new User(_id, email, first_name, last_name, friends, groups, password);
	}

	public User addGroups(Collection<Group> groups) {
		ArrayList<Group> newGroups = new ArrayList<>(this.groups);
		newGroups.addAll(groups);
		return new User(_id, email, first_name, last_name, friends, newGroups, password);
	}

	public User removeGroup(Group group) {
		ArrayList<Group> groups = new ArrayList<>(this.groups);
		groups.remove(group);
		return new User(_id, email, first_name, last_name, friends, groups, password);
	}

	public User removeGroups(Collection<Group> groups) {
		ArrayList<Group> newGroups = new ArrayList<>(this.groups);
		newGroups.removeAll(groups);
		return new User(_id, email, first_name, last_name, friends, newGroups, password);
	}

	public User removeFriendFromGroup(String groupName, User member) throws HttpException {
		if(groupName == null || member == null) {
			throw new HttpException(HttpStatus.BAD_REQUEST);
		}

		if(friends.parallelStream().noneMatch(Predicate.isEqual(member))) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "You are not friends with "+member);
		}

		Optional<Group> editThisGroupOptional = groups.parallelStream()
				.filter(group -> group.name.equals(groupName))
				.findFirst();

		if(! editThisGroupOptional.isPresent()) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "Could not find " + first_name + "'s group named " + groupName +".");
		}

		Group editThisGroup = editThisGroupOptional.get();

		groups.remove(editThisGroup);
		groups.add(editThisGroup.removeFriend(member));

		return new User(_id, email, first_name, last_name, friends, groups, password);
	}

	public User addFriendToGroup(String groupName, User newMember) throws HttpException{
		if(groupName == null || newMember == null) {
			throw new HttpException(HttpStatus.BAD_REQUEST);
		}

		if(friends.parallelStream().noneMatch(Predicate.isEqual(newMember))) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "You are not friends with "+newMember);
		}

		Optional<Group> editThisGroupOptional = groups.parallelStream()
				.filter(group -> group.name.equals(groupName))
				.findFirst();

		if(! editThisGroupOptional.isPresent()) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "Could not find " + first_name + "'s group named " + groupName +".");
		}

		Group editThisGroup = editThisGroupOptional.get();

		groups.remove(editThisGroup);
		groups.add(editThisGroup.addFriend(newMember));

		return new User(_id, email, first_name, last_name, friends, groups, password);
	}

	// this.login() {}  need to implement this method also
}
