 ## Distributed Word Count Application
 ### Authors
 1.   Somil Gupta
 2. Sahil Sharma
 
 ### About the project
 This project builds a distributed Word Count application. The application defines a single node, multi-process
 master-worker architecture to find the joint ordered word count frequency of a collection of files.
 
 Following components are generic and can be reused across different distributed applications that work in a
    master slave architecture.
 #### heartbeat 
 This package defines mechanism for ensuring node health check using system of ping pongs called
 heartbeat.
 #### resource
 This package creates and maintains the infrastructure by maintaining multiple process respresenting the
 various nodes in the single node, multi-process system. Does not manage task allocation.
  #### taskallocation
   This package handles the core task management and orchestration without holding the business
  specific execution logic e.g. wordcount here. The components also handles task associated communication between
  components and updates the task as per infrastructure or task status changes.
 #### util
  This package holds common business-agnostic utility functions for the application.
 
 -----------
   Following are application specific components to the Word Count application: 

 #### wordcount 
 This package contains the word count execution logic and worker application that needs to be run over
  the architecture.
 
 Master defines the generic interface for Master while WordCount is its word-count application
  specific implementation.

Please check `class-diagram.pdf` to get the class diagram of the application. 

You can also find the detailed javadoc in the `javadoc` package. 




---------

## Instructor default

Compile and test
```
./gradlew build
```

Compile only
```
./gradlew assemble
```

Test
```
./gradlew test
```

For those who use Windows, you should run `gradlew.bat` with the same parameters.

Both IDEA and Eclipse have plugins for Gradle.

Some existing tests need Java 8.


### Code location

`src/main/java` is for word count code.

`src/test/java` is for tests. And `src/test/resources` is for test data.

In most cases, adding or modifying files in other places is not necessary.


### Directions

Interface `Master` and class `WordCount` are already here.
`WordCount` should implement `Master` and have a constructor taking an integer (number of workers)
and array of strings (file paths) as input. There is documentation for every method of `Master`.
You should read them before implementing.

There are two basic tests here.
These tests should be passed before submission (class `TestUtil` can be modified
if there is error due to platform-dependent methods).
You may also want to add more tests, like larger files, unicode content, fault tolerance, etc.

You shouldn't use `Process.isAlive` to detect whether a worker is dead.



