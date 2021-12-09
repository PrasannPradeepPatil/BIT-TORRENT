package edu.ufl.cise.process;

import edu.ufl.cise.message.Handshake;
import edu.ufl.cise.message.MessageHandler;
import edu.ufl.cise.logger.Logger;
import edu.ufl.cise.model.Peer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RunPeer extends Thread {
    public Socket connection;
    public ObjectInputStream inputStream;
    public ObjectOutputStream outputStream;
    public Peer peer = null;
    public int peerId;
    public MessageHandler messageHandler;
    public boolean handshakeComplete = false;
    public static final String handshakeHeader = "P2PFILESHARINGPROJ";

    RunPeer(Socket connection, int peerId) {
        this.connection = connection;
        this.peerId = peerId;
        this.messageHandler=new MessageHandler(peerId);
    }

    RunPeer(Socket connection, int peerId, Peer peer) {
        this.connection = connection;
        this.peerId = peerId;
        this.peer = peer;
        this.messageHandler=new MessageHandler(peerId);
    }

    public void run() {
        try {
            if (peer == null) {
                inputStream = new ObjectInputStream(connection.getInputStream());
                outputStream = new ObjectOutputStream(connection.getOutputStream());
                outputStream.flush();
            } else {
                outputStream = new ObjectOutputStream(connection.getOutputStream());
                outputStream.flush();
                inputStream = new ObjectInputStream(connection.getInputStream());
            }
            // 2 (a)
            Handshake.sendHandshake(peerId, outputStream);
            byte[] handshakeMsg = (byte[]) inputStream.readObject();
            int peerId = Handshake.getHandshakePeerId(handshakeMsg);
            Logger.connectionMade(peerId);
            if (Handshake.getHandshakeHeader(handshakeMsg).equals(handshakeHeader) && (peer == null || peer.getPeerId() == peerId)) {
                handshakeComplete = true;
                // 2 (b)
                messageHandler.sendBitField(outputStream);
                messageHandler.createRunPeerMapping(peerId, this);
            }
            if (handshakeComplete)
                messageHandler.handleMessages(inputStream, outputStream, peerId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }
}
