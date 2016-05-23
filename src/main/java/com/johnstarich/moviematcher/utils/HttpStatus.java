package com.johnstarich.moviematcher.utils;

/**
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
	public static String getErrorMessageForCode(int statusCode) {
		switch (statusCode) {
			case 200: return "OK";
			case 201: return "Created";
			case 202: return "Accepted";
			case 204: return "No Content";
			case 205: return "Reset Content";
			case 301: return "Moved Permanently";
			case 302: return "Found";
			case 307: return "Temporary Redirect";
			case 308: return "Permanent Redirect";
			case 400: return "Bad Request";
			case 401: return "Unauthorized";
			case 403: return "Forbidden";
			case 404: return "Not Found";
			case 500: return "Internal Server Error";
			case 501: return "Not Implemented";
		}

		switch (statusCode / 100) {
			case 1: return "Info";
			case 2: return "Success";
			case 3: return "Redirect";
			case 4: return "Client Error";
			case 5: return "Server Error";
			default:return "Server Error";
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
			case INTERNAL_SERVER_ERROR: return "Internal Server Error";
			case NOT_IMPLEMENTED: return "Not Implemented";
			case INFO: return "Info";
			case SUCCESS: return "Success";
			case REDIRECT: return "Redirect";
			case CLIENT_ERROR: return "Client Error";
			default: case SERVER_ERROR: return "Server Error";
		}
	}
}
