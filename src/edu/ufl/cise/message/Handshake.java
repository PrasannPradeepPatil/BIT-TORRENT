package edu.ufl.cise.message;

import java.io.ObjectOutputStream;

public class Handshake {

    public static final String handshakeHeader = "P2PFILESHARINGPROJ";
    public static void sendHandshake(int peerId, ObjectOutputStream out) {
        byte[] message = new byte[32];
        byte[] messageHeader = handshakeHeader.getBytes();
        byte[] peerIdBytes = Utility.convertToByteArray(peerId);
        for (int i = 0; i < 32; i++) {
            if (i < 18) {
                message[i] = messageHeader[i];
            } else if (i > 27) {
                message[i] = peerIdBytes[i - 28];
            } else {
                message[i] = 0;
            }
        }
        SendMessage.pushMessage(message, out);
    }

    public static String getHandshakeHeader(byte[] handshake) {
        byte[] messageHeader = new byte[18];
        System.arraycopy(handshake, 0, messageHeader, 0, 18);
        return new String(messageHeader);
    }

    public static int getHandshakePeerId(byte[] handshake) {
        byte[] peerId = new byte[4];
        System.arraycopy(handshake, 28, peerId, 0, 4);
        return Utility.convertToInt(peerId);
    }
}
