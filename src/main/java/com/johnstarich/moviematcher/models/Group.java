package com.johnstarich.moviematcher.models;

import de.caluga.morphium.annotations.Index;
import de.caluga.morphium.annotations.Reference;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

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
        return new Group(null, name, members);
    }

    public Group removeFriend(User friend) {
        ArrayList<User> members = new ArrayList<>(this.members);
        members.remove(friend);
        return new Group(null, name, members);
    }

    public Group renameGroup(String newName) {
        return new Group(null, newName, members);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof Group)) return false;
        return ((Group) o).name != null && ((Group) o).name.equals(name) && ((Group) o).members.size() == members.size();
    }
}
