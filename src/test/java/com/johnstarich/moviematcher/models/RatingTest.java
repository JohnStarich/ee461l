package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.controllers.AbstractMongoDBTest;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

/**
 * Test for Rating
 * Created by Josue on 4/21/2016.
 */
public class RatingTest extends AbstractMongoDBTest {
    private final User Maximus = new User(new ObjectId(), "MaximusDecimusMeridius@Gladiator.com", "Maximus" , "Meridius");
    private final User Commodus = new User(new ObjectId(), "Commodus@EmperorKilla.com", "Commodus", "Aurelius");
    private final Movie Gladiator = new Movie(new ObjectId(), "Gladiator", null, null, null, null, null, null, null);
    private final Rating rating1 = new Rating(new ObjectId(), Maximus.id, Gladiator.id, "Best movie ever. I love that last fight scene", 10 );
    private final Rating rating2 = new Rating(new ObjectId(), Commodus.id, Gladiator.id, "It was okay. It was only cool when I was in the movie", 5);

    public void testEquals() throws Exception {
        assertNotSame(rating1, rating2);
        assertEquals(rating2, rating2);
    }

    public void testLoadRatingsByUser() throws Exception {
        rating1.save();
        Movie theNiceGuys = new Movie(new ObjectId(), "The Nice Guys", null, null, null, null, null, null, null);
        new Rating(new ObjectId(), Maximus.id, theNiceGuys.id, "Purely Awesome!", 10).save();

        Optional<List<Rating>> maximusRatings = Rating.loadRatingsByUser(Maximus.id);
        Optional<List<Rating>> commodusRatings = Rating.loadRatingsByUser(Commodus.id);
        assertTrue(maximusRatings.isPresent());
        assertTrue(commodusRatings.isPresent());
        assertEquals(2, maximusRatings.get().size());
        assertEquals(0, commodusRatings.get().size());
    }

    public void testLoadRatingByUser() throws Exception {
        rating1.save();
        Optional<Rating> r = Rating.loadRatingByUser(Maximus.id, Gladiator.id);
        Optional<Rating> rDos = Rating.loadRatingByUser(Commodus.id, Gladiator.id);

        assertTrue(r.isPresent());
        assertEquals(r.get().id, rating1.id);
        assertFalse(rDos.isPresent());
    }
}