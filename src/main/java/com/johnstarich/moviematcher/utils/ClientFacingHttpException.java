package com.johnstarich.moviematcher.utils;

/**
 * Allows a hidden message to be printed to console but shows a safer and more generic message to the API user
 * Created by johnstarich on 7/18/16.
 */
public class ClientFacingHttpException extends HttpException {
	private static final long serialVersionUID = -7598860384364800155L;

	private final HttpStatus hiddenHttpStatus;
	private final String hiddenMessage;

	public ClientFacingHttpException(HttpStatus httpStatus, String hiddenMessage, String message, Throwable cause) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
		hiddenHttpStatus = httpStatus;
		this.hiddenMessage = hiddenMessage;
	}

	public ClientFacingHttpException(HttpStatus httpStatus, String hiddenMessage, String message) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, message);
		hiddenHttpStatus = httpStatus;
		this.hiddenMessage = hiddenMessage;
	}

	public ClientFacingHttpException(HttpStatus httpStatus, String hiddenMessage) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong with your request, sorry for any inconvenience");
		hiddenHttpStatus = httpStatus;
		this.hiddenMessage = hiddenMessage;
	}

	public ClientFacingHttpException(String hiddenMessage, String message) {
		super(message);
		hiddenHttpStatus = getStatusCode();
		this.hiddenMessage = hiddenMessage;
	}

	public String getHiddenMessage() { return hiddenMessage; }

	public HttpStatus getHiddenHttpStatus() { return hiddenHttpStatus; }
}
