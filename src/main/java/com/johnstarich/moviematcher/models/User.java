package com.johnstarich.moviematcher.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import com.mongodb.client.MongoCollection;
import com.sun.org.apache.xpath.internal.operations.String;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Josue on 4/7/2016.
 */
public class User {
    private static class LazyUsersCollection {
        public static final MongoCollection<Document> usersCollection = MovieMatcherDatabase.getCollection("users");
    }
    private static MongoCollection<Document> getCollection() { return LazyUsersCollection.usersCollection; }

    /* need to decide how to store user preferences ... */

    public final ObjectId _id;
    public final String email;
    public final String first_name;
    public final String last_name;
    public final List<User> friends;
    public final List<?> groups; // ? should be replaced with Group class
    public final String password;   //not going to store this here ... need to figure out solution

    public User(ObjectId _id) {
        this._id = _id;
        this.email = null;
        this.first_name = null;
        this.last_name = null;
        this.friends = new ArrayList<>(0);
        this.groups = null;
        this.password = null;
    }

    public User(ObjectId _id, String email, String first_name, String last_name, List<User> friends, List groups, String password) {
        this._id = _id;
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.friends = friends;
        this.groups = groups;
        this.password = password;
    }

    public User(String email, String first_name, String last_name, String password) {
        this._id = new ObjectId();
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.friends = new ArrayList<>(0);
        this.groups = null;
        this.password = password;
    }

    public User load() {
        Document user = getCollection().find(eq("_id", _id)).first();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(user.toJson(), User.class);
    }

    private static User load(ObjectId _id) {
        Document user = getCollection().find(eq("_id", _id)).first();
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(user.toJson(), User.class);
    }

    /* will create the collection, once we insert our first user */
    /* not sure how to add friends or groups to mongoDB since they are lists*/
    /**
     * Add a new user to the "users" collection of our MovieMatcherDatabase.
     * (e.g. new User(email, first_name, last_name, password).addUser(); )
     * @return true if successful addition, false otherwise.
     * (e.g. false because a User already exist with the same email)
     */
    public boolean addUser() {
        if(load(_id).email == this.email) { return false; }
        getCollection().insertOne(
                new Document("_id", _id).append("email" , email).append("first_name" , first_name)
                        .append("last_name", last_name).append("password", password)
                        .append("friends", friends).append("groups", groups)
        );
        return true;
    }

    /**
     * Remove a specific user from the "users" collection of our MovieMatcherDatabase.
     * (e.g. Movie.getUser(ObjectId)
     */
    public void removeUser() {

    }

    public User addFriend(User friend) {
        ArrayList<User> friends = new ArrayList<>(this.friends);
        friends.add(friend);
        return new User(_id, email, first_name, last_name, friends, groups, password);
    }
    public void addFriends() {}
    public void removeFriend() {}
    public void removeFriends() {}
    public void updateFriends() {} //don't know if this would do something different or if it has a better name ...
    public void updateGroups() {} //don't know about this method
    public void addGroup() {}
    public void removeGroup() {}
    public void removeGroups() {}
    public boolean resetPassword() { return false; }

    public static List<User> search(String query) { return null; }
    public static boolean addUser(User user) { return false; }
   //public static User getUser(String email) { return null; }
}
