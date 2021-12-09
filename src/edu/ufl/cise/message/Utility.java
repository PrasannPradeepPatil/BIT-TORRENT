package edu.ufl.cise.message;

import java.util.LinkedList;
import java.util.List;

public class Utility {

    public static byte[] convertToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static int convertToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    public static void removeMapElement(int pieceId) {
        synchronized (MessageHandler.class) {
            List<Integer> nullList = new LinkedList<>();
            for (Integer id : MessageHandler.currentPeerInterestingTracker.keySet()) {
                if (MessageHandler.currentPeerInterestingTracker.get(id).contains(pieceId)) {
                    MessageHandler.currentPeerInterestingTracker.get(id).remove(Integer.valueOf(pieceId));
                }
                if (MessageHandler.currentPeerInterestingTracker.get(id).size() == 0) {
                    nullList.add(id);
                }
            }
            for (Integer id : nullList) {
                MessageHandler.currentPeerInterestingTracker.remove(id);
            }
        }
    }

    public static synchronized void removeAllMapElements() {
        for (Integer id : MessageHandler.otherPeerInterestingTracker.keySet()) {
            MessageHandler.otherPeerInterestingTracker.put(id, 0);
        }
    }
}
