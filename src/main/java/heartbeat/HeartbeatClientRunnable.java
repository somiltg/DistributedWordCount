package heartbeat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import static heartbeat.IHeartBeatManager.*;
import static heartbeat.IHeartBeatManager.Mode.Client;

/**
 * Heartbeat Client connection logic to be used for generating master-side thread per worker. Makes use of UDP for heartbeat.
 * Client is setup with IP localhost and calls server at the assigned server port=workerId. Breaks when server does not respond
 * and then calls {@link IHeartBeatManager} missing heartbeat.
 *
 * @author somilgupta
 */
class HeartbeatClientRunnable implements Runnable {

    private final int serverPort;
    private final IHeartBeatManager heartBeatManager;

    HeartbeatClientRunnable(final int serverPort, final IHeartBeatManager heartBeatManager) {
        this.serverPort = serverPort;
        this.heartBeatManager = heartBeatManager;
    }

    @Override
    public void run() {
        assert heartBeatManager != null;
        DatagramSocket clientSideSocket = null;

        try {
            clientSideSocket = new DatagramSocket();
            clientSideSocket.setReuseAddress(true);
            InetAddress serverAddr = InetAddress.getLocalHost();
            byte[] heartbeat = IHeartBeatManager.HEARTBEAT_REQ_MESSAGE.getBytes();
            DatagramPacket heartbeatPacket = new DatagramPacket(heartbeat, heartbeat.length, serverAddr, serverPort);
            clientSideSocket.setReceiveBufferSize(100);
            clientSideSocket.setSoTimeout(WORKER_RESPONSE_WAIT_TIME); // Timeout for informing about loss of worker.

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    break;
                }
                log(Client, "Calling Worker " + serverPort + " for heartbeat");
                clientSideSocket.send(heartbeatPacket);
                heartbeat = new byte[100];
                clientSideSocket.receive(new DatagramPacket(heartbeat, heartbeat.length));
                log(Client, "Worker " + serverPort + " responded to the heartbeat.");
            }

            log(Client, "Thread for master heartbeat for worker " + serverPort + " interrupted.");

        } catch (SocketTimeoutException e) {
            //Notify infrastructure manager of the failed node.
            log(Client, "Worker " + serverPort + " did not respond to heart beat.");
            heartBeatManager.handleMissingHeartbeat(serverPort);
        } catch (IOException e) {
            log(Client, e.getMessage());
            e.printStackTrace();
        } finally {
            assert clientSideSocket != null;
            clientSideSocket.close();
        }
    }
}
