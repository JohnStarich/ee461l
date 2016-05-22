package com.johnstarich.moviematcher.controllers;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Created by johnstarich on 3/22/16.
 */
public class BasicControllerTest {
	@Test
	public void basicApplicationShouldSetPrefix() {
		String prefix = "wee";
		BasicController app = new BasicController() {
			public String prefix() { return prefix; }
			public void initService() {}
		};

		assertEquals(app.PREFIX, prefix);
		assertEquals(app.PREFIX, app.prefix());
	}
}
