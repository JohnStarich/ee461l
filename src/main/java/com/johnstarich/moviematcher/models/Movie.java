package com.johnstarich.moviematcher.models;

import com.mongodb.MongoClient;
import org.bson.types.ObjectId;

/**
 * Created by Josue on 3/10/2016.
 */
public class Movie {
    private String title;
    private String rating;
    private String genre;
    private String release_date;
    private String imdb_rating;
    private String poster;
    private String plot;
    private String language;

    public Movie(String title, String rating, String genre, String release_date, String imdb_rating, String poster,
                 String plot, String language) {
        this.title = title;
        this.rating = rating;
        this.genre = genre;
        this.release_date = release_date;
        this.imdb_rating = imdb_rating;
        this.poster = poster;
        this.plot = plot;
        this.language = language;
    }

    public static Movie load(ObjectId id) throws Exception {
        String mongoHost = System.getenv("MONGO_HOST"); //gets an environment variable (this is where the db is located)
        if(mongoHost == null) {
            throw new Exception("Cannot get environment variable to database.");
        }

        MongoClient mongoClient = new MongoClient(mongoHost);

        return null;
    }
}
