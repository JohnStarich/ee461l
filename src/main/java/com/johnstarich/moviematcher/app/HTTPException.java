package com.johnstarich.moviematcher.app;

/**
 * Created by johnstarich on 2/25/16.
 */
public class HTTPException extends Exception {
	private int statusCode = 500;

	public HTTPException(int statusCode) {
		super();
		this.statusCode = statusCode;
	}

	public HTTPException() {
		super();
	}

	public HTTPException(int statusCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.statusCode = statusCode;
	}

	public HTTPException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public HTTPException(int statusCode, String message, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
	}

	public HTTPException(String message, Throwable cause) {
		super(message, cause);
	}

	public HTTPException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	public HTTPException(String message) {
		super(message);
	}

	public HTTPException(int statusCode, Throwable cause) {
		super(cause);
		this.statusCode = statusCode;
	}

	public HTTPException(Throwable cause) {
		super(cause);
	}

	public int getStatusCode() { return statusCode; }

	@Override
	public String getMessage() {
		if(super.getMessage() != null)
			return super.getMessage();
		else
			return getErrorMessageForCode(statusCode);
	}

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
}
