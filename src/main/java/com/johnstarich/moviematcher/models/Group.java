package com.johnstarich.moviematcher.models;

import de.caluga.morphium.annotations.Index;
import de.caluga.morphium.annotations.Reference;
import org.bson.types.ObjectId;

import javax.crypto.spec.OAEPParameterSpec;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Optional<List<Movie>> suggestMovies() {
       /* bring the ratings in O(Users) */
        if(members == null) return Optional.empty();
        members.parallelStream().filter(user -> {Rating.loadRatingsByUser(user.id).});
       /* filter ratings (all of them) */
       /* what do we want to filter ratings by (greater than 3 or 4) */
       /* let us go load the movies (We have to load the movies) (but if we can filter down to smaller subset, that's perfect)*/
       /* for every movie , split genre and throw it into the map */
        return Optional.empty();
    }
}
