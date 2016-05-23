package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.utils.CheckedRunnable;
import com.johnstarich.moviematcher.utils.HttpException;
import com.johnstarich.moviematcher.utils.HttpStatus;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import com.mongodb.DuplicateKeyException;
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
	@Transient
	protected static final int MODEL_DEFAULT_RESULTS = 20, MODEL_MAX_RESULTS = 100, MODEL_MIN_PAGE = 1;

	@Id
	public final ObjectId id;

	@Transient
	private Class<T> clazz;

	public AbstractModel(Class<T> clazz, ObjectId id) {
		this.id = id;
		this.clazz = clazz;
	}

	public boolean exists() {
		return load().isPresent();
	}

	public T save() throws HttpException {
		handleMongoExceptions(clazz, () -> MovieMatcherDatabase.morphium.store(this));
		return (T) this;
	}

	public T update() throws HttpException {
		handleMongoExceptions(clazz, () -> {
			String[] fieldNames = getNonNullFieldsNames(clazz, this).toArray(new String[0]);
			MovieMatcherDatabase.morphium.updateUsingFields(this, fieldNames);
		});
		return (T) this;
	}

	public Optional<T> load(){
		return load(clazz);
	}

	public Optional<T> load(Class<T> clazz) {
		List<T> results = MovieMatcherDatabase.morphium.findByField(clazz, "id", id);
		if(results.isEmpty()) return Optional.empty();
		else {
			T result = results.get(0);
			((AbstractModel<T>) result).clazz = clazz;
			return Optional.ofNullable(results.get(0));
		}
	}

	public T delete() {
		MovieMatcherDatabase.morphium.delete(this);
		return (T) this;
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
			.text(query);
		return MovieMatcherDatabase.morphium.createAggregator(clazz, clazz)
			.match(searchQuery)
			.project(getProjectFields(clazz))
			.sort("-score")
			.skip(results * (page - 1))
			.limit(results)
			.aggregate();
	}

	private static void handleMongoExceptions(Class clazz, CheckedRunnable runnable) throws HttpException {
		try {
			runnable.run();
		}
		catch(RuntimeException e) {
			if(e.getCause() != null) {
				Throwable cause = e.getCause();
				if(cause instanceof DuplicateKeyException) {
					throw new HttpException(HttpStatus.BAD_REQUEST, "Duplicate " + clazz.getSimpleName() + " cannot be saved");
				}
				else cause.printStackTrace();
			}
			throw e;
		}
	}

	private static Map<String, Object> getProjectFields(Class clazz) {
		Map<String, Object> map = new HashMap<>();
		map.put("score", Collections.singletonMap("$meta", "textScore"));
		getFieldNames(clazz).forEach(f -> map.put(f, true));
		return map;
	}

	private static List<String> getFieldNames(Class clazz) {
		//noinspection unchecked
		return MovieMatcherDatabase.morphium.getARHelper().getFields(clazz);
	}

	private static List<String> getNonNullFieldsNames(Class clazz, Object instance) {
		List<String> fieldNames = getFieldNames(clazz);
		return MovieMatcherDatabase.morphium.getARHelper().getAllFields(clazz).parallelStream()
			.filter(field -> fieldNames.contains(field.getName()))
			.filter(field -> {
				try {
					return field.get(instance) != null;
				}
				catch(IllegalAccessException e) {
					// should never occur because we access public fields only
					e.printStackTrace();
				}
				return false;
			})
			.map(Field::getName)
			.collect(Collectors.toList());
	}
}
