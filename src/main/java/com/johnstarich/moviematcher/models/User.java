package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.HttpException;
import com.johnstarich.moviematcher.app.HttpStatus;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import de.caluga.morphium.annotations.Index;
import de.caluga.morphium.annotations.Reference;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Josue on 4/7/2016.
 */
@Index("username:text,first_name:text,last_name:text")
public class User extends AbstractModel<User> {
	@Index(options={"unique", "weight:5"})
	public final String username;
	public final String first_name;
	public final String last_name;
	@Reference
	public final List<User> friends;
	@Reference
	public final List<Group> groups;
	public final String password;

	public User(ObjectId id) {
		super(User.class, id);
		this.username = null;
		this.first_name = null;
		this.last_name = null;
		this.friends = null;
		this.groups = null;
		this.password = null;
	}

	private User(ObjectId id, String username, String first_name, String last_name, List<User> friends, List<Group> groups, String password) {
		super(User.class, id);
		this.username = username;
		this.first_name = first_name;
		this.last_name = last_name;
		this.friends = friends;
		this.groups = groups;
		this.password = password;
	}

	private User(ObjectId id, String username, String first_name, String last_name, String password) {
		super(User.class, id);
		this.username = username;
		this.first_name = first_name;
		this.last_name = last_name;
		this.friends = null;
		this.groups = null;
		this.password = password;
	}

	public User(ObjectId id, String username, String first_name, String last_name) {
		super(User.class, id);
		this.username = username;
		this.first_name = first_name;
		this.last_name = last_name;
		this.friends = null;
		this.groups = null;
		this.password = null;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof User)) return false;
		if(o == this) return true;
		if( ((User) o).id == null || ((User) o).username == null) return false;
		if( ((User) o).id.equals(id) && ((User) o).username.equals(username) ) return true;
		else return false;
	}

	@Override
	public String toString() {
		return id + " " + username + " " + first_name + " " + last_name;
	}

	public User register(String password) throws HttpException {
		if(this.exists()) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "User with this username already exists.");
		}

		User u = new User(id, username, first_name, last_name, friends, groups, BCrypt.hashpw(password, BCrypt.gensalt()));
		return u.save();
	}

	public User resetPassword(String oldPassword, String newPassword) throws HttpException {
		if(password == null) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "No previous password set.");
		}
		if(! BCrypt.checkpw(oldPassword, password)) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "Invalid old password.");
		}

		User u = new User(id, null, null, null, null, null, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
		return u.update();
	}

	public static Optional<User> loadByUsername(String username) {
		List<User> users = MovieMatcherDatabase.morphium.createQueryFor(User.class).f("username").eq(username).limit(1).asList();
		if(users.size() == 0 || users.get(0) == null) {
			return Optional.empty();
		}
		return Optional.of(users.get(0));
	}

	public static Session login(String username, String password) throws HttpException {
		Optional<User> user = loadByUsername(username);
		// username does not exist
		if(! user.isPresent()) {
			throw new HttpException(HttpStatus.UNAUTHORIZED, "Bad username/password combination. Please try again.");
		}
		// passwords do not match
		if(! BCrypt.checkpw(password, user.get().password)) {
			throw new HttpException(HttpStatus.UNAUTHORIZED, "Bad username/password combination. Please try again.");
		}
		return new Session(null, user.get()).save();
	}

	public User addFriend(User friend) {
		ArrayList<User> friends;
		if(this.friends != null) friends = new ArrayList<>(this.friends);
		else friends = new ArrayList<>(1);
		friends.add(friend);
		return new User(id, username, first_name, last_name, friends, groups, password);
	}

	public User addFriends(Collection<User> friendsCollection) {
		ArrayList<User> friends;
		if(this.friends != null) friends = new ArrayList<>(this.friends);
		else friends = new ArrayList<>(friendsCollection.size());
		friends.addAll(friendsCollection);
		return new User(id, username, first_name, last_name, friends, groups, password);
	}

	public User removeFriend(User oldFriend) {
		ArrayList<User> newFriends;
		if(this.friends != null ) newFriends = new ArrayList<>(this.friends);
		else newFriends = new ArrayList<>(0);
		newFriends.remove(oldFriend);
		return new User(id, username, first_name, last_name, newFriends, groups, password);
	}

	public User removeFriends(Collection<User> oldFriends) {
		ArrayList<User> newFriends;
		if(this.friends != null) newFriends = new ArrayList<>(this.friends);
		else newFriends = new ArrayList<>(0);
		newFriends.removeAll(oldFriends);
		return new User(id, username, first_name, last_name, newFriends, groups, password);
	}

	public User addGroup(Group group) {
		ArrayList<Group> groups;
		if(this.groups != null) groups = new ArrayList<>(this.groups);
		else groups = new ArrayList<>(1);
		groups.add(group);
		return new User(id, username, first_name, last_name, friends, groups, password);
	}

	public User addGroups(Collection<Group> groups) {
		ArrayList<Group> newGroups;
		if(this.groups != null) newGroups = new ArrayList<>(this.groups);
		else newGroups = new ArrayList<>(groups.size());
		newGroups.addAll(groups);
		return new User(id, username, first_name, last_name, friends, newGroups, password);
	}

	public User removeGroup(Group group) {
		ArrayList<Group> groups;
		if(this.groups != null) groups = new ArrayList<>(this.groups);
		else groups = new ArrayList<>(0);
		groups.remove(group);
		return new User(id, username, first_name, last_name, friends, groups, password);
	}

	public User removeGroups(Collection<Group> groups) {
		ArrayList<Group> newGroups;
		if(this.groups != null) newGroups = new ArrayList<>(this.groups);
		else newGroups = new ArrayList<>(0);
		newGroups.removeAll(groups);
		return new User(id, username, first_name, last_name, friends, newGroups, password);
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

		return new User(id, username, first_name, last_name, friends, groups, password);
	}

	public User addFriendToGroup(String groupName, User newMember) throws HttpException{
		if(groupName == null || newMember == null) {
			throw new HttpException(HttpStatus.BAD_REQUEST);
		}

		if(friends.parallelStream().noneMatch(Predicate.isEqual(newMember))) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "You are not friends with "+ newMember.username);
		}

		Optional<Group> editThisGroupOptional = groups.parallelStream()
				.filter(group -> group.name.equals(groupName))
				.findFirst();

		if(! editThisGroupOptional.isPresent()) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "Could not find " + first_name + "'s group named " + groupName +".");
		}

		Group editThisGroup = editThisGroupOptional.get();
		if(editThisGroup.members.contains(newMember)) { throw new HttpException(HttpStatus.BAD_REQUEST, newMember.username + " is already in group"); }
		groups.remove(editThisGroup);


		/*I think save needs to be invoked here to save the changes in the group to the group collection*/
		groups.add(editThisGroup.addFriend(newMember).save());

		return new User(id, username, first_name, last_name, friends, groups, password);
	}



	public User noPassword() {
		return new User(id, username, first_name, last_name, friends, groups, null);
	}

	public User removeFriendFromGroups(User user) {
		if(groups == null) return this;

		List<Group> groupsContainUser = groups.parallelStream()
			.filter(group -> {
					if (group.members != null) return group.members.contains(user);
					return false;
				}
			)
			.collect(Collectors.toList());

		List<Group> groupUpdates = new ArrayList<>(this.groups);

		groupsContainUser.parallelStream().forEach(
			group -> {
				groupUpdates.remove(group);
				groupUpdates.add(group.removeFriend(user));
			}
		);

		return new User(id,username,first_name,last_name,friends,groupUpdates,password);
	}
	public Optional<Group> findGroup(String groupName) {
		return groups.parallelStream().filter(group -> group.name.equals(groupName)).findFirst();
	}

	public Optional<List<User>> getFriendsToAdd(String groupName) throws HttpException{
		if(groups == null) throw new HttpException(HttpStatus.BAD_REQUEST, "Could not find "+groupName);
		Optional<Group> g = groups.parallelStream().filter(group -> group.name.equals(groupName)).findFirst();
		if(! g.isPresent()) { throw new HttpException(HttpStatus.BAD_REQUEST, "Could not find "+groupName); }
		/** I want the users who are my friends and not in this group */
		/** these are members of the group , so return friends who are not in the member */
		if(friends == null) return Optional.of(new ArrayList<>(0));
		if(g.get().members == null) return Optional.of(new ArrayList<>(friends));
		return Optional.ofNullable(
					friends.parallelStream()
					.filter(friend -> g.get().members.parallelStream().noneMatch(Predicate.isEqual(friend)))
					.collect(Collectors.toList())
				);
	}

}

