package com.johnstarich.moviematcher.models;

import de.caluga.morphium.annotations.Reference;
import de.caluga.morphium.annotations.Index;
import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created by Josue on 4/25/2016.
 */
public class Session extends AbstractModel<Session> {
    @Reference
    public final User user;

    @Index(value = "createdAt:1", options = "expireAfterSeconds:60")//7200
    public final Date createdAt;

    Session(ObjectId sessionId, User user) {
        super(Session.class, sessionId);
        this.user = user;
        createdAt = new Date();
    }

    /*public static boolean isValid(Session s) {
        if(!s.exists()) {
            //need to throw some sort of error!
        }
        new Date()
    }

    public Optional<Session> loadUsersMostCurrrentSession() {

    }*/
}
