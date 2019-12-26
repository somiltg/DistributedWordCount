package heartbeat;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import static heartbeat.IHeartBeatManager.log;

/**
 * Implementation of {@link IHeartBeatManager}. Use Observable design pattern to notify the task manager and resource manager.
 *
 * @author somilgupta
 */
public class HeartbeatManager extends Observable implements IHeartBeatManager {

    private final Map<Integer, Thread> heartbeats;
    private final Mode mode;

    public HeartbeatManager(Mode mode) {
        this.heartbeats = new HashMap<>();
        this.mode = mode;
    }

    @Override
    public synchronized void handleMissingHeartbeat(final int port) {
        setChanged();
        notifyObservers(port);
        heartbeats.remove(port);
    }

    @Override
    public synchronized void addNewHeartBeat(int port) {
        if (heartbeats.containsKey(port)) {
            log(mode, "ALERT: Call to add heartbeat. Worker " + port + " is already alive.");
            return;
        }
        Thread heartBeat = generateHeartBeat(port);
        heartbeats.put(port, heartBeat);
        heartBeat.start();
        log(mode, "Started heartbeat for thread " + port);
    }

    @Override
    public synchronized void stopHeartBeat(int port) {
        if (!heartbeats.containsKey(port)) {
            log(mode, "ALERT: Call to stop heartbeat. Worker " + port + " is already dead.");
            return;
        }
        Thread heartbeatThread = heartbeats.get(port);
        heartbeatThread.interrupt();
        heartbeats.remove(port);
        log(mode, "Stopped heartbeat for thread " + port);
    }

    private Thread generateHeartBeat(int port) {
        Runnable runnable = null;
        switch (mode) {
            case Client:
                runnable = new HeartbeatClientRunnable(port, this);
                break;
            case Server:
                runnable = new HeartbeatServerRunnable(port);
        }
        return new Thread(runnable);
    }
}
