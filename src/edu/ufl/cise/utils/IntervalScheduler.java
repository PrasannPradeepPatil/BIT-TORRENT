package edu.ufl.cise.utils;

import edu.ufl.cise.message.MessageHandler;

import java.util.TimerTask;

public class IntervalScheduler extends TimerTask {

    public String type;
    public MessageHandler messageHandler;

    public IntervalScheduler(String type, MessageHandler messageHandler) {
        this.type = type;
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        synchronized (IntervalScheduler.class) {
            if (type.equals("PreferredNeighbor")) {
                messageHandler.findPreferredNeighbours();
            } else if (type.equals("OptimisticNeighbor")) {
                messageHandler.findOptimisticallyUnchokedNeighbor();
            }
        }
    }

    public void stopSignal() {
        System.gc();
        cancel();
    }
}
