package com.johnstarich.moviematcher.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Josue on 4/16/2016.
 */
public class Group {
    public final String name;
    public final List<User> members;

    public Group() {
        this.name = null;
        this.members = new ArrayList<>(0);
    }

    public Group(String name) {
        this.name = name;
        this.members = new ArrayList<>(0);
    }

    public Group(String name, List<User> members) {
        this.name = name;
        this.members = members;
    }

    public Group addFriend(User friend) {
        ArrayList<User> members = new ArrayList<>(this.members);
        members.add(friend);
        return new Group(name, members);
    }

    public Group removeFriend(User friend) {
        ArrayList<User> members = new ArrayList<>(this.members);
        members.remove(friend);
        return new Group(name, members);
    }
}
