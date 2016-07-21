package com.johnstarich.moviematcher.utils;

/**
 * Allows a hidden message to be printed to console but shows a safer and more generic message to the API user
 * Created by johnstarich on 7/18/16.
 */
public class ClientFacingHttpException extends HttpException {
	private static final long serialVersionUID = -7598860384364800155L;

	private final String hiddenMessage;

	public ClientFacingHttpException(HttpStatus httpStatus, String hiddenMessage, String message, Throwable cause) {
		super(httpStatus, message, cause);
		this.hiddenMessage = hiddenMessage;
	}

	public ClientFacingHttpException(HttpStatus httpStatus, String hiddenMessage, String message) {
		super(httpStatus, message);
		this.hiddenMessage = hiddenMessage;
	}

	public ClientFacingHttpException(HttpStatus httpStatus, String hiddenMessage) {
		super(HttpStatus.CLIENT_ERROR, "Something went wrong with your request, sorry for any inconvenience");
		this.hiddenMessage = hiddenMessage;
	}

	public ClientFacingHttpException(String hiddenMessage, String message) {
		super(message);
		this.hiddenMessage = hiddenMessage;
	}

	public String getHiddenMessage() { return hiddenMessage; }
}
