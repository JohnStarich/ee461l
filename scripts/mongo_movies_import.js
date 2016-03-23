print("starting");
print("creating title index");
db.movies.createIndex({title:"text"});
print("renaming fields");
db.movies.update({}, {$rename: 
	{'ID': 'id', 'Title':'title', 'Rating': 'rating', 
	'Genre': 'genre', 'Released': 'released', 'imdbRating': 'imdb_rating',
	'Poster': 'poster', 'Plot': 'plot', 'Language': 'movie_lang'}}, {multi: true});
print("unsetting fields")
db.movies.update({}, {$unset: {imdbID:1, Year:1, Runtime:1, Director:1, 
	Writer:1, Cast:1, Metacritic:1, imdbVotes:1, FullPlot:1, Country:1, 
	Awards:1, lastUpdate:1}}, {multi:true});
print("compacting");
db.runCommand({compact: 'movies'});
print("done");