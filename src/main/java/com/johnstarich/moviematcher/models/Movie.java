package com.johnstarich.moviematcher.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import de.caluga.morphium.annotations.Index;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.List;
import java.util.ArrayList;
import static java.util.Arrays.asList;

import static com.mongodb.client.model.Filters.eq;


/**
 * Created by Josue on 3/10/2016.
 */
@Index("title:text")
public class Movie extends AbstractModel<Movie> {
	public final String title;
	public final String rating;
	public final String genre;
	public final String release_date;
	public final String imdb_rating;
	public final String poster;
	public final String plot;
	public final String movie_lang;

	public Movie(ObjectId id) {
		super(Movie.class, id);
		this.title = null;
		this.rating = null;
		this.genre = null;
		this.release_date = null;
		this.imdb_rating = null;
		this.poster = null;
		this.plot = null;
		this.movie_lang = null;
	}

	public Movie(ObjectId id, String title, String rating, String genre, String release_date, String imdb_rating, String poster,
				 String plot, String movie_lang) {
		super(Movie.class, id);
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
		return o == this || o instanceof Movie && ((Movie) o).id.equals(id);
	}
}
