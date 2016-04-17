package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.AbstractMongoDBTest;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by johnstarich on 4/17/16.
 */
public class MovieTest extends AbstractMongoDBTest {
	@Test
	public void testLoad() throws Exception {
		MongoCollection<Document> collection = MovieMatcherDatabase.getCollection("movies");
		collection.createIndex(new Document("title", "text"));
		collection.insertOne(new Document("title", "Creed"));
		collection.insertOne(new Document("title", "The Dark Knight"));

		java.util.List<Movie> movieList = Movie.search("Creed");
		assertNotEquals(0, movieList.size());

		movieList = Movie.search("The Dark Knight");
		assertNotEquals(0, movieList.size());
		assertEquals(true, movieList.get(0).title.contains("Dark"));

		movieList = Movie.search("aklsjdflkj ajjasdkfj ajsfojoasjdfl");
		assertEquals(0, movieList.size());
	}

	@Test
	public void testSearch() throws Exception {
	}
}