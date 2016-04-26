package com.johnstarich.moviematcher.models;

import junit.framework.TestCase;
import org.bson.types.ObjectId;

import java.util.Arrays;

/**
 * Created by Josue on 4/25/2016.
 */
public class GroupTest extends TestCase {
    private User Josue = new User( new ObjectId(), "jalfaro@MovieMatcher.com", "Josue" , "Alfaro");
    private User Jeremy = new User( new ObjectId(), "jcastillo@MovieMatcher.com", "Jeremy", "Castillo");
    private User John = new User( new ObjectId(), "jstarich@MovieMatcher.com", "John", "Starich");
    private User Cesar = new User( new ObjectId(), "2cgonzalez@MovieMatcher.com", "Cesar", "Gonzalez");

    public void testAddFriend() throws Exception {
        User Romeo = new User(new ObjectId());
        User Henry = new User(new ObjectId());
        User Lenny = new User(new ObjectId());
        User Max = new User(new ObjectId());
        Group aventura = new Group("KOB", Arrays.asList(Lenny, Max, Henry, Romeo));
        aventura = aventura.addFriend(Josue);

        assertEquals(5, aventura.members.size());
        assertTrue(aventura.members.contains(Josue));
    }

    public void testRemoveFriend() throws Exception {
        Group bffs = new Group("We da best", Arrays.asList(Josue, John, Jeremy, Cesar));
        bffs = bffs.removeFriend(Josue);

        assertFalse(bffs.members.contains(Josue));
        assertEquals(3, bffs.members.size());
    }

    public void testRenameGroup() {
        Group g1 = new Group("This Group Name Sucks", Arrays.asList(Cesar, John, Jeremy, Josue));
        g1 = g1.renameGroup("Cooler Group Name");

        assertNotSame("This Group Name Sucks", g1.name);
        assertEquals("Cooler Group Name", g1.name);
    }

    public void testEquals() throws Exception {
        Group g1 = new Group("g.o.a.t.s", Arrays.asList(Josue, John));
        Group g2 = new Group("goats FC", Arrays.asList(Jeremy, Cesar));
        Group g3 = new Group("goats FC", Arrays.asList(Jeremy, Cesar, John, Josue));

        assertNotSame(g1, g2);
        assertEquals(g2, g2);
        assertNotSame(g3, g2);
    }
}