package taskallocation;

import resource.IResourceManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;

import static taskallocation.ITaskManager.TASK_ALLOCATION_SERVER_PORT;

/**
 * Worker client to request task allocation from master. Queries the task allocation server for tasks with its
 * workerId as identifier. In case it gets a task, it calls {@link ITaskExecutor} implementation to execute the task
 * and returns success message to the server once completed. If the server signals wait, it waits for the designated
 * period. Note that once the tasks are completed, master will anyway kill all the workers preemptively, so the
 * client remains in an infinite loop until explicitly interrupted. Worker application should call this client once
 * created, to query for tasks.
 *
 * @author somilgupta
 */
public class TaskAllocationClient {
    private final ITaskExecutor taskExecutor;
    private final int workerId;

    public TaskAllocationClient(final int workerId, final ITaskExecutor taskExecutor) {
        this.workerId = workerId;
        this.taskExecutor = taskExecutor;
    }

    public void run() {
        Socket sc = null;
        BufferedReader bf = null;
        DataOutputStream dout = null;
        //Request for task in first call.
        String message = "ALLOCATE " + workerId + "\n";
        while (true) {
            try {
                log("Querying port " + TASK_ALLOCATION_SERVER_PORT + " for some task. ");
                sc = new Socket(InetAddress.getLocalHost(), TASK_ALLOCATION_SERVER_PORT);
                dout = new DataOutputStream(sc.getOutputStream());
                dout.write(message.getBytes());
                bf = new BufferedReader(new InputStreamReader(sc.getInputStream()));
                String serverMessage = bf.readLine();
                StringTokenizer st = new StringTokenizer(serverMessage);
                String token = st.nextToken();
                switch (token) {
                    case "TASK":
                        Task task = new Task(st.nextToken(), st.nextToken());
                        log("Received task from server with input file " + task.getInputFile() + " and output file " + task.getOutputFile());
                        taskExecutor.execute(task);
                        message = "SUCCESS " + workerId + "\n";
                        break;
                    case "WAIT":
                        int waitTime = Integer.parseInt(st.nextToken());
                        log("Server has asked to wait for " + waitTime + " milliseconds.");
                        Thread.sleep(waitTime);
                        message = "ALLOCATE " + workerId + "\n";
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected token in request message:" + token);
                }

            } catch (Exception e) {
                log("Exception in receiving the task. " + e.getMessage());
                e.printStackTrace();
                break;
            } finally {
                try {
                    if (bf != null) bf.close();
                    if (sc != null) sc.close();
                    if (dout != null) dout.close();
                } catch (IOException e) {
                    log("Issue in closing the socket or stream. " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void log(String message) {
        ITaskManager.log(String.format("[Client %d ] %s", workerId, message));
    }
}
