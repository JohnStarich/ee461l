package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.controllers.AbstractMongoDBTest;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Test for Movie
 * Created by johnstarich on 4/17/16.
 */
public class MovieTest extends AbstractMongoDBTest {
	public void testLoad() throws Exception {
		new Movie(new ObjectId(), "Creed", null, null, null, null, null, null, null)
			.save();
		new Movie(new ObjectId(), "The Dark Knight", null, null, null, null, null, null, null)
			.save();

		List<Movie> movieList = AbstractModel.search(Movie.class, "Creed");
		assertNotSame(0, movieList.size());

		movieList = AbstractModel.search(Movie.class, "The Dark Knight");
		assertNotSame(0, movieList.size());
		assertEquals(true, movieList.get(0).title.contains("Dark"));

		movieList = AbstractModel.search(Movie.class, "aklsjdflkj ajjasdkfj ajsfojoasjdfl");
		assertEquals(0, movieList.size());
	}


	public void testDateFormat() throws Exception{
		Movie m = new Movie(new ObjectId(), "Cesar's Awesome Movie", null, null, new Date(), null, null, null, null);
		m.save();
		MovieMatcherDatabase.morphium.clearCachefor(Movie.class);
		Movie loadedMovie = m.load().get();
		assertEquals(m.release_date, loadedMovie.release_date);
	}

}