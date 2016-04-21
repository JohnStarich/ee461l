package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.HttpException;
import com.johnstarich.moviematcher.app.HttpStatus;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;



/**
 * Created by Josue on 4/7/2016.
 */
public class User extends AbstractModel<User> {
	/* need to decide how to store user preferences ... */

	public final String email;
	public final String first_name;
	public final String last_name;
	public final List<User> friends;
	public final List<Group> groups;
	public final String password;

	public User(ObjectId id) {
		super(User.class, id);
		this.email = null;
		this.first_name = null;
		this.last_name = null;
		this.friends = new ArrayList<>(0);
		this.groups = new ArrayList<>(0);
		this.password = null;
	}

	private User(ObjectId id, String email, String first_name, String last_name, List<User> friends, List groups, String password) {
		super(User.class, id);
		this.email = email;
		this.first_name = first_name;
		this.last_name = last_name;
		this.friends = friends;
		this.groups = groups;
		this.password = password;
	}

	private User(ObjectId id, String email, String first_name, String last_name, String password) {
		super(User.class, id);
		this.email = email;
		this.first_name = first_name;
		this.last_name = last_name;
		this.friends = new ArrayList<>(0);
		this.groups = new ArrayList<>(0);
		this.password = password;
	}

	public User(ObjectId id, String email, String first_name, String last_name) {
		super(User.class, id);
		this.email = email;
		this.first_name = first_name;
		this.last_name = last_name;
		this.friends = new ArrayList<>(0);
		this.groups = new ArrayList<>(0);
		this.password = null;
	}

	@Override
	public boolean equals(Object o) {
		return o == this || o instanceof User && ((User) o).id == id && ((User) o).email.equals(email);
	}

	@Override
	public String toString() {
		return id + " " + email + " " + first_name + " " + last_name;
	}

	public User register(String password) throws HttpException {
		if(this.exists()) {
			throw new HttpException(HttpStatus.BAD_REQUEST);
		}

		User u = new User(id, email, first_name, last_name, friends, groups, BCrypt.hashpw(password, BCrypt.gensalt()));
		return u.save();
	}

	public User resetPassword(String oldPassword, String newPassword) throws HttpException {
		if(password == null) {
			throw new HttpException(HttpStatus.BAD_REQUEST);
		}
		if(! BCrypt.checkpw(oldPassword, password)) {
			throw new HttpException(HttpStatus.BAD_REQUEST);
		}

		User u = new User(id, email, first_name, last_name, friends, groups, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
		return u.save();
	}

	public static User loadByUsername(String email) {
		return MovieMatcherDatabase.morphium.createQueryFor(User.class).f("email").eq(email).get();
	}

	public User addFriend(User friend) {
		ArrayList<User> friends = new ArrayList<>(this.friends);
		friends.add(friend);
		return new User(id, email, first_name, last_name, friends, groups, password);
	}

	public User addFriends(Collection<User> friends) {
		ArrayList<User> amigos = new ArrayList<>(this.friends);
		amigos.addAll(friends);
		return new User(id, email, first_name, last_name, amigos, groups, password);
	}

	public User removeFriend(User oldFriend) {
		ArrayList<User> newFriends = new ArrayList<>(this.friends);
		newFriends.remove(oldFriend);
		return new User(id, email, first_name, last_name, newFriends, groups, password);
	}

	public User removeFriends(Collection<User> oldFriends) {
		ArrayList<User> newFriends = new ArrayList<>(this.friends);
		newFriends.removeAll(oldFriends);
		return new User(id, email, first_name, last_name, newFriends, groups, password);
	}

	public User addGroup(Group group) {
		ArrayList<Group> groups = new ArrayList<>(this.groups);
		groups.add(group);
		return new User(id, email, first_name, last_name, friends, groups, password);
	}

	public User addGroups(Collection<Group> groups) {
		ArrayList<Group> newGroups = new ArrayList<>(this.groups);
		newGroups.addAll(groups);
		return new User(id, email, first_name, last_name, friends, newGroups, password);
	}

	public User removeGroup(Group group) {
		ArrayList<Group> groups = new ArrayList<>(this.groups);
		groups.remove(group);
		return new User(id, email, first_name, last_name, friends, groups, password);
	}

	public User removeGroups(Collection<Group> groups) {
		ArrayList<Group> newGroups = new ArrayList<>(this.groups);
		newGroups.removeAll(groups);
		return new User(id, email, first_name, last_name, friends, newGroups, password);
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

		return new User(id, email, first_name, last_name, friends, groups, password);
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

		return new User(id, email, first_name, last_name, friends, groups, password);
	}

	// this.login() {}  need to implement this method also
}
