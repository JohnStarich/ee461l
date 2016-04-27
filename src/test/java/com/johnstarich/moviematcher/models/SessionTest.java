package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.AbstractMongoDBTest;

import com.johnstarich.moviematcher.app.MovieMatcherApplication;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * Created by Josue on 4/26/2016.
 */
public class SessionTest extends AbstractMongoDBTest {
    User Josue = new User(new ObjectId());

    public void testExpiration() throws Exception {
        Josue.save();

        Session s = new Session(new ObjectId(), Josue);
        s.save();

        //{"collMod" : <collection> , "<flag>" : <value> }
        //{keyPattern: <index_spec>, expireAfterSeconds: <seconds> }
        //db.runCommand({ collMod: "Session", index : { keyPattern: { createdAt : 1} , expireAfterSeconds: 60 } })

        /*Map hm = MovieMatcherDatabase.morphium.execCommand(
                "{ collMod: \"Session\", index : { keyPattern: { createdAt : 1} , expireAfterSeconds: 60 } }"
        );*/

        /*assertTrue(hm.containsKey("expireAfterSeconds_new"));
        assertTrue(hm.containsValue(60));*/
        String index = MovieMatcherDatabase.morphium.getDatabase().getCollection("session").getIndexInfo().get(0).toString();
        //DBObject index = MovieMatcherDatabase.morphium.getDatabase().getCollection("session").getIndexInfo().get(0);
        MovieMatcherDatabase.morphium.getDatabase().getCollection("session").dropIndexes();
        MovieMatcherDatabase.morphium.getDatabase().getCollection("session").createIndex(index);

        //MovieMatcherDatabase.morphium.getDatabase().getCollection("session").createIndex();
                //.createIndex("value = \"createdAt:1\", options = \"expireAfterSeconds:60\"");

        Thread.sleep(120000);

        assertFalse(s.exists());
        assertTrue(Josue.exists());
        assertFalse(s.load().isPresent());
    }
}