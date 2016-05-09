# Movie Matcher Project Final Report

## Members

* Josue Alfaro - jja2244
* Jeremy Castillo - jcc4428
* Cesar Gonzalez - cg34887
* John Starich - js68634

## Site

Login
<http://tomcat.johnstarich.com/login>

Register
<http://tomcat.johnstarich.com/register>

## API Repo
<https://github.com/JohnStarich/ee461l>

## UI Repo
<https://github.com/JohnStarich/ee461l-ui>

## Motivation

There is not an easy way to find movies that groups of people, with their various preferences, would want to watch together. Our goal is to bridge that gap and recommend movies to our audience based on all of their movie preferences.

## User Benefits

Users can register on the site and browse the movie database. Users would spend a lot less time trying to brainstorm different movies to watch and they would spend more time watching movies. Our web application is very easy to navigate, add friends, create groups, and it is not distracting. With a click of a button, a user’s group will be provided with a list of movies to watch and enjoy together.

## Feature Description and Requirements

1. Users can register, login and rate movies they've seen.
2. Users can link up with their friends and generate a "Top 20" list of movies that they will most likely enjoy together. 
3. Users can change their name and password. 
4. Users are able to add and delete friends from their friends list, and from their friends list create groups.

## Design

![Design](screenshots/design.png)

![Updated Class Diagram](screenshots/classDiagram1.png)

![Updated Class Diagram](screenshots/classDiagram2.png)

## User Interface

![Login](screenshots/login.png)

![Register](screenshots/register.png)

![Movie Search w/ Rating](screenshots/movie_search_wrating.png)

![Friends List](screenshots/friendslist.png)

![Find Friends](screenshots/findfriends.png)

![Add Friends](screenshots/addfriends.png)

![Make Group](screenshots/makegroup.png)

![Add Group](screenshots/addedgroup.png)

![Add friends to group](screenshots/addfriendstogroup.png)

![Group has members](screenshots/grouphasfriends.png)

![Group Recommendations](screenshots/grouprecommendations.png)

![Settings](screenshots/settings.png)

![Change Name](screenshots/changename.png)

## Testing Tools, Test Scenarios, and Test Cases

Our application was divided into two different parts for testing, the first is the API (back end) and the second is the UI (front end). The team’s plan was to make sure that with each pull request on Github our systems would be tested. 
For each class within the API a JUnit test was created, this way we could test the functionality of each class separately. The Rating class was tested to make sure that a user could easily rate any movie. The loading, saving, and modifying aspects were also tested. The User class was tested so that each User could add friends, delete friends, create groups, delete groups, and login. The Session class was mainly tested to make sure that a session would be deleted from our database after x amount of time. The Group class was tested to ensure that adding friends into groups was fully functional as well as changing group names. The Movie class was tested to see that the equals method worked well. This completes the unit testing. 

All of our data is stored in MongoDB, so we decided to test the functionality of the database with our data. In order to do this without affecting our production database, we needed an instance of MongoDB run these tests after someone pushes to a pull request. Our solution was to add an embedded MongoDB instance that starts up, runs our tests, and throws out the data for every test. We saved Sessions, Movies, Ratings, Users, Groups, respectively in their own collections and we performed the following tests. We made sure to test the loading, saving, updating, and dropping of these data sets. The way we implemented this was by creating an AbstractModel that would handle those methods no matter what kind of data was being manipulated. 

The team decided it would be best if we did not merge buggy code onto our master branch. So a build pipeline was created for automatic testing and deployment once a team member made a pull request. Here is where we implemented regression and system testing. The code would be compiled and then all of our previous tests would be executed. This ensures that our previous code and implementations do not break with the new changes.

In terms of the front end, there was an ember automatic build that helped out when we would create and modify the front end. It was fairly quick because as soon as one saved a javascript or handlebars file we would know if that code broke or not. The team was not able to find an option where the UI tests could be done automatically. So with the help of browser debugging tools, the UI was tested by just playing with the features. 

Once we had most of the front and back end, a friend walked through our web application and reported what their opinion on the functionality of the website. We took their comments into consideration and made changes.

