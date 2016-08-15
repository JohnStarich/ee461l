package com.johnstarich.moviematcher.controllers;

import com.google.gson.internal.LinkedTreeMap;
import com.johnstarich.moviematcher.models.Movie;
import com.johnstarich.moviematcher.models.Session;
import com.johnstarich.moviematcher.models.User;
import org.bson.types.ObjectId;
import spark.Spark;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;

/**
 * Test for MovieService
 * Created by gonzalezcesar on 7/16/16.
 */
public class MovieServiceTest extends AbstractMongoDBTest {
	private final MovieService movieService = new MovieService();
	private final UserService userService = new UserService();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		userService.init();
		movieService.init();
		Spark.awaitInitialization();
	}

	public User register(String password) throws Exception {
		return new User(null, "Test0", "foo", "bar").register(password);
	}

	public Session login(String password) throws Exception {
		return User.login("Test0", password);
	}

	public Map<String, String> authHeaders(Session session) {
		if(session == null) throw new AssertionError("Session is null");
		if(session.id == null) throw new AssertionError("Session ID is null");
		return Collections.singletonMap("Authorization", session.id.toHexString());
	}

	public Movie addMovie(String title) throws Exception {
		return new Movie(new ObjectId(), title, null, null, null, null, null, null, null)
			.save();
	}
	public Movie addMovie(ObjectId id, String title) throws Exception {
		return new Movie(id, title, null, null, null, null, null, null, null)
		.save();
	}

	public void testGetMovieSearchResultsForDark() throws Exception {
		Movie theDarkKnight = addMovie("The Dark Knight");
		Movie theDarkKnightRises = addMovie("The Dark Knight Rises");
		Movie theRing = addMovie("The Ring");
		register("Welcome1");
		Session s = login("Welcome1");
		get(movieService.PREFIX + "/search/dark", authHeaders(s), response -> {
			Map<String, Object> returnedMap = response.json(HashMap.class);
			List<LinkedTreeMap> movieResults = (List) returnedMap.get("movies");
			HashSet<String> movieTitles = new HashSet<>();
			movieResults.forEach(m -> movieTitles.add(m.get("title").toString()));

			assertTrue(movieTitles.contains(theDarkKnight.title));
			assertTrue(movieTitles.contains(theDarkKnightRises.title));
			assertFalse(movieTitles.contains(theRing.title));
		});
	}
	public void testGetMovieSearchResultsForGarbage() throws Exception {
		Movie theDarkKnight = addMovie("The Dark Knight");
		Movie theDarkKnightRises = addMovie("The Dark Knight Rises");
		Movie theRing = addMovie("The Ring");
		register("Welcome1");
		Session s = login("Welcome1");
		get(movieService.PREFIX + "/search/dllsakdjflaiwef;alsdkhlueiahe", authHeaders(s), response -> {
			Map<String, Object> returnedMap = response.json(HashMap.class);
			List<LinkedTreeMap<String, String>> movieResults = (List) returnedMap.get("movies");

			assertTrue(movieResults.isEmpty());
		});
	}
	public void testGetMovieByMovieId() throws Exception { 
		ObjectId movieId = new ObjectId(); 
		Movie theDarkKnight = addMovie(movieId, "The Dark Knight"); 
		register("Welcome1"); 
		Session s = login("Welcome1"); 
		get(movieService.PREFIX + "/"+movieId, authHeaders(s), response -> { 
			Movie movieResult = response.json(Movie.class); 
			assertEquals(theDarkKnight.title, movieResult.title); 
			assertEquals(theDarkKnight.id, movieResult.id); 
		}); 
	}

}
