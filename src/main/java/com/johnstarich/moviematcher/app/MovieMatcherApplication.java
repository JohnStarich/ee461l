package com.johnstarich.moviematcher.app;

import com.johnstarich.moviematcher.models.Movie;
import com.johnstarich.moviematcher.models.Status;
import org.bson.types.ObjectId;
import spark.Route;
import spark.Spark;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Movie Matcher API is defined here. These routes make up the Movie Matcher services.
 * Created by johnstarich on 2/25/16.
 */
public class MovieMatcherApplication extends JsonApplication {
	/** Home page HTML to be rendered for any page without a route specified. */
	private final String INDEX_HTML = renderHTML("index.html");

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

		htmlService();
	}

	public void htmlService() {
		Route index = (request, response) -> INDEX_HTML;
		Spark.get("/login", index);
		Spark.get("/login/*", index);
		Spark.get("/movies", index);
		Spark.get("/movies/*", index);
		Spark.get("/friends", index);
		Spark.get("/friends/*", index);
		Spark.get("/groups", index);
		Spark.get("/groups/*", index);
	}

	/**
	 * Register login services, like registration and user login API,
	 * as well as authentication services.
	 */
	public void loginService() {
		jpost("/login", (request, response) -> {
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		});

		jpost("/login/register", (request, response) -> {
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
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

	/**
	 * Register movie services, like movie search and movie ID lookup
	 */
	public void moviesService() {
		jget("/movies/search/:search_query", (request, response) -> {
			String searchQuery = request.params("search_query").replaceAll("\\+", " ");
			System.out.println("Searched for \""+searchQuery+"\"");
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		});

		Route movieRoute = (request, response) -> {
			String movieId = request.params("id");
			System.out.println("Looked up movie with ID: "+movieId);
			return new Movie(new ObjectId(movieId)).load();
		};
		jget("/movies/:id", movieRoute);
		jget("/movies/:id/*", movieRoute);

		Route unimplemented = (request, response) -> {
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		};
		jget("/movies", unimplemented);
		jget("/movies/*", unimplemented);
	}

	/**
	 * Register friend services, like friend search and friend ID lookup
	 */
	public void friendsService() {
		jget("/friends/search/:search_query", (request, response) -> {
			String searchQuery = request.params("search_query").replaceAll("\\+", " ");
			System.out.println("Searched for \""+searchQuery+"\"");
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		});

		Route friendRoute = (request, response) -> {
			String movieId = request.params("id");
			System.out.println("Looked up movie with ID: "+movieId);
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		};
		jget("/friends/:id", friendRoute);
		jget("/friends/:id/*", friendRoute);

		Route unimplemented = (request, response) -> {
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		};
		jget("/friends", unimplemented);
		jget("/friends/*", unimplemented);
	}

	/**
	 * Register group services, like group search and group ID lookup
	 */
	public void groupsService() {
		jget("/groups/search/:search_query", (request, response) -> {
			String searchQuery = request.params("search_query").replaceAll("\\+", " ");
			System.out.println("Searched for \""+searchQuery+"\"");
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		});

		Route groupsRoute = (request, response) -> {
			String movieId = request.params("id");
			System.out.println("Looked up movie with ID: "+movieId);
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		};
		jget("/groups/:id", groupsRoute);
		jget("/groups/:id/*", groupsRoute);

		Route unimplemented = (request, response) -> {
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		};
		jget("/groups", unimplemented);
		jget("/groups/*", unimplemented);
	}

	/**
	 * Render an HTML file as a String we can send in a response body.
	 * @param htmlFile the file location (relative to WEB-INF folder)
	 * @return the content of that file
	 */
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
