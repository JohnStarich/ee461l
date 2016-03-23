package com.johnstarich.moviematcher.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.johnstarich.moviematcher.store.MoviesDatabase;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.omg.CORBA.Object;

import static com.mongodb.client.model.Filters.eq;


/**
 * Created by Josue on 3/10/2016.
 */
public class Movie {
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
        title = null;
        rating = null;
        genre = null;
        release_date = null;
        imdb_rating = null;
        poster = null;
        plot = null;
        movie_lang = null;
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

    public Movie load() {
        MongoCollection<Document> collection = MoviesDatabase.getCollection("movies");
        Document movie = collection.find(eq("_id", _id)).first();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(movie.toJson(), Movie.class);
    }
}
