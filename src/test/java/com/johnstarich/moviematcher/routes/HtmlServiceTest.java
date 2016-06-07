package com.johnstarich.moviematcher.routes;

import com.johnstarich.moviematcher.controllers.HtmlService;
import com.johnstarich.moviematcher.store.ConfigManager;
import com.johnstarich.moviematcher.utils.HttpStatus;
import spark.Spark;

/**
 * Test for HtmlService
 * Created by johnstarich on 6/3/16.
 */
public class HtmlServiceTest extends AbstractHttpClientTest {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		ConfigManager.setProperty("CACHE_STATIC_FILES", "true");
		new HtmlService().init();
		Spark.awaitInitialization();
	}

	public void testRoot() throws Exception {
		ServeStaticFileRoute.setPage("/html", "It works!", "text/html");
		get("/html", response -> {
			assertEquals("It works!", response.body);
			assertEquals("text/html", response.type);
			assertEquals(HttpStatus.OK.code, response.status);
		});
	}

	public void testCss() throws Exception {
		ServeStaticFileRoute.setPage("/css", "/* This is CSS */", "text/css");
		get("/css", response -> {
			assertEquals("/* This is CSS */", response.body);
			assertEquals("text/css", response.type);
			assertEquals(HttpStatus.OK.code, response.status);
		});
	}
}