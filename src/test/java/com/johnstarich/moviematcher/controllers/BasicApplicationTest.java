package com.johnstarich.moviematcher.controllers;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Created by johnstarich on 3/22/16.
 */
public class BasicApplicationTest {
	@Test
	public void basicApplicationShouldSetPrefix() {
		String prefix = "wee";
		BasicApplication app = new BasicApplication() {
			public String prefix() { return prefix; }
			public void app() {}
		};

		assertEquals(app.PREFIX, prefix);
		assertEquals(app.PREFIX, app.prefix());
	}
}
