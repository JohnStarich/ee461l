package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.AbstractMongoDBTest;
import junit.framework.TestCase;
import org.bson.types.ObjectId;

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
        assertFalse(rating1.equals(rating2));
        assertTrue(rating2.equals(rating2));
    }

    public void testLoadRatingsByUser() {
        rating1 = rating1.save();
        Movie theNiceGuys = new Movie(new ObjectId(), "The Nice Guys", null, null, null, null, null, null, null);
        new Rating(new ObjectId(), Maximus.id, theNiceGuys.id, "Purely Awesome!", 10).save();

        assertEquals(2, Rating.loadRatingsByUser(Maximus.id).size());
        assertEquals(0, Rating.loadRatingsByUser(Commodus.id).size());
    }

    public void testExists() {
        rating1 = rating1.save();
        assertTrue(rating1.exists());
        assertFalse(rating2.exists());
    }

    public void testDelete() {
        rating2 = rating2.save();
        assertTrue(rating2.exists());
        rating2 = rating2.delete();
        assertFalse(rating2.exists());
    }

}
