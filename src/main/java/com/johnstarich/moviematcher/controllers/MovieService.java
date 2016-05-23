package com.johnstarich.moviematcher.controllers;

import com.johnstarich.moviematcher.utils.HttpException;
import com.johnstarich.moviematcher.utils.HttpStatus;
import com.johnstarich.moviematcher.models.AbstractModel;
import com.johnstarich.moviematcher.models.Movie;
import com.johnstarich.moviematcher.models.Rating;
import com.johnstarich.moviematcher.routes.AuthenticatedRoute;
import org.bson.types.ObjectId;
import spark.Route;

import java.util.*;

/**
 * Register movie services, like movie search and movie ID lookup
 * Created by johnstarich on 5/22/16.
 */
public class MovieService extends JsonService {
	@Override
	public String resource() {
		return "movies";
	}

	@Override
	public void initService() {
		AuthenticatedRoute searchRoute = (request, response, user) -> {
			Optional<String> queryParam = Optional.ofNullable(request.params("search_query"));
			String searchQuery = queryParam.orElse("").replaceAll("\\+", " ").trim();
			System.out.println("Searched for \"" + searchQuery + "\"");
			int results = asIntOpt(request.queryParams("results")).orElse(20);
			int page = asIntOpt(request.queryParams("page")).orElse(1);
			if (searchQuery.equals("") || results == 0)
				return Collections.EMPTY_LIST;

			List<Movie> movies = AbstractModel.search(Movie.class, searchQuery, results, page);
			Map<ObjectId, Integer> ratingsMap = new HashMap<>();

			for (Movie m : movies) {
				Optional<Rating> r = Rating.loadRatingByUser(user.id, m.id);
				if (r.isPresent()) {
					ratingsMap.put(m.id, r.get().numeric_rating);
				} else {
					ratingsMap.put(m.id, null);
				}
			}

			Map<String, Object> ret = new HashMap<>();
			ret.put("movies", movies);
			ret.put("ratings", ratingsMap);
			return ret;
		};

		jget("search/:search_query", searchRoute);
		jget("search/", searchRoute);
		jget("search", searchRoute);

		Route movieRoute = (request, response) -> {
			String movieId = request.params("id");
			System.out.println("Looked up movie with ID: " + movieId);
			return new Movie(new ObjectId(movieId)).load(Movie.class);
		};

		jget(":id", movieRoute);
		jget(":id/*", movieRoute);

		Route unimplemented = (request, response) -> {
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		};
		jget(unimplemented);
		jget("/*", unimplemented);
	}
}
