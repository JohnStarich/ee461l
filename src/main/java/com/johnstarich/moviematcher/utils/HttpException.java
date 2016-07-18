package com.johnstarich.moviematcher.utils;

/**
 * An exception with an HTTP status code to help produce good error messages on HTTP responses.
 * Created by johnstarich on 2/25/16.
 */
public class HttpException extends Exception {
	private static final long serialVersionUID = 1832901938935139436L;

	private final HttpStatus statusCode;

	public HttpException(HttpStatus statusCode) {
		this.statusCode = statusCode;
	}

	public HttpException() {
		statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public HttpException(HttpStatus statusCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.statusCode = statusCode;
	}

	public HttpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public HttpException(HttpStatus statusCode, String message, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
	}

	public HttpException(String message, Throwable cause) {
		super(message, cause);
		statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public HttpException(HttpStatus statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	public HttpException(String message) {
		super(message);
		statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public HttpException(HttpStatus statusCode, Throwable cause) {
		super(cause);
		this.statusCode = statusCode;
	}

	public HttpException(Throwable cause) {
		super(cause);
		statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public HttpStatus getStatusCode() { return statusCode; }

	@Override
	public String getMessage() {
		if(super.getMessage() != null)
			return super.getMessage();
		else
			return HttpStatus.getErrorMessageForCode(statusCode);
	}
}
