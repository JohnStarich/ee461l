package com.johnstarich.moviematcher.app;

import com.johnstarich.moviematcher.controllers.*;

/**
 * Movie Matcher API is defined here. These controllers make up the Movie Matcher services.
 * Created by johnstarich on 2/25/16.
 */
public class MovieMatcherApplication extends AbstractApplication {
	public void app() {
		mount(new HealthService());
		mount(new UserService());
		mount(new MovieService());
		mount(new FriendService());
		mount(new GroupService());
		mount(new HtmlService());
	}
}
