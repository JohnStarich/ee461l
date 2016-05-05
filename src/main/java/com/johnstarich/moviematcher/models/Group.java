package com.johnstarich.moviematcher.models;

import com.johnstarich.moviematcher.app.HttpException;
import com.johnstarich.moviematcher.app.HttpStatus;
import de.caluga.morphium.annotations.Index;
import de.caluga.morphium.annotations.Reference;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.Map.Entry;

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

    public Map<String, Object> suggestMovies(User me) throws HttpException {
       /* bring the ratings in O(Users) */
        if(members == null) throw new HttpException(HttpStatus.BAD_REQUEST, "Please add friends to your group.");
        List<User> membersAndI = new ArrayList<>(this.members);
        membersAndI.add(me);
        //really don't want to create local variable ....
        List<Rating> groupRatings = new ArrayList<>();
        membersAndI.parallelStream().forEach(
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
                    if(movie.get().genre != null) {
                        genres.addAll(Arrays.asList(movie.get().genre.split(", ")));
                    }
                }
            }
        );

        Map<String, Integer> genreMap = new HashMap();
        genres.stream().forEach(
            g -> {
                if(genreMap.containsKey(g)) {
                    genreMap.put(g, genreMap.get(g)+1);
                } else {
                    genreMap.put(g, 1);
                }
            }
        );

        /* we have now mapped the most rated genres */
        List<Entry<String, Integer>> list = new LinkedList<>(genreMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<>();

        list.stream().forEach(
            entry -> sortedMap.put(entry.getKey(), entry.getValue())
        );

        Optional<List<Movie>> foundMovies = Movie.searchByGenre(sortedMap.entrySet().iterator().next().getKey());
        if(! foundMovies.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "Couldn't find any movies ...");

        Map<ObjectId, Integer> ratingsMap = new HashMap<>();
        for(Movie m: foundMovies.get()) {
            Optional<Rating> r = Rating.loadRatingByUser(me.id, m.id);
            if(r.isPresent()) {
                ratingsMap.put(m.id, r.get().numeric_rating);
            }
            else {
                ratingsMap.put(m.id, null);
            }
        }

        Map<String, Object> ret = new HashMap<>();
        ret.put("movies", foundMovies.get());
        ret.put("ratings", ratingsMap);
        return ret;
    }
}
