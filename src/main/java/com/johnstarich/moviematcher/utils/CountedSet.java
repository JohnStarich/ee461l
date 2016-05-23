package com.johnstarich.moviematcher.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by johnstarich on 5/8/16.
 */
public class CountedSet<T> extends HashMap<T, Long> {
	public CountedSet() {
		super();
	}

	public long count(T key) {
		return super.getOrDefault(key, 0L);
	}

	public CountedSet(int initialCapacity) {
		super(initialCapacity);
	}

	public CountedSet(Collection<? extends T> c) {
		super(c.size());
		addAll(c);
	}

	public void add(T item) {
		synchronized (this) {
			put(item, getOrDefault(item, 0L) + 1L);
		}
	}

	public void addAll(Collection<? extends T> c) {
		c.parallelStream().forEach(this::add);
	}
}
