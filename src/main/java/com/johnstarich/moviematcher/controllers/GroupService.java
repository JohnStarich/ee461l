package com.johnstarich.moviematcher.controllers;

import com.johnstarich.moviematcher.utils.HttpException;
import com.johnstarich.moviematcher.utils.HttpStatus;
import com.johnstarich.moviematcher.models.Group;
import com.johnstarich.moviematcher.models.User;
import com.johnstarich.moviematcher.routes.AuthenticatedRoute;
import org.bson.types.ObjectId;
import spark.Route;

import java.util.*;

/**
 * Register group services, like group search and group ID lookup
 * Created by johnstarich on 5/22/16.
 */
public class GroupService extends JsonService {
	@Override
	public String resource() {
		return "groups";
	}

	@Override
	public void initService() {
		AuthenticatedRoute addGroup = (request, response, user) -> {
			Optional<String> group_name = bodyParam(request, "group_name");
			if(! group_name.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, "No group name provided");
			String groupName = group_name.get();
			if(user.groups == null) {
				Group g = new Group(null, groupName).save();
				user.addGroup(g).save();
				return g.id;
			}
			for(Group existingGroup : user.groups) {
				// does group already exist
				if(existingGroup.name.equals(groupName)) {
					throw new HttpException(HttpStatus.BAD_REQUEST, groupName + " already exists");
				}
			}
			// create new group, save group, add to user, and save changes to user
			Group g = new Group(null, groupName).save();
			user.addGroup(g).save();
			return g.id;
		};
		jpost(addGroup);
		jpost("/", addGroup);

		jget("search/:search_query", (request, response) -> {
			String searchQuery = request.params("search_query").replaceAll("\\+", " ");
			System.out.println("Searched for \""+searchQuery+"\"");
			throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
		});

		Route idRoute = (request, response) -> {
			String group_id = request.params("id");
			System.out.println("Looked up group with ID: "+group_id);
			Optional<Group> result = new Group(new ObjectId(group_id)).load();
			if(! result.isPresent()) throw new HttpException(HttpStatus.NOT_FOUND, "Group not found with ID: "+group_id);

			return result.get().noPasswords();
		};
		jget(":id", idRoute);

		AuthenticatedRoute userGroups = (request, response, user) -> {
			if(request==null) { return Collections.EMPTY_LIST; }

			Map<String, List<User>> groupsMap = new HashMap<>();
			Map<String, Object> ret = new HashMap<>();

			if(user.groups == null) {
				groupsMap.put("", new ArrayList<>(0));
				ret.put("groups", new ArrayList<>(0));
			} else {
				for (Group g : user.groups) {
					groupsMap.put(g.name, g.members);
				}
				ret.put("groups", user.groups);
			}
			ret.put("members", groupsMap);
			return ret;
		};
		jget(userGroups);
		jget("/", userGroups);

		AuthenticatedRoute userSubGroup = (request, response, user) -> {
			String group_name = request.params("group_name");
			return user.getFriendsToAdd(group_name);
		};
		jget(":group_name/user", userSubGroup);
		jget(":group_name/user/", userSubGroup);

		AuthenticatedRoute addUserToGroup = (request, response, user) -> {
			// friend to add to group
			Optional<String> username = bodyParam(request, "username");
			Optional<User> friend = User.loadByUsername(username.get());
			if(! friend.isPresent()) throw new HttpException(HttpStatus.BAD_REQUEST, username.get() + " does not exist");

			// don't add self to group
			if(user.username.equals(friend.get().username)) throw new HttpException(HttpStatus.BAD_REQUEST, "Cannot add self to group");

			// group to add to
			String groupName = request.params("group_name");
			if(groupName == null) throw new HttpException(HttpStatus.BAD_REQUEST, "No group name provided");

			// attempt to add to group
			user = user.addFriendToGroup(groupName, friend.get()).save();

			return user.groups.parallelStream().filter(group -> group.name.equals(groupName)).findFirst().get().id;
		};

		jpost(":group_name/user", addUserToGroup);
		jpost(":group_name/user/", addUserToGroup);

		jdelete(":group_id", (request, response, user) -> {
			String groupId = request.params("group_id");
			Optional<Group> groupToRemoveOpt = new Group(new ObjectId(groupId)).load();
			if(! groupToRemoveOpt.isPresent()) throw new HttpException(HttpStatus.NOT_FOUND, "Group not found with ID: "+groupId);
			user.removeGroup(groupToRemoveOpt.get()).save();
			return "Success! Group successfully deleted.";
		});

		jdelete(":group_id/:member_id", (request, response, user) -> {
			String groupId = request.params("group_id");
			String memberId = request.params("member_id");
			Optional<Group> groupToRemoveMemberOpt = new Group(new ObjectId(groupId)).load();
			if(! groupToRemoveMemberOpt.isPresent()) throw new HttpException(HttpStatus.NOT_FOUND, "Group not found with ID: "+groupId);
			groupToRemoveMemberOpt.get().removeFriendWithId(new ObjectId(memberId)).save();
			return "Success! Group member successfully removed.";
		});

		AuthenticatedRoute recommendationList = (request, response, user) -> {
			// group to generate list for
			String groupName = request.params("group_name");
			if(groupName == null) throw new HttpException(HttpStatus.BAD_REQUEST, "No group name provided");
			Optional<Group> g = user.findGroup(groupName);
			if(! g.isPresent()) { throw new HttpException(HttpStatus.BAD_REQUEST, "Could not find "+groupName); }
			return g.get().suggestMovies(user);
		};

		jget(":group_name/recommendations", recommendationList);
		jget(":group_name/recommendations/", recommendationList);
	}
}
