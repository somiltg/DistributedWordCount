 ## Distributed Word Count Application
 ### Authors
 1.   Somil Gupta | somilgupta@umass.edu
 2.  Sahil Sharma | sahilsharma@umass.edu
 
 ### Project structure and description
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
  
  #### kill-workers.sh
  **Usage:** sh kill-workers.sh [ << Worker id >> ]
 
 This kills the list of worker processes corresponding to the worker Ids provided to it. Please note that the workerId corresponds to the port at which heartbeat is up on the worker. 
  
  #### free-ports.sh
   **Usage:** sh free-ports.sh << Number of workers >> 
   
   This script frees all the ports starting from the WORKER_PREFIX (default 12000) that are to be used by the application for assigning to workers. It also frees the task allocation server port. This comes handy to prevent application crash due to occupied ports as a result of interrupts to previously processes that did not die graciously. 

### Reference material
* Please check `class-diagram.pdf` to get the class structure of the application. 

* You can also find the detailed javadoc in the `javadoc` package. Download the folder and open the index.html in browser to view the entire documentation. 

### Salient features

#### Modularisation and Decoupling
We have modularised the entire code in such a way that, the heartbeat, task manager and the resource allocation server/client represents our generic infrastructural libraries over which any application, in this case the Distributed Wordcount, can be run on. This type of modularisation separates the application-specific high level logic from the infrastructural low level logic. This also helped independent development and testing of components and their concurrent development. 
#### Concurrent and non-blocking execution
For heartbeat, we have kept different threads for each worker. Similarly, for task allocation, we handle task allocation requests in a multi-threaded manner. The requests made and the response from the workers about their tasks are handled in different communications, to prevent blocking multiple threads simultaneously for long running jobs. 
#### Serialization instead of File writer
We have used serialised files as our intermediate worker outputs since they are faster to read (binary files) than using a character based FileWriter parsers.
#### Efficient merging and sorting
For handling the word counts, we have used HashMaps instead of lists which provides O(1) read and write operations. Additionally, instead of sorting the output result twice (one for words and another for natural ordering of frequencies), we have performed both the sorting operation in the same comparator (in one go). 
#### Additional feature for releasing occupied ports before application run
Using applications with socket programming often have issues of Address port binding since the previous application may not have closed the ports of their servers, causing application failures and manual killing of processes. We have configured the code to release the required ports before the application commences so as to prevent these problems and therefore even if the previous application run did not close the ports graciously, our application run does not break.
#### Decoupled fault tolerance using observer-observable modelling
Instead of directly interacting between components, we have used asynchronous **observer-observable** design pattern for fault tolerance where hearbeat is observale and notifies resource manager and task manager about failures. This does not bind the components together, and decouples the responsibities of components from each other while also ensuring that every failure gets noticed. 
#### Resilient to unexpected failures
The application is resilient to failures like output folder not present, input file missing, etc. and handles them graciously. 
#### Clean logging and documentation
We have employed consistent debug notification across the application similar to Logger by registering the time, location and event in every print statement. Moreover, these logs can be enabled or disabled per component to allow section wise log analysis without overwhelming the console. To enable/disable logs from a component, say hearbeat, just go to its interface and toggle, **DEBUG** variable.
Besides, we have added proper documentation for every public function to make the code readable and consistent to industry standards. You can also view the Javadoc for the code by downloading the javadoc folder and opening index.html in your browser. 

### Testing
We did a bunch of testing to make sure the code matches the benchmarks set for this project. They can be summarised as follows:
#### Fault tolerance
Fault tolerance was tested using a script `kill-worker.sh` which is provided in the package. After the workers were instantiated and assigned tasks, we killed a few of the workers. After that, we closely followed the logs and the program behaved expectatedly where these workers were respawned and the tasks were reassigned, and the flow followed its normal course after that. We had to sleep some workers and make them slow for this testing since the task execution is very fast normally.
#### Resource
After providing N workers as input, querying number of workers active was checked ton find whether the processes were created. Moreover we also checked using ps -a and nmap to find the number of distinct processes created and their ids. 
#### Task Allocation
We tested all possible scenarios where we provided number of task/s(input files) equal to, less than and greater than the number of workers. The expected logic could be traced via logs as well.
#### Heartbeat
We tested the ping pong functionality by using nmap to check if the ports are up and general debugging via logs. Then we killed using kill-worker.sh to check if the loss of hearbeat was perceived by the master and it sent the notification to the server. 
#### Custom test cases with files
The current test cases just provided count wise (worker level) testing, we had to add additional cases to include the test logic for merging several files for which we modified the `FormatTest` function. After that, we needed reference output files for testing, which we generated from the instructor provided python program and added those additional files to `test/resources`. All these changes including additional tests and files have been pushed.


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



