package com.johnstarich.moviematcher.models;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.eq;


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
        MongoDatabase database = mongoClient.getDatabase("moviematcher");
        MongoCollection<Document> collection = database.getCollection("movies");

        //using first()because there should only be one movie with the specific "id"
        //however, we could get an iterable and search through that ... but again there should only be one!
        Document movie = collection.find(eq("_id",id)).first();


        return new Movie(movie.get("title").toString(), movie.get("rating").toString(), movie.get("genre").toString(),
                movie.get("release_date").toString(), movie.get("imdb_rating").toString(),
                movie.get("poster").toString(), movie.get("plot").toString(), movie.get("language").toString());
    }

    public String getTitle() {return title;}
    public String getRating() {return rating;}
    public String getGenre() {return genre;}
    public String getReleaseDate() {return release_date;}
    public String getImdbRating() {return imdb_rating;}
    public String getPoster() {return poster;}
    public String getPlot() {return plot;}
    public String getLanguage() {return language;}
}
