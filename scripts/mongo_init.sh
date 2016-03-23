#!/bin/bash
mongoimport --db moviematcher --collection movies --type csv --headerline --file omdbMovies.csv
# mongoexport -d moviematcher -c omdb | mongoimport -d moviematcher -c movies --drop
mongo moviematcher mongo_movies_import.js
