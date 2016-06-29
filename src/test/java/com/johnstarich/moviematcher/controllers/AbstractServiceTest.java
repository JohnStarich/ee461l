package com.johnstarich.moviematcher.controllers;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Test for AbstractService
 * Created by johnstarich on 3/22/16.
 */
public class AbstractServiceTest {
	@Test
	public void basicApplicationShouldSetPrefix() {
		String prefix = "/wee";
		AbstractService app = new AbstractService() {
			public String mountPoint() { return ""; }
			public String resource() { return prefix; }
		};

		assertEquals(app.PREFIX, prefix);
		assertEquals(app.PREFIX, app.resource());
	}
}
