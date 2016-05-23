package com.johnstarich.moviematcher.utils;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * Test for CountedSet
 * Created by johnstarich on 5/8/16.
 */
public class CountedSetTest extends TestCase {
	public void testAdd() throws Exception {
		CountedSet<String> set = new CountedSet<>();
		assertEquals(set.count("woot"), 0);
		set.add("woot");
		assertEquals(set.count("woot"), 1);
	}

	public void testAddAll() throws Exception {
		List<String> elements = Arrays.asList("hello", "hello", "nope", "hello", "maybe");
		CountedSet<String> set = new CountedSet<>();
		set.addAll(elements);
		assertEquals(3, set.count("hello"), 3);
		assertEquals(1, set.count("nope"), 1);
		assertEquals(1, set.count("maybe"), 1);

		CountedSet<String> set2 = new CountedSet<>(elements);
		assertEquals(3, set2.count("hello"));
		assertEquals(1, set2.count("nope"));
		assertEquals(1, set2.count("maybe"));
	}

	public void testNotNull() throws Exception {
		try {
			CountedSet set = new CountedSet<>(null);
			throw new AssertionError("Should not be able to instantiate CountedSet with a null collection.");
		}
		catch (NullPointerException e) {
			System.out.println("Not null passed");
		}
	}
}