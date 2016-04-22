#!/bin/bash
mongoimport --db moviematcher --drop --collection movie --type csv --headerline --file omdbMovies.csv
# mongoexport -d moviematcher -c omdb | mongoimport -d moviematcher -c movies --drop
mongo moviematcher mongo_movies_import.js
