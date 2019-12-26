package heartbeat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static heartbeat.IHeartBeatManager.Mode.Server;
import static heartbeat.IHeartBeatManager.log;

/**
 * Heartbeat Server connection logic to be used for generating worker-side thread. Makes use of UDP for heartbeat.
 * Server is setup with IP localhost and port corresponding to unique worker Id.
 *
 * @author somilgupta
 */
class HeartbeatServerRunnable implements Runnable {
    private final int serverPort;

    HeartbeatServerRunnable(final int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        DatagramSocket serverSideSocket = null;
        try {
            serverSideSocket = new DatagramSocket(serverPort);
            serverSideSocket.setReuseAddress(true);
            while (!Thread.currentThread().isInterrupted()) {
                byte[] data = new byte[100];
                DatagramPacket heartbeat = new DatagramPacket(data, data.length);
                serverSideSocket.receive(heartbeat);
                log(Server, "Worker " + serverPort + " received heartbeat . Message = " + new String(heartbeat.getData()).trim());
                data = IHeartBeatManager.HEARTBEAT_RES_MESSAGE.getBytes();
                serverSideSocket.send(new DatagramPacket(data, data.length, heartbeat.getAddress(), heartbeat.getPort()));
            }
            log(Server, "Thread for worker heartbeat " + serverPort + " interrupted.");
        } catch (Exception e) {
            log(Server, e.getMessage());
            e.printStackTrace();
        } finally {
            assert serverSideSocket != null;
            serverSideSocket.close();
        }
    }
}
