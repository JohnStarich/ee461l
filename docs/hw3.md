# Homework 3: Testing in our project

This paper talks about our testing plan and what tests we’ve already implemented. We will explain unit tests, integration tests, and our pipeline build process. 

## Unit and Integration Testing

### Groups and Lists
Groups are Lists of Users who generally want to meet up and check out movies together, so the functionality with Groups are adding and removing friends from groups. Within our Unit GroupTest we make sure that when a User wants to modify one of their groups the changes are saved.  So basically we test adding friends, removing friends, and renaming a group. Also, for completeness we make sure that the Group.equals(Object) method also has a test method. 

### Abstract MongoDB
We built an AbstractMongoDBTest class that extends the TestCase class to handle the setup and teardown of certain tests. The sole purpose of  AbstractMongoDBTest  is a to setup a monogoDB instance in order for other tests to use the said mongoDB instance. This way we can run the tests that require a mongo instance from anywhere. We don’t have to create a local host connection on our computers when running tests. And if the tests are being run on the server, then this provides us with the required mongo instance. 

So the following test are all integration tests since for each test method they are set up with a mongoDB instance. Each test uses the mongo instance to test different certain aspects. 
* Abstract Model Test
* Movie Test
* Rating Test
* Session Test
* User Test

The Abstract Model Test made sure that we were able to save, load, delete, and perform other operations on the mongodb instance that was created by the AbstractMongoDBTest. The Session Test ensures that sessions expire after a minute. Usually sessions will expire after two hours but for testing purposes we made sure they were automatically deleted after a minute. The Rating tests ensures that we can look up certain ratings on movies that were made by users. The User test make sures that the functionality of a user works properly. The Movie Test allows us to verify that java Date objects are compatible with mongodb Date objects. 

### UI 
User interface testing has been done primarily using Ember serve and compile commands. Ember does live reloading whenever any changes are made and saved to any of the files in the project. This has made working with the UI easier to know when changes were breaking the build of the entire front end. All debugging of UI to API calls has been done through the use of an internet browser's console and debugger. Style changes are done using the 'Inspect Element' style windows.


## System Testing

System testing for both UI and API repo
Plan : Build pipeline using Jenkins 

* Used to create a checkpoint for the merging of all our work.
* Works with Github so any time we want to merge code to master a build is  triggered.
* We each had to make pull requests and ensure our code didn’t break the app all together.
* Then the team reviews the request, comments on style and techniques, and ensures the checks pass. 
* After review the code is merged to master.
* The new version of our app deploys once another set of checks are run and passed.

This form of system testing has been crucial to our development process because it has allowed for all of us to develop on our own and worry less about integrating our work into the main branch. This was established early on in our development process for several reasons: one, it creates a modular development environment. Two, it keeps everyone up to date on status of project. We all receive Slack and email notification when pull requests are made, commented on and merged. Three, it ensures a build finishes and works before merging to our main branch and deploying the web app. 


