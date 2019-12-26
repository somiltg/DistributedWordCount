package taskallocation;

import resource.IResourceManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.StringTokenizer;

/**
 * Handles task allocation requests from workers and calls {@link TaskManager} to allocate tasks to them. Keeps
 * listening for requests until all the tasks are completed as validated by TaskManager. Thereafter it requests
 * {@link IResourceManager} to stop all workers and returns the execution.
 * <p> Handles all the requests asynchronously. It can handle multiple requests at the same time and spawns
 * {@link TaskCommunicationServerThread} for each of the requests that handle the requests concurrently. See the
 * documentation of {@link TaskCommunicationServerThread} for details on how requests are handled. Uses TCP protocol
 * for reliable communication.
 *
 * @author somilgupta
 */
public class TaskAllocationServer {
    private static final int MAX_TASK_REQUEST_WAIT = 15000; //in milliseconds
    private static final int WORKER_WAIT_TIME_FOR_AVAILABILITY = 3000; //in milliseconds
    private final ITaskManager taskManager;
    private final IResourceManager resourceManager;

    public TaskAllocationServer(ITaskManager taskManager, IResourceManager resourceManager) {
        this.taskManager = taskManager;
        this.resourceManager = resourceManager;
    }

    public void run() {
        ServerSocket listeningSocket = null;
        try {
            listeningSocket = new ServerSocket(ITaskManager.TASK_ALLOCATION_SERVER_PORT);
            //Wait for a client connection
            listeningSocket.setSoTimeout(MAX_TASK_REQUEST_WAIT);
            listeningSocket.setReuseAddress(true);
            while (!taskManager.areAllTasksCompleted()) {
                try {
                    log("Listening for next task request to port " + listeningSocket.getLocalPort());
                    new TaskCommunicationServerThread(listeningSocket.accept()).start();
                } catch (SocketTimeoutException e) {
                    log("Timed out listening sockets for requests for port:" + listeningSocket.getLocalPort());
                }
            }
            log("Stopped listening as all tasks are completed. Stopping all workers now. Number of active workers=" +
                    resourceManager.getActiveWorkers().size());
            resourceManager.stopAllWorkers();
            log("Stopped all workers.");
        } catch (IOException e) {
            log("Error in running Task Allocation " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (listeningSocket != null) listeningSocket.close();
            } catch (IOException e) {
                log("Error in closing Task Allocation socket." + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void log(String message) {
        ITaskManager.log("[Server]" + message);
    }

    /**
     * Separate execution path for all incoming task allocation requests, as created by {@link TaskAllocationServer}.
     * For each request, worker is identified by workerId and task manager is called for availability of task. If
     * available, the task is allocated to the worker, else the worker is signalled to wait for given time. When the
     * worker responds back with the success, it marks the task completed with {@link ITaskManager} and requests for
     * new allocation, if any, for the worker. Please note that workers are not killed even though there are no
     * available tasks, until all tasks are completed. Calls {@link IResourceManager} to kill the worker.
     *
     * @author somilgupta
     */
    private class TaskCommunicationServerThread extends Thread {
        private final Socket taskCommSocket;

        TaskCommunicationServerThread(final Socket taskCommSocket) {
            this.taskCommSocket = taskCommSocket;
        }

        @Override
        public void run() {
            BufferedReader reader = null;
            DataOutputStream writer = null;
            int workerId = -1;
            try {
                log("Handling communication for request from port " + taskCommSocket.getPort());
                reader = new BufferedReader(new InputStreamReader(taskCommSocket.getInputStream()));
                String message = reader.readLine();
                StringTokenizer st = new StringTokenizer(message);
                String token = st.nextToken();
                switch (token) {
                    case "SUCCESS":
                        workerId = Integer.parseInt(st.nextToken());
                        log("Success message from worker " + workerId);
                        taskManager.markTaskCompleted(workerId);
                        break;
                    case "ALLOCATE":
                        workerId = Integer.parseInt(st.nextToken());
                        log("Task request message from worker " + workerId);
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected token in request message:" + token);
                }
                writer = new DataOutputStream(taskCommSocket.getOutputStream());
                String outString;
                if (taskManager.areAllTasksCompleted()) {
                    log("All tasks are completed. Stopping worker " + workerId + ".");
                    resourceManager.stopWorker(workerId);
                    //Stopping the resource before writing to prevent re-instantiation via heartbeat failure.
                } else {
                    if (taskManager.isAnyTaskAvailable()) {
                        try {
                            Task task = taskManager.allocateTask(workerId);
                            log("Task for input file " + task.getInputFile() + " given to " + workerId);
                            outString = "TASK " + task.getInputFile() + " " + task.getOutputFile() + "\n";
                        } catch (NoTaskAvailableException e) {
                            // This can happen as a result of race condition.
                            log("TaskManager showed task available but no task returned for " + workerId);
                            outString = "WAIT " + WORKER_WAIT_TIME_FOR_AVAILABILITY + "\n";
                        }
                        writer.write(outString.getBytes());
                    } else {
                        log("No task is available for worker " + workerId);
                        outString = "WAIT " + WORKER_WAIT_TIME_FOR_AVAILABILITY + "\n";
                        writer.write(outString.getBytes());
                    }
                }
            } catch (IOException e) {
                log("Error in reading from socket. Error message: " + e.getMessage() + ". WorkerId = " + workerId);
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (writer != null) writer.close();
                    taskCommSocket.close();
                } catch (IOException e) {
                    log("Error in reading from socket. Error message: " + e.getMessage() + ". WorkerId = " + workerId);
                    e.printStackTrace();
                }
            }
        }
    }
}
