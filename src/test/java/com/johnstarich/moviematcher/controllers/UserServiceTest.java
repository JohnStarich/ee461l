package com.johnstarich.moviematcher.controllers;

import com.johnstarich.moviematcher.models.Session;
import com.johnstarich.moviematcher.models.User;
import org.bson.types.ObjectId;
import spark.Spark;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Test for UserService
 * Created by johnstarich on 5/27/16.
 */
public class UserServiceTest extends AbstractMongoDBTest {
	private UserService service = new UserService();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		service.init();
		Spark.awaitInitialization();
	}

	public User register(String password) throws Exception {
		return new User(null, "johnstarich", "John", "Starich").register(password);
	}

	public Session login(String password) throws Exception {
		return User.login("johnstarich", password);
	}

	public Map<String, String> authHeaders(Session session) throws Exception {
		return Collections.singletonMap("Authorization", session.id.toHexString());
	}

	public void testRegister() throws Exception {
		Map<String, Object> payload = new HashMap<>();
		payload.put("username", "johnstarich");
		payload.put("first_name", "John");
		payload.put("last_name", "Starich");
		payload.put("password", "Welcome1");

		post(service.PREFIX, payload, response -> {
			User user = response.json(User.class);
			assertEquals("johnstarich", user.username);
			assertEquals("John", user.first_name);
			assertEquals("Starich", user.last_name);
			assertNull(user.password);
		});
	}

	public void testLogin() throws Exception {
		User registeredUser = register("Welcome1");
		Map<String, Object> payload = new HashMap<>();
		payload.put("username", registeredUser.username);
		payload.put("password", "Welcome1");

		post(service.PREFIX + "/login", payload, response -> {
			Session s = response.json(Session.class);
			assertEquals("John", s.user.first_name);
			assertEquals("Starich", s.user.last_name);
			assertEquals("johnstarich", s.user.username);
			assertNotSame("Welcome1", s.user.password);
		});
	}

	public void testGetUser() throws Exception {
		register("Welcome1");
		Session s = login("Welcome1");
		get(service.PREFIX, authHeaders(s), response -> {
			assertEquals("John", s.user.first_name);
			assertEquals("Starich", s.user.last_name);
			assertEquals("johnstarich", s.user.username);
			assertNotSame("Welcome1", s.user.password);
		});
	}

	public void testUpdatedUserSettings() throws Exception {
	}

	public void testAddRatings() throws Exception {
		register("Welcome1");
		Session s = login("Welcome1");
		Map<String, Object> data = new HashMap<>();
		Map<String, Object> innerRatingData = new HashMap<>();
		data.put("user", innerRatingData);
		innerRatingData.put("id", new ObjectId().toHexString());
		innerRatingData.put("rating", 6.5D);
		post(service.PREFIX + "/ratings", data, authHeaders(s), response -> {
			assertEquals("\"rating saved!\"", response.body);
		});
	}
}