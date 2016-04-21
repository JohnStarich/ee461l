package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.AbstractMongoDBTest;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

/**
 * Created by Josue on 4/21/2016.
 */
public class UserTest extends AbstractMongoDBTest {
    private User Josue = new User( new ObjectId(), "jalfaro@MovieMatcher.com", "Josue" , "Alfaro");
    private User Jeremy = new User( new ObjectId(), "jcastillo@MovieMatcher.com", "Jeremy", "Castillo");
    private User John = new User( new ObjectId(), "jstarich@MovieMatcher.com", "John", "Starich");
    private User Cesar = new User( new ObjectId(), "2cgonzalez@MovieMatcher.com", "Cesar", "Gonzalez");

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testLoadByUserName()  throws Exception {
        Jeremy = Jeremy.register("betterPassword1");

        User u = User.loadByUsername("jcastillo@MovieMatcher.com");
        assertEquals(u, Jeremy);

        User nonExisting = User.loadByUsername("aventura@MovieMatcher.com");
        assertEquals(null, nonExisting);
    }

    @Test
    public void testAddFriends() {

    }
}
