package com.johnstarich.moviematcher.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.johnstarich.moviematcher.app.HttpException;
import com.johnstarich.moviematcher.app.HttpStatus;
import com.johnstarich.moviematcher.store.MovieMatcherDatabase;
import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
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

    private static Gson gson = new GsonBuilder().create();

    public final ObjectId _id;
    public final String email;
    public final String first_name;
    public final String last_name;
    public final List<User> friends;
    public final List<?> groups; // ? should be replaced with Group class
    public final String password;

    public User(ObjectId _id) {
        this._id = _id;
        this.email = null;
        this.first_name = null;
        this.last_name = null;
        this.friends = new ArrayList<>(0);
        this.groups = new ArrayList<>(0);
        this.password = null;
    }

    private User(ObjectId _id, String email, String first_name, String last_name, List<User> friends, List groups, String password) {
        this._id = _id;
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.friends = friends;
        this.groups = groups;
        this.password = password;
    }

    private User(ObjectId _id, String email, String first_name, String last_name, String password) {
        this._id = _id;
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.friends = new ArrayList<>(0);
        this.groups = new ArrayList<>(0);
        this.password = password;
    }

    public User(ObjectId _id, String email, String first_name, String last_name) {
        this._id = _id;
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.friends = new ArrayList<>(0);
        this.groups = new ArrayList<>(0);
        this.password = null;
    }

    @Override
    public boolean equals(Object o) { return o == this || o instanceof User && ((User) o)._id == _id && ((User) o).email == email; }

    @Override
    public int hashCode() {
        return _id.hashCode();
    }

    @Override
    public String toString() { return _id + " " + email + " " + first_name + " " + last_name; }

    public User load() {
        Document user = getCollection().find(eq("_id", _id)).first();
        //if(user.isEmpty() == true) { throw new UserNotFoundException("This User " + this.toString() + " has not been registered."); }
        return gson.fromJson(user.toJson(), User.class);
    }

    private boolean isRegistered() {
        Document user = getCollection().find(eq("_id", _id)).first();
        if(user.isEmpty()) { return false; }
        return gson.fromJson(user.toJson(), User.class).equals(this);
    }

    /* not sure how to add friends or groups to mongoDB since they are lists*/
    /* will create the collection, once we insert our first user */
    private void insert() {
        getCollection().insertOne(
                new Document("_id", _id)
                        .append("email" , email)
                        .append("first_name" , first_name)
                        .append("last_name", last_name)
                        .append("password", password)
                        .append("friends", friends)
                        .append("groups", groups)
        );
    }

    public User register(String password) throws HttpException {
        if(this.isRegistered() == true) {
            throw new HttpException(HttpStatus.BAD_REQUEST);
        }

        User u = new User(_id, email, first_name, last_name, friends, groups, BCrypt.hashpw(password, BCrypt.gensalt()));
        u.insert();
        return u;
    }

    private User updateDBPassword() {
        getCollection().findOneAndUpdate(
                new Document("_id", _id),
                new Document("$set", new Document("password", password))
        );
        return this;
    }

    public User resetPassword(String oldPassword, String newPassword) throws HttpException {
        if(BCrypt.checkpw(oldPassword, password) == false) {
            throw new HttpException(HttpStatus.BAD_REQUEST);
        }

        return new User(_id, email, first_name, last_name, friends, groups, BCrypt.hashpw(newPassword, BCrypt.gensalt()))
                .updateDBPassword();
    }


    private User updateDBFriends() {
        getCollection().findOneAndUpdate(
                new Document("_id", _id),
                new Document("$set", new Document("friends", friends))
        );
        return this;
    }

    private User updateDBGroups() {
        getCollection().findOneAndUpdate(
                new Document("_id", _id),
                new Document("$set", new Document("groups", groups))
        );
        return this;
    }

    public static List<User> search(String query) {
        AggregateIterable<Document> iterable = getCollection().aggregate(
            asList(
                new Document("$match", new Document("$text", new Document("$search", query))),
                new Document(
                    "$project",
                    new Document("email", true)
                        .append("first_name", true)
                        .append("last_name", true)
                        .append("friends", true)
                        .append("groups", true)
                        .append("password", true)
                        .append("score", new Document("$meta", "textScore"))
                ),
                new Document("$sort", new Document("score",-1))
            )
        );

        ArrayList<User> queryResults = new ArrayList<User>();

        iterable
                .map(document -> gson.fromJson(document.toJson(), User.class))
                .forEach((Block<User>) u -> queryResults.add(u));

        return queryResults;
    }

    public static User loadByUsername(String email) {
        Document user = getCollection().find(eq("email", email)).first();
        return gson.fromJson(user.toJson(), User.class);
    }

    public void removeUser() {
        DeleteResult deleteResult = getCollection().deleteOne(
                new Document("_id", _id)
                    .append("email", email)
                    .append("first_name", first_name)
                    .append("last_name", last_name)
                    .append("friends", friends)
                    .append("groups", groups)
                    .append("password", password)
        );
        //deleteResult.getDeletedCount() == 1 ? yay it worked : wtf;
    }

    public User addFriend(User friend) {
        ArrayList<User> friends = new ArrayList<>(this.friends);
        friends.add(friend);
        return new User(_id, email, first_name, last_name, friends, groups, password).updateDBFriends();
    }

    public User addFriends(Collection<User> friends) {
        ArrayList<User> amigos = new ArrayList<>(this.friends);
        amigos.addAll(friends);
        return new User(_id, email, first_name, last_name, amigos, groups, password).updateDBFriends();
    }

    public User removeFriend(User oldFriend) {
        ArrayList<User> newFriends = new ArrayList<>(this.friends);
        newFriends.remove(oldFriend);
        return new User(_id, email, first_name, last_name, newFriends, groups, password).updateDBFriends();
    }

    public User removeFriends(Collection<User> oldFriends) {
        ArrayList<User> newFriends = new ArrayList<>(this.friends);
        newFriends.removeAll(oldFriends);
        return new User(_id, email, first_name, last_name, newFriends, groups, password).updateDBFriends();
    }

    /* need to build Group class in order to implement */
    public void addGroup() {}
    public void addGroups() {}
    public void removeGroup() {}
    public void removeGroups() {}

    // this.login() {}  need to implement this method also
}

/*
class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) { super(message); }
}
*/
