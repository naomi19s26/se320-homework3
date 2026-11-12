
# SE320 Homework 3: Object Dependencies
## Fall 2026

- Late Policy: standard for the course, see the syllabus

### Overview
In this assignment you'll put into practice some material on testing with dependencies.  There are two steps to this homework:

1. Refactor the code to expose the dependency on the collaborator
2. Use the [Mockito](http://site.mockito.org/) mocking framework to test the code's interaction
   with its dependency.


### Refactoring
The code you've been given depends on a network connection implementation, which knows the details of some protocol
to request files from a server.  The details of this protocol are not important, because you're going to
test *without access to the real implementation*.  What is relevant is that the protocol includes
explicit connect, request, and close operations, and an iterator-like pair of methods
```moreBytes()``` and ```read()```.  See the javadoc in ```ServerConnection.java```.

We'd like to test the functionality independent of the actual dependency, as we've discussed in class, using a mock of the ```ServerConnection```. 
This corresponds to situations such as:

- Your colleagues aren't finished implementing the server connection
- The ```ServerConnection``` interface is actually going to be used to abstract over multiple connection channels, but you don't yet know which and you want to write tests that check what your abstraction is supposed to do
- You need to write your tests so that they run quickly and independently of the actual physical network, so you need to use mocks to check without the physical dependency.

Pick an approach discussed in class (or an adaptation of the collaborator factory version to another
creational pattern, if you like).  Refactor the code appropriately.

In a comment at the top of your test file,
identify
which approach you took (especially if you used another creational pattern), and explain why you
picked it over other alternatives.

Once you've refactored, change the test ```testServerConnectionFailureGivesNull```: the test
already constructs a mock, so change the test to get the mock ```ServerConnection``` where it needs
to be.  The test should pass with your changes, but the only changes you should make to that method are to pass the mock in the appropriate place so that the core logic in the client is run with your mock.  Do not modify the construction of the mock (call to ```mock```), the call to ```when```, or the call to ```assertNull``` in that example test.

### Mocking
For this assignment, we'll be using [Mockito](http://site.mockito.org/), a popular Java mocking
framework.  As usual, all of the relevant dependencies are already pulled in by the Gradle build
file.

Mockito is a very substantial framework.  We'll only scratch the surface.

You'll probably want to peruse the [documentation for Mockito](https://javadoc.io/static/org.mockito/mockito-core/latest/org/mockito/Mockito.html).  For this assignment, you'll really only need sections 1-7, 10, and 12 (don't worry, they're short, and mostly examples).  If you use this (or a similar framework) in a real project, there are lots of bells and whistles for trickier tests or to make using many of the same mock easier; we won't use them for this homework.


We'd like you to write tests for the following 10 behaviors:

1. Test that if the attempt to ```connectTo(...)``` the server fails, the client code calls no
   further methods on the connection.
2. Test that if the connection succeeds but there is no valid file of that name, the client code calls no
   further methods on the connection except ```closeConnection```.  That is, the client code *is*
   expected to call ```closeConnection``` exactly once, but should not call other methods after it is known the
   file name is invalid.
3. Test that if the connection succeeds and the file is valid and non-empty, that the connection asks for at
   least some part of the file. (We want you to check that the client makes the request; examining the return value is insufficient for this.)
4. Test that if the connection succeeds and the file is valid but empty, the client returns an
   empty string.
5. Test that if the client successfully reads *part* of a file, and *then* an ```IOException```
   occurs before the file is fully read (i.e., ```moreBytes()``` has not returned false), the
   client still returns null to indicate an error, rather than returning a partial result.
6. Test that if the initial server connection succeeds, then if an ```IOException``` occurs while
   retrieving the file (requesting, or reading bytes, either one) the client still explicitly
   closes the server connection.
7. Test that if the initial connection succeeds and the file is valid and non-empty, the client calls moreBytes() before it calls read().
8. If the server returns the file in four pieces (i.e., four calls to ```read()``` must be executed), the client concatenates them in the correct order).
9. If ```read()``` ever returns ```null```, the client treats this as the empty string.
    + This stands in contrast to appending "null" to the file contents read thus far, which is the default if you simply append null. In Java, ```"asdf"+null``` evaluates to "asdfnull".
10. Test that if any of the connection operations fails the first time it is executed with an ```IOException```, the client
    returns null.


You should end up with 14 tests (one each for 1-9, plus 5 for item 10 --- since there are 5 methods
in ```ServerConnection```), each with a slightly different mock.  

Please name your test methods "test1" through "test9" and "test10a" through "test10e". This will make it much easier to grade your assignments,
and will be enforced by the autograder upon submission --- it will only run tests by those names, nothing else, and will fail if any
are missing! 

One or more of the tests should fail, because the client code has bugs!  You'll have to find out which --- if all of your tests pass, then one or more of your tests do not reflect the tests requested above.  You do not have to fix the client code (and in fact, please *do not fix the client code*), as this will simplify grading.

An example test from the Mockito documentation is in ```MockTests.java``` in the test directory.
You should add your tests there.  (You're welcome to delete the example test, but it's worth
running once to make sure everything is working for you.)

Document which test is which in your test file!  Do more than simply naming your tests cleverly ---
write explicit comments above each test indicating which of the above cases the test is for.  *You
earn points by writing this documentation, even if the actual test is incorrect.*

Note that figuring out the mapping between some of the English above and which methods they refer to is part of the assignment: test cases that are proposed based on various scenarios are generally not immediately given in terms of specific methods, so this is a skill you're supposed to build in this assignment! (Though this is not supposed to be the main challenge of the assignment).


### Grading

- 15% refactoring and explanation
    + 5% refactoring
    + 10% explanation
- 70% testing with mocks (and/or stubs and fakes)
    + 45% main teststests (both the property, and whether they pass/fail correctly)
        - 5 per test (9 tests)
    + 15% error tests a-e (5 tests, 3 each)
    + 10% documentation of which test is for which case, including but not limited to following our naming scheme
- 15% Check-in w/ Professor Gordon
    + Within one week of submitting, you must meet with Professor Gordon and talk through some parts of your solutions. If you can explain what you did and why you'll receive full credit here.


Submit via Gradescope.
