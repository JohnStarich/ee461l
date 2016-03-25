# Homework 2: Design in Our Project

## Information Hiding
The information hiding principle is being applied to the MoviesDatabase class as we hide the implementation of connecting to our database. The reason we decided to hide this implementation is because it could possibly change. Some of the changes include where the database is located, the database we are connecting to, and how we connect to the database. Since we hide this implementation, it will be easy enough to make these changes without having to modify the other classes. One potential disadvantage that could arise is where we would have to refactor most of the whole class if we switched to a completely different database. In order to mitigate this problem, we decided early to choose MongoDB because it best fit our design constraints.

```java
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
```

This load method is provided by our Movie class so that the application interacts solely with movies and not the database in which they are stored. The load method is designed for a future change in the kind of database we use. If we encounter an issue with MongoDB, then we could change the database we use and only have to change this load method to work with the new database. Then the application would be able to use the load method as before and still get a movie object. The Movie class is designed in such a way that additional methods to interact with the movies database in a variety of ways can be added to this class. 

```java
public class Movie {
    ...

    public Movie load() {
        MongoCollection<Document> collection = MoviesDatabase.getCollection("movies");
        Document movie = collection.find(eq("_id", _id)).first();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(movie.toJson(), Movie.class);
    }
}
```

## Design Patterns

### Singleton
In our MoviesDatabase class, we designed it to function as a singleton class. The only constructor is private. Additionally there is only one instance of the class stored in a private, immutable, and static field. The purpose of this class using the singleton design pattern is so that there is only one MongoDB connection at any time. This connection is reused every time someone asks for access to a MongoDB collection so the pattern helps cut down on unneeded network and memory usage.

```java
public class MoviesDatabase {
	private static final MoviesDatabase database = new MoviesDatabase();
	private MongoDatabase mongoDatabase;

	private MoviesDatabase() {...}

	public static MongoCollection<Document> getCollection(String collectionName) {
		return database.mongoDatabase.getCollection(collectionName);
	}
}
```

### Builder
In our Movie class, we implement the builder pattern. In methods such as `load()`, `save()`, `update()`, and `combine(Movie m)`, we are able to construct and fill in various data fields of a Movie and then modify the associate MongoDB document. By following the builder pattern, we manipulate the object’s internal representation and control the object’s construction. 

```java
public class Movie { 
	public final ObjectId _id;
	public final String title; 

	public Movie(ObjectId _id) {...}
	public Movie(...) {...}
	public Movie load() {...}
	public Movie save() {...}
	public Movie update() {...}
	public Movie combine(Movie m) {...}
}
```

### MVC
Our application is based off the model view controller design pattern. In our web application, the model of a movie is a small HTML template showing the title, movie poster, and rating. The view is the user interface that connects the user to our movie controller. The controller is the application’s API.

For example, a user can search for movies through the provided search bar in the UI. The view would then send the search query to the API. The API would then query the database with the user’s search text and return the movie results. These movies are then sent to the view to show the user.

#### View

```html
<div class="movie">
	<img class="movie-image" src="movie_poster.png" />
	<span class="movie-title">Movie Title</span>
	<span class="movie-rating" data-rating="3">&star;&star;&star;</span>
</div>
```

#### Model

```java
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

	...
}
```

#### Controller

```java
public class Movie {
	...
	
	public Movie load() {...}
	public Movie save() {...}
	public Movie update() {...}
	public Movie combine(Movie m) {...}
	public static List<Movie> search(String query) {...}
}
```
