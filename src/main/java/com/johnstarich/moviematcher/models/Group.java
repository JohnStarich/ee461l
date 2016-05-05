package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.HttpException;
import com.johnstarich.moviematcher.app.HttpStatus;
import de.caluga.morphium.annotations.Index;
import de.caluga.morphium.annotations.Reference;
import org.bson.types.ObjectId;

import javax.crypto.spec.OAEPParameterSpec;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    public Optional<List<Movie>> suggestMovies() throws HttpException {
       /* bring the ratings in O(Users) */
        if(members == null) return Optional.empty();

        //really don't want to create local variable ....
        List<Rating> groupRatings = new ArrayList<>();
        members.parallelStream().forEach(
            user -> {
                Optional<List<Rating>> r = Rating.loadRatingsByUser(user.id);
                if(r.isPresent()) r.get().parallelStream().forEach(groupRatings::add);
            }
        );

        if(groupRatings.isEmpty()) throw new HttpException(HttpStatus.BAD_REQUEST, "Please have each user in " +name+" rate more movies!");
        /* filter ratings (all of them) */
       /* what do we want to filter ratings by (greater than 3 or 4) */
        List<Rating> GroupRatings = groupRatings.parallelStream().filter(movieRating -> movieRating.numeric_rating >= 4).collect(Collectors.toList());

       /* let us go load the movies (We have to load the movies) (but if we can filter down to smaller subset, that's perfect)*/
        List<Optional<Movie>> movies = new ArrayList<>();
        GroupRatings.parallelStream().forEach(rating -> movies.add(new Movie(new ObjectId(rating.movie_id.toString())).load(Movie.class)));

       /* for every movie , split genre and throw it into the map */
        List<String> genres = new ArrayList<>();
        movies.parallelStream().forEach(
            movie -> {
                if(movie.isPresent()) {
                    if(movie.get().genre != null)
                        genres.addAll(Arrays.asList(movie.get().genre.split(", ")));
                }
            }
        );


        return Optional.empty();
    }
}
