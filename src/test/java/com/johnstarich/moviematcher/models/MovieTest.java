package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.AbstractMongoDBTest;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by johnstarich on 4/17/16.
 */
public class MovieTest extends AbstractMongoDBTest {
	@Test
	public void testLoad() throws Exception {
		new Movie(new ObjectId(), "Creed", null, null, null, null, null, null, null)
			.save();
		new Movie(new ObjectId(), "The Dark Knight", null, null, null, null, null, null, null)
			.save();

		java.util.List<Movie> movieList = AbstractModel.search(Movie.class, "Creed");
		assertNotEquals(0, movieList.size());

		movieList = AbstractModel.search(Movie.class, "The Dark Knight");
		assertNotEquals(0, movieList.size());
		assertEquals(true, movieList.get(0).title.contains("Dark"));

		movieList = AbstractModel.search(Movie.class, "aklsjdflkj ajjasdkfj ajsfojoasjdfl");
		assertEquals(0, movieList.size());
	}

	@Test
	public void testSearch() throws Exception {
	}
}