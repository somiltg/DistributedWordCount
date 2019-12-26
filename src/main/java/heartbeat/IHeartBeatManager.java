package heartbeat;

/**
 * Interface for component managing heartbeat. A heartbeat is a continuous ping-pong between the master and the worker.
 * Every worker is a heartbeat server that must respond to the request within a duration. The port of the worker uniquely
 * identifies the worker across all other components.
 *
 * @author somilgupta
 */
public interface IHeartBeatManager {
    /*
     * Turn this to true to see heartbeat logs.
     */
    boolean DEBUG = false;
    /**
     * Message sent in heartbeat request from Master.
     */
    String HEARTBEAT_REQ_MESSAGE = "Alive?";
    /**
     * Message returned in heartbeat response from Worker.
     */
    String HEARTBEAT_RES_MESSAGE = "Yes";
    /**
     * Max time to wait for the response from worker before issuing missing heartbeat.
     */
    int WORKER_RESPONSE_WAIT_TIME = 3000; // in milliseconds
    /**
     * Interval between 2 heartbeats.
     */
    int HEARTBEAT_INTERVAL = 3000; // in milliseconds

    /**
     * Handler function for missing heartbeat. The function must notify resource manager to create new resource and
     * task allocator to deallocate tasks for the failed node with the port number of that worker.
     *
     * @param port Port number of the worker that missed heartbeat.
     */
    void handleMissingHeartbeat(final int port);

    /**
     * Register new heartbeat a worker active at port given.
     *
     * @param port Port number of the new worker.
     */
    void addNewHeartBeat(final int port);

    /**
     * Explicitly kill the heartbeat for the given workerId. This is done when master wants to stop the worker preemptively.
     *
     * @param port Port number of the worker to be killed.
     */
    void stopHeartBeat(final int port);

    /**
     * Used by the implementation to identify the mode it is to work in and corresponding business logic (Runnable) to implement.
     * Note that there is only a single heartbeat manager implementation working for both worker and the master.
     */
    enum Mode {Server, Client}

    /**
     * Specific log message for heartbeat manager component.
     *
     * @param mode    Server/Client.
     * @param message Specific message.
     */
    static void log(final Mode mode, final String message) {
        if (DEBUG)
            System.out.println(String.format("[Heartbeat][Time: %d][Mode: %s] %s", System.nanoTime(), mode, message));
    }

}
