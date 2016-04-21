package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.AbstractMongoDBTest;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Josue on 4/21/2016.
 */
public class UserTest extends AbstractMongoDBTest {
    private User Josue = new User( new ObjectId(), "jalfaro@MovieMatcher.com", "Josue" , "Alfaro");
    private User Jeremy = new User( new ObjectId(), "jcastillo@MovieMatcher.com", "Jeremy", "Castillo");
    private User John = new User( new ObjectId(), "jstarich@MovieMatcher.com", "John", "Starich");
    private User Cesar = new User( new ObjectId(), "2cgonzalez@MovieMatcher.com", "Cesar", "Gonzalez");

    public void testUserRegister() throws Exception {
        Josue = Josue.register("goodPassword");
        Jeremy = Jeremy.register("betterPassword1");
        John = John.register("evenbetterPassword2");
        Cesar = Cesar.register("thebestPassword#3");

        assertTrue(Josue.exists());
        assertTrue(Jeremy.exists());
        assertTrue(John.exists());
        assertTrue(Cesar.exists());
    }

    public void testEncryption() throws Exception {
        Josue = Josue.register("goodPassword");
        Jeremy = Jeremy.register("betterPassword1");
        John = John.register("evenbetterPassword2");
        Cesar = Cesar.register("thebestPassword#3");

        assertFalse(Cesar.password.equals("thebestPassword#3"));
        assertFalse(John.password.equals("evenbetterPassword2"));
        assertFalse(Jeremy.password.equals("betterPassword1"));
        assertFalse(Josue.password.equals("goodPassword"));
    }

    public void testResetPassword() throws Exception {
        John = John.register("evenbetterPassword2");

        try {
            John = John.resetPassword("wrongPassword", "INeedBetterPassword@2013");
        } catch (com.johnstarich.moviematcher.app.HttpException e) {
            assertEquals("Invalid password.", e.getMessage());
        }

        String johnsOldPsw = John.password;

        John = John.resetPassword("evenbetterPassword2", "INeedBetterPassword@2013");

        assertNotSame(johnsOldPsw, John.password);

    }

    public void testLoadByUserName()  throws Exception {
        Jeremy = Jeremy.register("betterPassword1");

        User u = User.loadByUsername("jcastillo@MovieMatcher.com");
        assertEquals(u, Jeremy);

        User nonExisting = User.loadByUsername("aventura@MovieMatcher.com");
        assertEquals(null, nonExisting);
    }

    public void testAddFriend() throws Exception {
        Josue = Josue.register("goodPassword");
        Cesar = Cesar.register("thebestPassword#3");

        Josue = Josue.addFriend(Cesar);
        Optional<User> noNewFriendsJosue = Josue.load();
        assertNotSame(noNewFriendsJosue.get().friends, Josue.friends);

        Josue.save();
        Optional<User> newFriendJosue = Josue.load();

        assertEquals(newFriendJosue.get().friends, Josue.friends);
    }

    public void testAddFriends() throws Exception {
        Josue = Josue.register("goodPassword");
        Jeremy = Jeremy.register("betterPassword1");
        John = John.register("evenbetterPassword2");
        Cesar = Cesar.register("thebestPassword#3");

        List<User> collectionOfFriends = new ArrayList<>(3);
        collectionOfFriends.add(Jeremy); collectionOfFriends.add(John); collectionOfFriends.add(Cesar);

        Josue = Josue.addFriends(collectionOfFriends);

        Josue.save();
        Optional<User> newFriends = Josue.load();

        assertEquals(newFriends.get().friends, Josue.friends);
    }

    public void testRemoveFriend() throws Exception {
        Josue = Josue.register("goodPassword");
        Jeremy = Jeremy.register("betterPassword1");
        John = John.register("evenbetterPassword2");
        Cesar = Cesar.register("thebestPassword#3");

        Josue = Josue.addFriend(Cesar);

        List<User> oldFriend = Josue.friends;

        Josue = Josue.removeFriend(Cesar);
        assertNotSame(oldFriend, Josue.friends);

    }

    public void testAddFriendToGroup() throws Exception {
        Josue = Josue.register("goodPassword");
        Jeremy = Jeremy.register("betterPassword1");
        John = John.register("evenbetterPassword2");
        Cesar = Cesar.register("thebestPassword#3");

        List<User> collectionOfFriends = new ArrayList<>(3);
        collectionOfFriends.add(Jeremy); collectionOfFriends.add(John); collectionOfFriends.add(Cesar);
        Group creators = new Group("Creators", collectionOfFriends);

        John = John.addGroup(creators);

        John = John.addFriend(Josue);
        John = John.addFriendToGroup("Creators", Josue);

        assertTrue(John.groups.get(0).members.contains(Josue));
    }
}
