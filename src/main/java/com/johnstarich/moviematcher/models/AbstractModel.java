package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.HttpException;
import com.johnstarich.moviematcher.app.HttpStatus;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import de.caluga.morphium.annotations.Entity;
import de.caluga.morphium.annotations.Id;
import de.caluga.morphium.annotations.Transient;
import de.caluga.morphium.query.Query;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base for models stored in the database
 * Created by johnstarich on 4/19/16.
 */
@Entity
public abstract class AbstractModel<T extends AbstractModel> {
	protected static final int MODEL_DEFAULT_RESULTS = 20, MODEL_MAX_RESULTS = 100, MODEL_MIN_PAGE = 1;

	@Id
	public final ObjectId id;

	@Transient
	private final Class<T> clazz;

	public AbstractModel(Class<T> clazz, ObjectId id) {
		this.id = id;
		this.clazz = clazz;
	}

	public boolean exists() {
		return load().isPresent();
	}

	public T save() {
		MovieMatcherDatabase.morphium.store(this);
		return (T) this;
	}

	public Optional<T> load() {
		List<T> results = MovieMatcherDatabase.morphium.findByField(clazz, "id", id);
		if(results.isEmpty()) return Optional.empty();
		else return Optional.of(results.get(0));
	}

	public List<T> search(String query) throws HttpException {
		return search(clazz, query, MODEL_DEFAULT_RESULTS, MODEL_MIN_PAGE);
	}

	public List<T> search(String query, int results, int page) throws HttpException {
		return search(clazz, query, results, page);
	}

	public static final <T extends AbstractModel> List<T> search(Class<T> clazz, String query) throws HttpException {
		return search(clazz, query, MODEL_DEFAULT_RESULTS, MODEL_MIN_PAGE);
	}

	public static final <T extends AbstractModel> List<T> search(Class<T> clazz, String query, int results, int page) throws HttpException {
		if(results < 1 || results > MODEL_MAX_RESULTS) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "Results must be a positive integer no greater than " + MODEL_MAX_RESULTS);
		}
		if(page < MODEL_MIN_PAGE) {
			throw new HttpException(HttpStatus.BAD_REQUEST, "Page must be a positive integer");
		}
		MovieMatcherDatabase.morphium.ensureIndicesFor(clazz);
		Query<T> searchQuery = MovieMatcherDatabase.morphium.createQueryFor(clazz)
			.text(query)
			.skip(results * (page - 1))
			.limit(results);
		return MovieMatcherDatabase.morphium.createAggregator(clazz, clazz)
			.match(searchQuery)
			.project(getProjectFields(clazz))
			.sort("score")
			.aggregate();
	}

	private static Map<String, Object> getProjectFields(Class clazz) {
		Map<String, Object> map = new HashMap<>();
		map.put("score", Collections.singletonMap("$meta", "textScore"));
		getFieldNames(clazz).forEach(f -> map.put(f, true));
		return map;
	}

	private static List<String> getFieldNames(Class clazz) {
		return Arrays.stream(clazz.getFields())
			.map(Field::getName)
			.collect(Collectors.toList());
	}
}
