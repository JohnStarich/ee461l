package com.johnstarich.moviematcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles all requests sent to the root ("/") of this server.
 * Created by johnstarich on 1/30/16.
 */
public class RootServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("<h1>Hey there!</h1><h2>This is our EE461L design project</h2> <p>This is the request path we received: " + request.getPathInfo() + "</p>");
		System.out.println("Request was = GET " + request.getPathInfo());
	}
}
