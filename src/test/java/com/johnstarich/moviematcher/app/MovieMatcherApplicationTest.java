package com.johnstarich.moviematcher.app;

import com.johnstarich.moviematcher.models.Movie;
import org.bson.types.ObjectId;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by johnstarich on 3/28/16.
 */
public class MovieMatcherApplicationTest {
	@Test
	public void moviesShouldBeEqualById() {
		Movie m = new Movie(new ObjectId());
		assertEquals(m, new Movie(m._id));
	}
}
