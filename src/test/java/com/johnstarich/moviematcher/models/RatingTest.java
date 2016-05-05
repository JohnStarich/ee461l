package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.AbstractMongoDBTest;
import junit.framework.TestCase;
import org.bson.types.ObjectId;

import java.util.Optional;

/**
 * Created by Josue on 4/21/2016.
 */
public class RatingTest extends AbstractMongoDBTest {
    private User Maximus = new User(new ObjectId(), "MaximusDecimusMeridius@Gladiator.com", "Maximus" , "Meridius");
    private User Commodus = new User(new ObjectId(), "Commodus@EmperorKilla.com", "Commodus", "Aurelius");
    private Movie Gladiator = new Movie(new ObjectId(), "Gladiator", null, null, null, null, null, null, null);
    private Rating rating1 = new Rating(new ObjectId(), Maximus.id, Gladiator.id, "Best movie ever. I love that last fight scene", 10 );
    private Rating rating2 = new Rating(new ObjectId(), Commodus.id, Gladiator.id, "It was okay. It was only cool when I was in the movie", 5);

    public void testEquals() throws Exception {
        assertNotSame(rating1, rating2);
        assertEquals(rating2, rating2);
    }

    public void testLoadRatingsByUser() throws Exception {
        rating1 = rating1.save();
        Movie theNiceGuys = new Movie(new ObjectId(), "The Nice Guys", null, null, null, null, null, null, null);
        new Rating(new ObjectId(), Maximus.id, theNiceGuys.id, "Purely Awesome!", 10).save();

        assertEquals(2, Rating.loadRatingsByUser(Maximus.id).get().size());
        assertEquals(0, Rating.loadRatingsByUser(Commodus.id).get().size());
    }

    public void testLoadRatingByUser() throws Exception {
        rating1 = rating1.save();
        Optional<Rating> r = Rating.loadRatingByUser(Maximus.id, Gladiator.id);
        Optional<Rating> rDos = Rating.loadRatingByUser(Commodus.id, Gladiator.id);

        assertEquals(r.get().id, rating1.id);
        assertFalse(rDos.isPresent());
    }
}