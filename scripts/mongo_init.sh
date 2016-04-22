#!/bin/bash -e
# If you run into issues with file encoding, run this:
#   iconv -f LATIN1 -t UTF8 -o omdbMovies-utf8.csv omdbMovies.csv
# Then re run the next line with the new CSV file
mongoimport --db moviematcher --drop --collection movie --type csv --headerline --file omdbMovies.csv
# mongoexport -d moviematcher -c omdb | mongoimport -d moviematcher -c movies --drop
mongo moviematcher mongo_movies_import.js
