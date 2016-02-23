package com.johnstarich.moviematcher;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.net.URL;

/**
 * Runs an embedded Jetty server with the packaged webapp.
 * Created by johnstarich on 1/25/16.
 */
public class Main {
	public static final String WEB_APP_ROOT = "WEB-INF";

	public static void main(String args[]) throws Exception {
		Server server = new Server(8080);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.setResourceBase(getBaseURL());

		context.addServlet(RootServlet.class, "/*");

		server.setHandler(context);
		server.start();
		server.dump(System.err);
		server.join();
	}

	private static String getBaseURL() {
		URL webInfUrl = Main.class.getClassLoader().getResource(WEB_APP_ROOT);
		if (webInfUrl == null) {
			throw new RuntimeException("Error: Could not find the web app root: " + WEB_APP_ROOT);
		}
		return webInfUrl.toExternalForm();
	}
}
