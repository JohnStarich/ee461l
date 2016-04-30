# Homework 3: Testing in our project

This paper talks about our testing plan and what tests we’ve already implemented. We will explain unit tests, integration tests, and our pipeline build process. 

## Unit and Integration Testing

### Groups and Lists Unit Tests
Groups are Lists of Users who want to meet up and check out movies together. Groups, then, involve adding and removing friends as well as renaming the group. Within our Group unit tests, we make sure that when a User wants to modify one of their groups their changes are saved. We test adding friends, removing friends, and renaming a group. Also, for completeness, we make sure that the `Group.equals(Object)` method also has a test method. 

### Integration Tests
We built an AbstractMongoDBTest class that extends the TestCase class to handle the setup and teardown of certain tests. The sole purpose of AbstractMongoDBTest is to setup a MonogoDB instance in order for other tests to use the embedded MongoDB instance. This way we can run the tests that require a MongoDB instance from anywhere. We do not have to create a local host connection on our computers while running tests. Additionally, if the tests are being run on the server, this provides us with the required MongoDB instance.

So the following test are all integration tests since for each test method they are set up with a MongoDB instance. Each test uses the MongoDB instance to test these aspects:

* Abstract Model Test
* Movie Test
* Rating Test
* Session Test
* User Test

The Abstract Model Test made sure that we were able to save, load, delete, and perform other operations on the MongoDB instance that was created by the AbstractMongoDBTest. The Session Test ensures that sessions expire after a minute. Usually sessions will expire after two hours but for testing purposes we made sure they were automatically deleted after a minute. The Rating tests ensures that we can look up certain ratings on movies that were made by users. The User test make sures that the functionality of registering and logging in a user works properly. The Movie Test allows us to verify that Java Date objects are compatible with MongoDB Date objects. 

### UI
User interface testing has been done primarily using Ember serve and compile commands. Ember does live reloading whenever any changes are made and saved to any of the files in the project. This has made working with the UI easier to know when changes were breaking the build of the entire front end. All debugging of UI to API calls has been done through the use of an internet browser's console and debugger. Style changes are done using the 'Inspect Element' style windows.


## System Testing

### System testing for both UI and API repo

We perform system testing by utilizing out automated build pipeline in Jenkins:

* Used to create a checkpoint for the merging of all our work.
* Works with Github so any time we want to merge code to master a build is triggered.
* We each had to make pull requests and ensure our code didn’t break the app all together.
* Then the team reviews the request, comments on style and techniques, and ensures the checks pass. 
* After review the code is merged to master.
* The new version of our app deploys once another set of checks are run and passed.

This kind of system testing has been crucial to our development process because it has allowed us to develop on our own and worry less about integrating our work into the master branch. This was established early on in our development process for a couple reasons: first, it creates a modular development environment; second, it keeps everyone up to date on status of project via Slack; three, it ensures a build finishes and works before merging to our master branch and deploying the web app. 
