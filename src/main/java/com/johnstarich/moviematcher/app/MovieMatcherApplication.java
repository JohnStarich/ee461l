package com.johnstarich.moviematcher.app;

import com.johnstarich.moviematcher.models.AbstractModel;
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
import java.util.List;

/**
 * Movie Matcher API is defined here. These routes make up the Movie Matcher services.
 * Created by johnstarich on 2/25/16.
 */
public class MovieMatcherApplication extends JsonApplication {
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
		Spark.get("/assets/*", new ServeStaticFileRoute());
		Spark.get("/*", new ServeStaticFileRoute("/index.html"));
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
			int results = asIntOpt(request.queryParams("results")).orElse(20);
			int page = asIntOpt(request.queryParams("page")).orElse(1);
			return AbstractModel.search(Movie.class, searchQuery, results, page);
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
}
