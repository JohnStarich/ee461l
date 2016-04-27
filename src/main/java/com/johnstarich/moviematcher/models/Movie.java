package com.johnstarich.moviematcher.models;

import de.caluga.morphium.annotations.Index;
import org.bson.types.ObjectId;

import java.util.Date;


/**
 * Created by Josue on 3/10/2016.
 */
@Index("title:text")
public class Movie extends AbstractModel<Movie> {
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
<<<<<<< Updated upstream
		return o == this || o instanceof Movie && ((Movie) o).id.equals(id);
=======
		return o == this || o instanceof Movie && ((Movie) o)._id == _id;
	}

	@Override
	public int hashCode() {
		return _id.hashCode();
	}

	public Movie load() {
		Document movie = getCollection().find(eq("_id", _id)).first();
		return gson.fromJson(movie.toJson(), Movie.class);
	}

	public static List<Movie> search(String query, int results, int page) {
		AggregateIterable<Document> iterable = getCollection().aggregate(
				asList( new Document("$match",
						new Document("$text", new Document("$search", query))),
						new Document("$project",
								new Document("title", true)
									.append("rating", true)
									.append("genre", true)
									.append("release_date", true)
									.append("imdb_rating", true)
									.append("poster", true)
									.append("plot",true)
									.append("movie_lang", true)
									.append("score", new Document("$meta", "textScore"))),
						new Document("$sort", new Document("score", -1)),
						new Document("$limit", results),
						new Document("$skip", (page-1) * results )
				)
		);

		ArrayList<Movie> queryResults = new ArrayList<Movie>();

		iterable
			.map(document -> gson.fromJson(document.toJson(), Movie.class))
			.forEach((Block<Movie>) m -> queryResults.add(m));

		return queryResults;
	}

	public static List<Movie> search(String query) {
		return search(query, 20, 1);
>>>>>>> Stashed changes
	}
}
