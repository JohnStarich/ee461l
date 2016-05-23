package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.utils.CountedSet;
import com.johnstarich.moviematcher.utils.HttpException;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import de.caluga.morphium.annotations.Index;
import de.caluga.morphium.annotations.Reference;
import de.caluga.morphium.query.Query;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Josue on 4/16/2016.
 */
@Index("name:text")
public class Group extends AbstractModel<Group> {
    public final String name;
    @Reference
    public final List<User> members;

    public Group(ObjectId id) {
        super(Group.class, id);
        this.name = "";
        this.members = new ArrayList<>(0);
    }

    public Group(ObjectId id, String name) {
        super(Group.class, id);
        this.name = name;
        this.members = new ArrayList<>(0);
    }

    public Group(ObjectId id, String name, List<User> members) {
        super(Group.class, id);
        this.name = name;
        this.members = members;
    }

    public Group addFriend(User friend) {
        ArrayList<User> members = new ArrayList<>(this.members);
        members.add(friend);
        return new Group(id, name, members);
    }

    public Group removeFriendWithId(ObjectId friend) {
        return new Group(
            id,
            name,
            members.parallelStream()
                .filter(n -> ! n.id.equals(friend))
                .collect(Collectors.toList())
        );
    }

    public Group removeFriend(User friend) {
        ArrayList<User> members = new ArrayList<>(this.members);
        members.remove(friend);
        return new Group(id, name, members);
    }

    public Group renameGroup(String newName) {
        return new Group(id, newName, members);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof Group)) return false;
        return ((Group) o).name != null && ((Group) o).name.equals(name) && ((Group) o).members.size() == members.size();
    }

    public Group noPasswords() {
        return new Group(
            id,
            name,
            members.parallelStream()
                .map(User::noPassword)
                .collect(Collectors.toList())
        );
    }

    public List<Movie> suggestMovies(User me) throws HttpException {
        // get ratings for this group and me
        // find common genres
        // return highest rated (but unrated by group) movies
        List<ObjectId> groupMembers = new ArrayList<>(
            members.parallelStream()
                .map(u -> u.id)
                .collect(Collectors.toList())
        );
        groupMembers.add(me.id);

        Query<Rating> ratings = MovieMatcherDatabase.morphium.createQueryFor(Rating.class)
            .f("user_id").in(groupMembers)
            .limit(100);
        List<ObjectId> movieIds = ratings.asList().parallelStream()
            .map(rating -> rating.movie_id)
            .collect(Collectors.toList());

        Query<Movie> ratedMovies = MovieMatcherDatabase.morphium.createQueryFor(Movie.class)
            .f("_id").in(movieIds)
            .limit(100)
            .skip(0);

        List<String> genres = ratedMovies.asList().parallelStream()
            .map(movie -> movie.genre)
            .map(genre -> genre.split(", "))
            .map(Arrays::asList)
            .flatMap(List::parallelStream)
            .filter(genre -> ! genre.isEmpty())
            .collect(Collectors.toList());
        CountedSet<String> frequencyOfGenres = new CountedSet<>(genres);

        List<Query<Movie>> genreQueries = frequencyOfGenres.keySet().parallelStream()
            .map(genre ->
                MovieMatcherDatabase.morphium.createQueryFor(Movie.class)
                    .f("genre").matches(".*" + genre + ".*")
            )
            .collect(Collectors.toList());
        Query<Movie> recommendations = MovieMatcherDatabase.morphium.createQueryFor(Movie.class)
            .or(genreQueries)
            .f("imdb_rating").ne("")
            .sort("-imdb_rating")
            .limit(20);
        return recommendations.asList();
    }
}
