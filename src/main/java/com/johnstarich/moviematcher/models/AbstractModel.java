package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.annotations.Id;
import de.caluga.morphium.annotations.Transient;
import de.caluga.morphium.query.Query;
import org.bson.types.ObjectId;

import java.util.Collections;
import java.util.List;

/**
 * Created by johnstarich on 4/19/16.
 */
@Entity
public abstract class AbstractModel<T extends AbstractModel> {
	@Id
	public final ObjectId id;

	@Transient
	private final Class<T> clazz;

	public AbstractModel(Class<T> clazz, ObjectId id) {
		this.id = id;
		this.clazz = clazz;
	}

	public boolean exists() {
		return MovieMatcherDatabase.morphium.getId(this) != null;
	}

	public T save() {
		MovieMatcherDatabase.morphium.store(this);
		return (T) this;
	}

	public T load() {
		return (T) MovieMatcherDatabase.morphium.getId(this);
	}

	public List<T> search(String query, int results, int page) {
		return search(clazz, query, results, page);
	}

	public static <T extends AbstractModel> List<T> search(Class<T> clazz, String query, int results, int page) {
		Query<T> searchQuery = MovieMatcherDatabase.morphium.createQueryFor(clazz)
			.text(query)
			.skip(results * (page - 1))
			.limit(results);
		return MovieMatcherDatabase.morphium.createAggregator(clazz, clazz)
			.match(searchQuery)
			.project(Collections.singletonMap("score", Collections.singletonMap("$meta", "textScore")))
			.sort("score")
			.aggregate();
	}
}
