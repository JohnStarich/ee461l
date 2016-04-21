package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.AbstractMongoDBTest;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Josue on 4/21/2016.
 */
public class UserTest extends AbstractMongoDBTest {
    private User Josue = new User( new ObjectId(), "jalfaro@MovieMatcher.com", "Josue" , "Alfaro");
    private User Jeremy = new User( new ObjectId(), "jcastillo@MovieMatcher.com", "Jeremy", "Castillo");
    private User John = new User( new ObjectId(), "jstarich@MovieMatcher.com", "John", "Starich");
    private User Cesar = new User( new ObjectId(), "2cgonzalez@MovieMatcher.com", "Cesar", "Gonzalez");

    @Before
    public void setUp() {
        try {
            Josue.register("goodPassword");
            Jeremy.register("betterPassword1");
            John.register("evenbetterPassword2");
            Cesar.register("thebestPassword#3");
        } catch (com.johnstarich.moviematcher.app.HttpException e) {
            assertEquals("User with this email already exists.", e.getMessage());
        }
    }

    @Test
    public void testUserRegister() {
        assertEquals(true, Josue.exists());
        assertEquals(true, Jeremy.exists());
        assertEquals(true, John.exists());
        assertEquals(true, Cesar.exists());
    }

    @Test
    public void testEncryption() {
        assertEquals(false, Cesar.password.equals("thebestPassword#3"));
        assertEquals(false, John.password.equals("evenbetterPassword2"));
        assertEquals(false, Jeremy.password.equals("betterPassword1"));
        assertEquals(false, Josue.password.equals("goodPassword"));
    }

    @Test
    public void testResetPassword() {
        try {
            John.resetPassword("wrongPassword", "INeedBetterPassword@2013");
        } catch (com.johnstarich.moviematcher.app.HttpException e) {
            assertEquals("Invalid password.", e.getMessage());
        }
        String johnsOldPsw = John.password;
        try {
            John.resetPassword("evenbetterPassword2", "INeedBetterPassword@2013");
        } catch (com.johnstarich.moviematcher.app.HttpException e) {
            assertEquals("Invalid password.", e.getMessage());
        }
        assertEquals(false, johnsOldPsw.equals(John.password));
    }

    @Test
    public void testLoadByUserName() {
        User u = User.loadByUsername("jcastillo@MovieMatcher.com");
        assertEquals(true, u.equals(Jeremy));

        User nonExisting = User.loadByUsername("aventura@MovieMatcher.com");
        System.out.println(nonExisting);
    }

    @Test
    public void testAddFriends() {

    }
}
