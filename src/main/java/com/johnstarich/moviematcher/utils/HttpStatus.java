package com.johnstarich.moviematcher.utils;

/**
 * Provides industry standard error codes and their associated meanings for use in throwing HTTP errors
 * Created by johnstarich on 3/28/16.
 */
public enum HttpStatus {
	INFO (100),
	SUCCESS (200),
	REDIRECT (300),
	CLIENT_ERROR (400),
	SERVER_ERROR (500),

	OK (200),
	CREATED (201),
	ACCEPTED (202),
	NO_CONTENT (204),
	RESET_CONTENT (205),

	MOVED_PERMANENTLY (301),
	FOUND (302),
	TEMPORARY_REDIRECT (307),
	PERMANENT_REDIRECT (308),

	BAD_REQUEST (400),
	UNAUTHORIZED (401),
	FORBIDDEN (403),
	NOT_FOUND (404),
	UNSUPPORTED_MEDIA_TYPE (415),

	INTERNAL_SERVER_ERROR (500),
	NOT_IMPLEMENTED (501),
	;

	public final int code;

	HttpStatus(int code) {
		this.code = code;
	}

	/**
	 * Tries to figure out what kind of response occurred based on the status code and returns the appropriate message.
	 * @param statusCode the code to retrieve a message for
	 * @return the message
	 */
	public static HttpStatus getGenericErrorMessageForCode(int statusCode) {
		switch (statusCode / 100) {
			case 1: return INFO;
			case 2: return SUCCESS;
			case 3: return REDIRECT;
			case 4: return CLIENT_ERROR;
			case 5:
			default:return SERVER_ERROR;
		}
	}

	/**
	 * Tries to figure out what kind of response occurred based on the status code and returns the appropriate message.
	 * @param statusCode the code to retrieve a message for
	 * @return the message
	 */
	public static String getErrorMessageForCode(HttpStatus statusCode) {
		switch (statusCode) {
			case OK: return "OK";
			case CREATED: return "Created";
			case ACCEPTED: return "Accepted";
			case NO_CONTENT: return "No Content";
			case RESET_CONTENT: return "Reset Content";
			case MOVED_PERMANENTLY: return "Moved Permanently";
			case FOUND: return "Found";
			case TEMPORARY_REDIRECT: return "Temporary Redirect";
			case PERMANENT_REDIRECT: return "Permanent Redirect";
			case BAD_REQUEST: return "Bad Request";
			case UNAUTHORIZED: return "Unauthorized";
			case FORBIDDEN: return "Forbidden";
			case NOT_FOUND: return "Not Found";
			case UNSUPPORTED_MEDIA_TYPE: return "Unsupported Media Type";
			case INTERNAL_SERVER_ERROR: return "Internal Server Error";
			case NOT_IMPLEMENTED: return "Not Implemented";
			case INFO: return "Info";
			case SUCCESS: return "Success";
			case REDIRECT: return "Redirect";
			case CLIENT_ERROR: return "Client Error";
			case SERVER_ERROR:
			default:
				return "Server Error";
		}
	}
}
