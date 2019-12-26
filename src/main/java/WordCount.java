import heartbeat.HeartbeatManager;
import heartbeat.IHeartBeatManager;
import resource.IResourceManager;
import resource.ResourceManager;
import taskallocation.ITaskManager;
import taskallocation.TaskAllocationServer;
import taskallocation.TaskManager;
import wordcount.MergerSorter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static heartbeat.IHeartBeatManager.Mode.Client;


public class WordCount implements Master {
    private final static boolean DEBUG = true;
    private static final String OUTPUT_LOCATION = "wcOutput/";
    private static final String OUTPUT_FILE_EXTENSION = ".ser";
    private static final String WORKER_APPLICATION_CLASSPATH = "build/classes/main/";
    private static final String WORKER_APPLICATION_NAME = "wordcount.Worker";
    private final int numberOfWorkers;
    private final String[] inputFiles;
    private OutputStream resultStream;
    private IResourceManager resourceManager;
    private TaskAllocationServer taskAllocationServer;

    public WordCount(final int workerNum, final String[] filenames)
            throws IOException {
        numberOfWorkers = workerNum;
        inputFiles = filenames;
        File outputLocation = new File(OUTPUT_LOCATION);

        // if the directory does not exist, create it
        if (!outputLocation.exists()) {
            if (outputLocation.mkdir()) {
                log("Output directory created at " + outputLocation.getAbsolutePath());
            } else {
                throw new IOException("Issue with creating output directory.");
            }
        }
        //Default output to console
        resultStream = new PrintStream(System.out);
        staleResourceCleanup();
        setupMasterInfrastructure();
    }

    @Override
    public void setOutputStream(PrintStream out) {
        resultStream = out;
    }

    @Override
    public void run() {
        for (int i = 0; i < numberOfWorkers; i++) {
            this.createWorker();
        }
        taskAllocationServer.run();
        MergerSorter mergerSorter = new MergerSorter();
        mergerSorter.execute(OUTPUT_LOCATION, inputFiles, OUTPUT_FILE_EXTENSION, resultStream);
    }

    @Override
    public Collection<Process> getActiveProcess() {
        Map<Integer, Process> workers = resourceManager.getActiveWorkers();
        log("Number of active workers at the moment: " + workers.size() + " which are: " + workers.keySet());
        return workers.values();
    }

    @Override
    public void createWorker() {
        resourceManager.createWorker();
    }

    private void setupMasterInfrastructure() {
        IHeartBeatManager heartBeatManager = new HeartbeatManager(Client);
        resourceManager = new ResourceManager(numberOfWorkers, WORKER_APPLICATION_CLASSPATH, WORKER_APPLICATION_NAME,
                heartBeatManager);
        ITaskManager taskManager = new TaskManager(inputFiles, OUTPUT_LOCATION, OUTPUT_FILE_EXTENSION);
        ((Observable) heartBeatManager).addObserver(taskManager);
        ((Observable) heartBeatManager).addObserver(resourceManager);
        taskAllocationServer = new TaskAllocationServer(taskManager, resourceManager);
    }

    private void staleResourceCleanup() {
        try {
            log("Reserving ports for the required worker size " + numberOfWorkers + " by deleting previous resources.");
            Process st = new ProcessBuilder("sh", "free-ports.sh", numberOfWorkers + "").inheritIO().start();
            st.waitFor();
        } catch (Exception e) {
            log("Error in cleaning up ports for new workers. ");
            e.printStackTrace();
        }
    }

    private static void log(final String message) {
        if (DEBUG)
            System.out.println(String.format("[Master][Time: %d] %s", System.nanoTime(), message));
    }

    public static void main(String[] args) throws Exception {
        //Test files here.
        String[] files = new String[] {"/Users/somilgupta/Desktop/CS_532/workspace/project-2-group-13/build/resources" +
                "/test/random.txt", "/Users/somilgupta/Desktop/CS_532/workspace/project-2-group-13/build/resources" +
                "/test/simple.txt"};

        new WordCount(2, files).run();
    }
}

