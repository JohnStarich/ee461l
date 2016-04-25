package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by Josue on 4/17/2016.
 */
public class Rating extends AbstractModel<Rating> {
    public final ObjectId user_id;
    public final ObjectId movie_id;
    public final String comment;
    public final int numeric_rating; //ratings are [0,10]

    public Rating(ObjectId rating_id) {
        super(Rating.class, rating_id);
        this.user_id = null;
        this.movie_id = null;
        this.comment = null;
        this.numeric_rating = Integer.MIN_VALUE;
    }

    public Rating(ObjectId rating_id, ObjectId user_id, ObjectId movie_id, String comment, int numeric_rating) {
        super(Rating.class, rating_id);
        this.user_id = user_id;
        this.movie_id = movie_id;
        this.comment = comment;
        if(numeric_rating > 10 || numeric_rating < 0 ) {
            throw new IllegalArgumentException("Please provide a rating within these bounds, [0,10].");
        }
        else { this.numeric_rating = numeric_rating; }
     }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Rating)) return false;
        return ((Rating) o).id != null && ((Rating) o).id.equals(id);
    }

    public static List<Rating> loadRatingsByUser(ObjectId userID) {
        return MovieMatcherDatabase.morphium.findByField(Rating.class, "user_id", userID);
    }

}
