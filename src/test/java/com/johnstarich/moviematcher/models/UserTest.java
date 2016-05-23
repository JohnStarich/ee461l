package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.controllers.AbstractMongoDBTest;
import com.johnstarich.moviematcher.utils.HttpException;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by Josue on 4/21/2016.
 */
public class UserTest extends AbstractMongoDBTest {
	private User Josue = new User(new ObjectId(), "jalfaro@MovieMatcher.com", "Josue", "Alfaro");
	private User Jeremy = new User(new ObjectId(), "jcastillo@MovieMatcher.com", "Jeremy", "Castillo");
	private User John = new User(new ObjectId(), "jstarich@MovieMatcher.com", "John", "Starich");
	private User Cesar = new User(new ObjectId(), "2cgonzalez@MovieMatcher.com", "Cesar", "Gonzalez");

	public void testUserRegister() throws Exception {
		Josue = Josue.register("goodPassword!");
		Jeremy = Jeremy.register("betterPassword1");
		John = John.register("evenbetterPassword2");
		Cesar = Cesar.register("thebestPassword#3");

		assertTrue(Josue.exists());
		assertTrue(Jeremy.exists());
		assertTrue(John.exists());
		assertTrue(Cesar.exists());
	}

	public void testEncryption() throws Exception {
		Josue = Josue.register("goodPassword!");
		Jeremy = Jeremy.register("betterPassword1");
		John = John.register("evenbetterPassword2");
		Cesar = Cesar.register("thebestPassword#3");

		assertNotSame(Cesar.password, "thebestPassword#3");
		assertNotSame(John.password, "evenbetterPassword2");
		assertNotSame(Jeremy.password, "betterPassword1");
		assertNotSame(Josue.password, "goodPassword!");

		User test1 = new User(new ObjectId(), "blah@example.org", "Joe", "Shmoe");
		User test2 = new User(new ObjectId(), "blah2@example.org", "Joe", "Shmoe");
		assertNotSame(test1.register("same_Password1").password, test2.register("same_Password1").password);
	}

	public void testResetPassword() throws Exception {
		John = John.register("evenbetterPassword2");

		try {
			John = John.resetPassword("wrongPassword", "INeedBetterPassword@2013");
		} catch (HttpException e) {
			assertEquals("Invalid old password.", e.getMessage());
		}

		String johnsOldPsw = John.password;

		John = John.resetPassword("evenbetterPassword2", "INeedBetterPassword@2013");

		assertNotSame(johnsOldPsw, John.password);

	}

	public void testLoadByUserName() throws Exception {
		Jeremy = Jeremy.register("betterPassword1");

		Optional<User> u = User.loadByUsername("jcastillo@MovieMatcher.com");
		assertTrue(u.isPresent());
		assertEquals(u.get(), Jeremy);

		Optional<User> nonExisting = User.loadByUsername("aventura@MovieMatcher.com");
		assertFalse(nonExisting.isPresent());
	}

	public void testAddFriend() throws Exception {
		Josue = Josue.register("goodPassword!");
		Cesar = Cesar.register("thebestPassword#3");

		Josue = Josue.addFriend(Cesar);
		Optional<User> noNewFriendsJosue = Josue.load();
		assertNotSame(noNewFriendsJosue.get().friends, Josue.friends);

		Josue.save();
		Optional<User> newFriendJosue = Josue.load();

		assertEquals(newFriendJosue.get().friends, Josue.friends);
	}

	public void testAddFriends() throws Exception {
		Josue = Josue.register("goodPassword!");
		Jeremy = Jeremy.register("betterPassword1");
		John = John.register("evenbetterPassword2");
		Cesar = Cesar.register("thebestPassword#3");

		List<User> collectionOfFriends = new ArrayList<>(3);
		collectionOfFriends.add(Jeremy);
		collectionOfFriends.add(John);
		collectionOfFriends.add(Cesar);

		Josue = Josue.addFriends(collectionOfFriends);

		Josue.save();
		Optional<User> newFriends = Josue.load();

		assertEquals(newFriends.get().friends, Josue.friends);
	}

	public void testRemoveFriend() throws Exception {
		Josue = Josue.register("goodPassword!");
		Jeremy = Jeremy.register("betterPassword1");
		John = John.register("evenbetterPassword2");
		Cesar = Cesar.register("thebestPassword#3");

		Josue = Josue.addFriend(Cesar);

		List<User> oldFriend = Josue.friends;

		Josue = Josue.removeFriend(Cesar);
		assertNotSame(oldFriend, Josue.friends);

	}

	public void testAddFriendToGroup() throws Exception {
		Josue = Josue.register("goodPassword!");
		Jeremy = Jeremy.register("betterPassword1");
		John = John.register("evenbetterPassword2");
		Cesar = Cesar.register("thebestPassword#3");

		List<User> collectionOfFriends = new ArrayList<>(3);
		collectionOfFriends.add(Jeremy);
		collectionOfFriends.add(John);
		collectionOfFriends.add(Cesar);
		Group creators = new Group(null, "Creators", collectionOfFriends);

		John = John.addGroup(creators);

		John = John.addFriend(Josue);
		John = John.addFriendToGroup("Creators", Josue);

		assertTrue(John.groups.get(0).members.contains(Josue));
	}

	public void testEquals() throws Exception {
		User user1 = new User(new ObjectId(), "joe@hotmail.com", "Joe", "Shmoe").register("Password123");
		User user2 = new User(new ObjectId(), "billybob@hotmail.com", "Billy", "Bob").register("Password123");

		assertFalse(user1.equals(user2));
	}

	public void testGetFriendsToAdd() throws Exception {
		Josue = Josue.register("goodPassword!");
		Jeremy = Jeremy.register("betterPassword1");
		John = John.register("evenbetterPassword2");
		Cesar = Cesar.register("thebestPassword#3");
		User Jane = new User(null);
		Group zero = new Group(null, "zero");
		Josue = Josue.addGroup(zero);
		Optional<List<User>> potentialCandidates = Josue.getFriendsToAdd("zero");
		assertTrue(potentialCandidates.isPresent());
		List<User> candidates = potentialCandidates.get();
		assertEquals(0, candidates.size());
		Josue = Josue.addFriends(Arrays.asList(Jeremy, John, Cesar, Jane)).save();
		Group creators = new Group(null, "Creators", Arrays.asList(Jeremy, John, Cesar));
		Josue = Josue.addGroup(creators).save();
		Optional<List<User>> friendsToAdd = Josue.getFriendsToAdd("Creators");
		assertTrue(friendsToAdd.isPresent());
		List<User> friends = friendsToAdd.get();
		assertEquals(1, friends.size());
		User friend = friends.get(0);
		assertSame(Jane, friend);
	}
}

