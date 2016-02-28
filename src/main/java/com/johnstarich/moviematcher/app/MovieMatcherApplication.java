package com.johnstarich.moviematcher.app;

import com.johnstarich.moviematcher.models.Status;
import spark.Route;
import spark.Spark;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by johnstarich on 2/25/16.
 */
public class MovieMatcherApplication extends JsonApplication {
	private final String indexHtml = renderHTML("index.html");

	@Override
	public String prefix() { return "/v1"; }

	@Override
	public void app() {
		Route statusRoute = (request, response) -> new Status("1.0.0");
		jget("", statusRoute);
		jget("/", statusRoute);

		loginService();
		moviesService();
		friendsService();
		groupsService();

		Spark.get("/*", (request, response) -> indexHtml);
	}

	public void loginService() {
		jpost("/login", (request, response) -> {
			throw new HTTPException(501);
		});

		jpost("/login/register", (request, response) -> {
			throw new HTTPException(501);
		});

		Spark.before("/*", (request, response) -> {
			if(request.pathInfo().equals(PREFIX) ||
					request.pathInfo().equals(PREFIX+"/") ||
					request.pathInfo().startsWith("/login") ||
					request.pathInfo().startsWith(PREFIX+"/login"))
				return;
			System.out.println("Checking authentication for path: "+request.pathInfo());
		});
	}

	public void moviesService() {
		jget("/movies/search/:search_query", (request, response) -> {
			String searchQuery = request.params("search_query").replaceAll("\\+", " ");
			System.out.println("Searched for \""+searchQuery+"\"");
			throw new HTTPException(501);
		});

		Route movieRoute = (request, response) -> {
			String movieId = request.params("id");
			System.out.println("Looked up movie with ID: "+movieId);
			throw new HTTPException(501);
		};
		jget("/movies/:id", movieRoute);
		jget("/movies/:id/*", movieRoute);
	}

	public void friendsService() {
		jget("/friends/search/:search_query", (request, response) -> {
			String searchQuery = request.params("search_query").replaceAll("\\+", " ");
			System.out.println("Searched for \""+searchQuery+"\"");
			throw new HTTPException(501);
		});

		Route friendRoute = (request, response) -> {
			String movieId = request.params("id");
			System.out.println("Looked up movie with ID: "+movieId);
			throw new HTTPException(501);
		};
		jget("/friends/:id", friendRoute);
		jget("/friends/:id/*", friendRoute);
	}

	public void groupsService() {
		jget("/groups/search/:search_query", (request, response) -> {
			String searchQuery = request.params("search_query").replaceAll("\\+", " ");
			System.out.println("Searched for \""+searchQuery+"\"");
			throw new HTTPException(501);
		});

		Route groupsRoute = (request, response) -> {
			String movieId = request.params("id");
			System.out.println("Looked up movie with ID: "+movieId);
			throw new HTTPException(501);
		};
		jget("/groups/:id", groupsRoute);
		jget("/groups/:id/*", groupsRoute);
	}

	public String renderHTML(String htmlFile) {
		try {
			URL url = MovieMatcherApplication.class.getClassLoader().getResource("WEB-INF/"+htmlFile);
			if (url != null) {
				Path path = Paths.get(url.toURI());
				return new String(Files.readAllBytes(path), Charset.defaultCharset());
			}
		}
		catch (Exception e) {
			System.out.println("Could not find "+htmlFile);
			e.printStackTrace();
		}
		return null;
	}
}
