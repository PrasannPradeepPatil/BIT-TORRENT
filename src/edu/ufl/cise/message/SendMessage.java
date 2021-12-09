package edu.ufl.cise.message;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class SendMessage {

    public static void pushMessage(byte[] message, ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void sendMessage(int type, ObjectOutputStream outputStream) {
        byte[] message = new byte[5];
        message[4] = Utility.convertToByteArray(type)[3];
        SendMessage.pushMessage(message, outputStream);
    }
}
