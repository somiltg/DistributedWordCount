package wordcount;

import heartbeat.HeartbeatManager;
import heartbeat.IHeartBeatManager;
import taskallocation.ITaskExecutor;
import taskallocation.TaskAllocationClient;

import static heartbeat.IHeartBeatManager.Mode.Server;

class Worker {
    private final static boolean DEBUG = true;
    private final int id;
    private final IHeartBeatManager heartBeatManager;
    private final TaskAllocationClient taskAllocationClient;


    private Worker(final int workerId) {
        id = workerId;
        log("wordcount.Worker process created.");
        heartBeatManager = new HeartbeatManager(Server);
        ITaskExecutor executor = new WordCounter();
        taskAllocationClient = new TaskAllocationClient(workerId, executor);
    }

    private void run() {
        heartBeatManager.addNewHeartBeat(id);
        taskAllocationClient.run();

    }

    private void log(final String message) {
        if (DEBUG)
            System.out.println(String.format("[wordcount.Worker %d][Time: %d] %s", id,
                    System.nanoTime(), message));
    }

    public static void main(String[] args) {
        new Worker(Integer.parseInt(args[0])).run();
    }
}
