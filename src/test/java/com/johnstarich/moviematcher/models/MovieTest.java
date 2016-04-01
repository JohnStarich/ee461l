package com.johnstarich.moviematcher.models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Josue on 3/31/2016.
 */
public class MovieTest {
    //we can only run this test when we have access to MONGO_HOST...
    //@Test
    public void moviesSearchShouldReturnRelevantResults() {
        java.util.List<Movie> movieList = Movie.search("Creed");
        assertNotEquals(0, movieList.size());

        movieList = Movie.search("The Dark Knight");
        assertNotEquals(0, movieList.size());
        assertEquals(true, movieList.get(0).title.contains("Dark"));

        movieList = Movie.search("aklsjdflkj ajjasdkfj ajsfojoasjdfl");
        assertEquals(0, movieList.size());

    }
}
