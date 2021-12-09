package edu.ufl.cise.model;

/**
 * @author Aryan
 */
public class Peer {

    private int peerId;
    private String peerHost;
    private int peerPort;
    private boolean filePresent;

    public Peer(int peerId) {
        super();
        this.peerId = peerId;
        this.peerHost = "127.0.0.1";
        this.peerPort = 0;
        this.filePresent = false;
    }

    public Peer(int peerId, String peerHost, int peerPort, boolean filePresent) {
        this.peerId = peerId;
        this.peerHost = peerHost;
        this.peerPort = peerPort;
        this.filePresent = filePresent;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public String getPeerHost() {
        return peerHost;
    }

    public void setPeerHost(String peerHost) {
        this.peerHost = peerHost;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public void setPeerPort(int peerPort) {
        this.peerPort = peerPort;
    }

    public boolean isFilePresent() {
        return filePresent;
    }

    public void setFilePresent(boolean filePresent) {
        this.filePresent = filePresent;
    }
}
