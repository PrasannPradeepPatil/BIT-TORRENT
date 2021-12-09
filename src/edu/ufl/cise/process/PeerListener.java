package edu.ufl.cise.process;

import edu.ufl.cise.logger.Logger;
import edu.ufl.cise.model.Peer;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class PeerListener extends Thread {
    private List<Peer> peerInfo;
    private int peerId;

    public PeerListener(int peerId, List<Peer> peerInfo) {
        this.peerInfo = peerInfo;
        this.peerId = peerId;
    }

    public void run() {
        int clientCount = 0;
        try {
            for (Peer peer: peerInfo) {
                if (peer.getPeerId() >= peerId) {
                    break;
                }
                // 1 (b)
                Socket connection = new Socket(peer.getPeerHost(), peer.getPeerPort());
                new RunPeer(connection, peerId, peer).start();
                Logger.attemptConnection(peer.getPeerId());
                clientCount += 1;
            }
            clientCount = peerInfo.size() - clientCount - 1;
            int currentPeerPort = 0;
            for (Peer peer : peerInfo) {
                if (peer.getPeerId() == peerId) {
                    currentPeerPort = peer.getPeerPort();
                    break;
                }
            }
            // 1 (b)
            ServerSocket listener = new ServerSocket(currentPeerPort);
            while (true) {
                if (clientCount == 0)
                    break;
                new RunPeer(listener.accept(), peerId).start();
                clientCount -= 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
