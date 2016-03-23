package com.johnstarich.moviematcher.store;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Created by johnstarich on 3/22/16.
 */
public class MoviesDatabase {
	private static final MoviesDatabase database = new MoviesDatabase();

	private MongoDatabase mongoDatabase;

	private MoviesDatabase() {
		String mongoHost = System.getenv("MONGO_HOST"); //gets an environment variable (this is where the db is located)
		if(mongoHost == null) {
			throw new EnvironmentError("Cannot get environment variable to database.");
		}

		MongoClient mongoClient = new MongoClient(mongoHost);
		this.mongoDatabase = mongoClient.getDatabase("moviematcher");
	}

	public static MongoCollection<Document> getCollection(String collectionName) {
		return database.mongoDatabase.getCollection(collectionName);
	}
}

class EnvironmentError extends Error {
	public EnvironmentError(String message) {
		super(message);
	}
}