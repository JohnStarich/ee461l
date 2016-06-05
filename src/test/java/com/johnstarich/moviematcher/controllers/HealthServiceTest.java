package com.johnstarich.moviematcher.controllers;

import com.johnstarich.moviematcher.routes.AbstractHttpClientTest;
import com.johnstarich.moviematcher.utils.HttpStatus;
import spark.Spark;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Test for HealthService
 * Created by johnstarich on 5/31/16.
 */
public class HealthServiceTest extends AbstractHttpClientTest {
	private static final Pattern VERSION_NUMBER = Pattern.compile("(?:[0-9]+\\.){2}[0-9]+");
	@Override
	public void setUp() throws Exception {
		super.setUp();
		new HealthService().init();
		Spark.awaitInitialization();
	}

	public void testStatus() throws Exception {
		get("/v1", response -> {
			assertEquals(HttpStatus.OK.code, response.status);
			assertEquals("application/json", response.type);
			Map jsonBody = response.json(Map.class);
			String version = jsonBody.get("version").toString();
			assertTrue(VERSION_NUMBER.matcher(version).matches());
		});
	}
}