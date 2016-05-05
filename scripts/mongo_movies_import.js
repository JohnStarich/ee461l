print("starting");
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
	var newPoster;
	if(movie.Poster && movie.Poster.trim() !== '') {
		newPoster = "http://img.omdbapi.com/?i=" + movie.imdbID;
	}
	else {
		newPoster = undefined;
	}
	db.movie.update({_id:movie._id},
		{$set:
			{
				release_date: date,
				Poster: newPoster
			}
		});
});
print("renaming fields");
db.movie.update({}, {$rename:
	{'imdbID': 'imdb_id', 'ID': 'omdb_id', 'Title':'title', 'Rating': 'rating',
	'Genre': 'genre', 'imdbRating': 'imdb_rating',
	'Poster': 'poster', 'Plot': 'plot', 'Language': 'movie_lang'}}, {multi: true});
print("unsetting fields")
db.movie.update({}, {$unset: {Runtime:1, Director:1,
	Writer:1, Cast:1, Metacritic:1, imdbVotes:1, FullPlot:1, Country:1, 
	Awards:1, Year:1, Released:1, lastUpdated:1}}, {multi:true});
print("compacting");
db.runCommand({compact: 'movie'});
print("done");

