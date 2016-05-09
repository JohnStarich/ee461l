package com.johnstarich.moviematcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by johnstarich on 5/8/16.
 */
public class CountedSet<T> extends HashMap<T, AtomicLong> {
	public CountedSet() {
		super();
	}

	public long count(T key) {
		AtomicLong value = super.get(key);
		if(value == null) return 0L;
		return value.get();
	}

	public CountedSet(int initialCapacity) {
		super(initialCapacity);
	}

	public CountedSet(Collection<? extends T> c) {
		super(c.size());
		addAll(c);
	}

	public void add(T item) {
		AtomicLong currentCount = get(item);
		if(currentCount == null) {
			currentCount = new AtomicLong(0L);
			putIfAbsent(item, currentCount);
		}
		currentCount.incrementAndGet();
	}

	public void addAll(Collection<? extends T> c) {
		c.parallelStream().forEach(this::add);
	}
}
