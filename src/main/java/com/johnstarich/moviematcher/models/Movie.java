package com.johnstarich.moviematcher.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.List;
import java.util.ArrayList;
import static java.util.Arrays.asList;

import static com.mongodb.client.model.Filters.eq;


/**
 * Created by Josue on 3/10/2016.
 */
public class Movie {
	private static class LazyMoviesCollection {
		public static final MongoCollection<Document> moviesCollection = MovieMatcherDatabase.getCollection("movies");
	}
	private static MongoCollection<Document> getCollection() {
		return LazyMoviesCollection.moviesCollection;
	}

	public final ObjectId _id;
	public final String title;
	public final String rating;
	public final String genre;
	public final String release_date;
	public final String imdb_rating;
	public final String poster;
	public final String plot;
	public final String movie_lang;

	public Movie(ObjectId _id) {
		this._id = _id;
		this.title = null;
		this.rating = null;
		this.genre = null;
		this.release_date = null;
		this.imdb_rating = null;
		this.poster = null;
		this.plot = null;
		this.movie_lang = null;
	}

	public Movie(ObjectId _id, String title, String rating, String genre, String release_date, String imdb_rating, String poster,
				 String plot, String movie_lang) {
		this._id = _id;
		this.title = title;
		this.rating = rating;
		this.genre = genre;
		this.release_date = release_date;
		this.imdb_rating = imdb_rating;
		this.poster = poster;
		this.plot = plot;
		this.movie_lang = movie_lang;
	}

	@Override
	public boolean equals(Object o) {
		return o == this || o instanceof Movie && ((Movie) o)._id == _id;
	}

	@Override
	public int hashCode() {
		return _id.hashCode();
	}

	public Movie load() {
		Document movie = getCollection().find(eq("_id", _id)).first();
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(movie.toJson(), Movie.class);
	}

	public static List<Movie> search(String query) {
		AggregateIterable<Document> iterable = getCollection().aggregate(
				asList( new Document("$match", new Document("$text", new Document("$search", query))),
						new Document("$project",
								new Document("title", true).append("rating", true).
								append("genre", true).append("release_date", true).
								append("imdb_rating", true).append("poster", true).
								append("plot",true).append("movie_lang", true).
								append("score", new Document("$meta", "textScore"))),
						new Document("$sort", new Document("score", -1))
					)
		);

		ArrayList<Movie> queryResults = new ArrayList<Movie>();
		Gson gson = new GsonBuilder().create();
		
		iterable
			.map(document -> gson.fromJson(document.toJson(), Movie.class))
			.forEach((Block<Movie>) m -> queryResults.add(m));

		return queryResults;
	}
}
