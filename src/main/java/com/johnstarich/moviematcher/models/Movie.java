package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import de.caluga.morphium.annotations.Index;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;


/**
 * Created by Josue on 3/10/2016.
 */
@Index("title:text,genre:text")
public class Movie extends AbstractModel<Movie> {
	@Index(options={"weight:5"})
	public final String title;
	public final String rating;
	public final String genre;
	public final Date release_date;
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

	public Movie(ObjectId id, String title, String rating, String genre, Date release_date, String imdb_rating, String poster,
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


	public static Optional<List<Movie>> searchByGenre(String genre) {
		return Optional.ofNullable(MovieMatcherDatabase.morphium
			.findByField(Movie.class, "genre", genre)
			.parallelStream()
			.filter(movie -> movie.imdb_rating != null && movie.imdb_rating.compareTo("") != 0 && Double.parseDouble(movie.imdb_rating) >= 8.0)
			.limit(20).collect(Collectors.toList()));
	}

/*	public static Optional<List<Movie>> searchByGenre(Map<String, Integer> genreMap) {
		//genreMap.keySet().stream().filter(genreMap::containsKey).forEach(k->genreMap.put(k, genreMap.get(k)/20));
		List<Movie> movies = new ArrayList<>();
		for(Map.Entry<String, Integer> e : genreMap.entrySet()) {
			MovieMatcherDatabase.morphium
					.findByField(Movie.class, "genre", e.getKey())
					.parallelStream()
					.limit(e.getValue()/20)
					.collect(Collectors.toList());
		}
		return Optional.ofNullable(movies);
	}*/
}
