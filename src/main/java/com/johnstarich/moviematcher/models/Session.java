package com.johnstarich.moviematcher.models;

import de.caluga.morphium.annotations.Reference;
import de.caluga.morphium.annotations.Index;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Optional;

/**
 * A user's login session
 * Allow's for authenticated access without having to
 * store the password in an insecure fashion on the browser or server
 * Created by Josue on 4/25/2016.
 */
public class Session extends AbstractModel<Session> {
    /** Session expiration time in milliseconds (2 hours) */
    public final static long EXPIRATION_IN_MILLISECONDS = 7200000;

    @Reference
    public final User user;

    @Index(value = "createdAt:1", options = "expireAfterSeconds:7200")
    public final Date createdAt;

    public Session(ObjectId sessionId, User user) {
        super(Session.class, sessionId);
        this.user = user;
        createdAt = new Date();
    }

    public static boolean isValid(Session s) {
        Optional<Session> session = s.load();
        if(session.isPresent()) {
            long rightNow = new Date().getTime();
            long timeCreated = session.get().createdAt.getTime();
            return rightNow - timeCreated <= EXPIRATION_IN_MILLISECONDS && rightNow - timeCreated > 0;
        }
        return false;
    }

}

