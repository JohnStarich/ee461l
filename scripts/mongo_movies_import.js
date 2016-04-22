print("starting");
print("creating textIndex for title, genre, and plot\n setting weights: title(10), genre(5), plot(3)");
db.movie.createIndex({title:"text", genre:"text", plot:"text"},
	{weights: {title: 10, genre: 5, plot: 3}},
	name = "textIndex");
print("removing movies without released dates");
db.movie.remove({Released: ""});
print("removing movies without a year");
db.movie.remove({Year: ""});
print("removing movies prior to 1900");
db.movie.remove({Year:{$lt:1900}})
print("creating ISODate for each movie");
db.movie.find().forEach(function(movie){
	var date = new Date(movie.Released);
	if(movie.Year > 1999)
	{
		date.setYear(movie.Year);
	}
	db.movie.update({_id:movie._id},
		{$set:
			{release_date:date}
		});
});
print("renaming fields");
db.movie.update({}, {$rename: 
	{'ID': 'omdb_id', 'Title':'title', 'Rating': 'rating',
	'Genre': 'genre', 'imdbRating': 'imdb_rating',
	'Poster': 'poster', 'Plot': 'plot', 'Language': 'movie_lang'}}, {multi: true});
print("unsetting fields")
db.movie.update({}, {$unset: {imdbID:1, Runtime:1, Director:1, 
	Writer:1, Cast:1, Metacritic:1, imdbVotes:1, FullPlot:1, Country:1, 
	Awards:1, Year:1, Released:1, lastUpdated:1}}, {multi:true});
print("compacting");
db.runCommand({compact: 'movie'});
print("done");


