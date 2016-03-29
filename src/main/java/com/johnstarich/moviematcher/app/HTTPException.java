package com.johnstarich.moviematcher.app;

/**
 * An exception with an HTTP status code to help produce good error messages on HTTP responses.
 * Created by johnstarich on 2/25/16.
 */
public class HttpException extends Exception {
	private int statusCode = 500;

	public HttpException(int statusCode) {
		super();
		this.statusCode = statusCode;
	}

	public HttpException(HttpStatus statusCode) {
		this.statusCode = statusCode.code;
	}

	public HttpException() {
		super();
	}

	public HttpException(int statusCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.statusCode = statusCode;
	}

	public HttpException(HttpStatus statusCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.statusCode = statusCode.code;
	}

	public HttpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public HttpException(int statusCode, String message, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
	}

	public HttpException(HttpStatus statusCode, String message, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode.code;
	}

	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	public HttpException(HttpStatus statusCode, String message) {
		super(message);
		this.statusCode = statusCode.code;
	}

	public HttpException(String message) {
		super(message);
	}

	public HttpException(int statusCode, Throwable cause) {
		super(cause);
		this.statusCode = statusCode;
	}

	public HttpException(HttpStatus statusCode, Throwable cause) {
		super(cause);
		this.statusCode = statusCode.code;
	}

	public HttpException(Throwable cause) {
		super(cause);
	}

	public int getStatusCode() { return statusCode; }

	@Override
	public String getMessage() {
		if(super.getMessage() != null)
			return super.getMessage();
		else
			return HttpStatus.getErrorMessageForCode(statusCode);
	}


}
