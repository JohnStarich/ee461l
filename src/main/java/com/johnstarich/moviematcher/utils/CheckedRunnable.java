package com.johnstarich.moviematcher.utils;

/**
 * A simple runnable that is allowed to throw runtime exceptions
 * This is used in cases where the body of the lambda function
 * must throw an exception to be properly handled.
 * Created by johnstarich on 5/1/16.
 */
@FunctionalInterface
public interface CheckedRunnable {
	void run() throws RuntimeException;
}
