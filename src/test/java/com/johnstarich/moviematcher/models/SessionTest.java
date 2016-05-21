package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.AbstractMongoDBTest;

import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;


/**
 * Created by Josue on 4/26/2016.
 */
public class SessionTest extends AbstractMongoDBTest {
    User Josue = new User(new ObjectId());

    public void testExpiration() throws Exception {
        /* test will usually run for 2m 4s */

        Session s = new Session(new ObjectId(), Josue);
        s = s.save();
        /* need to drop expiration of two hours */
        MovieMatcherDatabase.morphium
            .getDatabase()
            .getCollection("session")
            .dropIndex(new BasicDBObject("created_at", 1));

        DBObject value = new BasicDBObject("created_at", 1);
        DBObject property = new BasicDBObject("expireAfterSeconds", 60);
        /* creates expiration for 60 seconds */
        MovieMatcherDatabase.morphium
            .getDatabase()
            .getCollection("session")
            .createIndex(value, property);

        Josue = Josue.save();

        Thread.sleep(120000);

        /* ensures that only the session was removed */
        assertFalse(s.exists());
        assertTrue(Josue.exists());
    }

    public void testIsValid() throws Exception {
        Session s = new Session(new ObjectId(), Josue);
        s = s.save();
        assertTrue(Session.isValid(s));

        s = s.delete();
        assertFalse(Session.isValid(s));
    }
}
