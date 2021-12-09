package edu.ufl.cise.message;

import edu.ufl.cise.logger.Logger;
import edu.ufl.cise.model.MessageType;
import edu.ufl.cise.process.PeerProcess;
import edu.ufl.cise.process.RunPeer;
import edu.ufl.cise.utils.IntervalScheduler;
import edu.ufl.cise.utils.CommonConfigParser;
import edu.ufl.cise.utils.FileParser;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessageHandler {
    public static int peerId;
    public boolean interested = false;
    public static Map<Integer, RunPeer> RunPeerMapping = new ConcurrentHashMap<>();
    public static Map<Integer, List<Integer>> currentPeerInterestingTracker = new ConcurrentHashMap<>();
    public static Map<Integer, Integer> otherPeerInterestingTracker = new ConcurrentHashMap<>();
    public static Set<Integer> unchokedNeighbors = new HashSet<>();
    public static Set<Integer> optimallyUnchokedNeighbor = new HashSet<>();
    public IntervalScheduler unChokeIntervalScheduler;
    public IntervalScheduler optimalUnChokeIntervalScheduler;

    public MessageHandler(int peerId) {
        MessageHandler.peerId = peerId;
    }

    public void handleMessages(ObjectInputStream inputStream, ObjectOutputStream outputStream, int peerId) {
        byte[] peerBitfield;
        while (true) {
            //1 (c)
            if (MessageHandler.peerId == PeerProcess.firstPeerId && PeerProcess.peersToProcess.size() == 0) {
                for (Integer key : RunPeerMapping.keySet())
                    SendMessage.sendMessage(MessageType.all_finish.getMessageTypeValue(), RunPeerMapping.get(key).getOutputStream());
                Logger.applicationExit();
                System.exit(0);
            }

            try {
                byte[] message = (byte[]) inputStream.readObject();
                int messageType = message[4];
                if(messageType==MessageType.piece.getMessageTypeValue()){
                    synchronized (MessageHandler.class) {
                        if (otherPeerInterestingTracker.containsKey(peerId)) {
                            otherPeerInterestingTracker.put(peerId, otherPeerInterestingTracker.get(peerId) + 1);
                        } else {
                            otherPeerInterestingTracker.put(peerId, 1);
                        }
                        byte[] pieceIdInBytes = new byte[4];
                        System.arraycopy(message, 5, pieceIdInBytes, 0, 4);
                        int pieceId = Utility.convertToInt(pieceIdInBytes);
                        PeerProcess.field[pieceId] = 1;
                        Utility.removeMapElement(pieceId);
                        System.arraycopy(message, 9, FileParser.fileFragments[pieceId], 0, Math.min(message.length - 9, FileParser.fileFragments[pieceId].length));
                        // 3(b)
                        sendRequestOrHaveMessage(MessageType.have.getMessageTypeValue(), pieceId, outputStream);
                        for (Integer peerIndex : PeerProcess.peersBitfields.keySet()) {
                            peerBitfield = PeerProcess.peersBitfields.get(peerIndex);
                            isInterested(peerBitfield, RunPeerMapping.get(peerIndex).getOutputStream(), peerId);
                        }
                        int total = 0;
                        for (byte x : PeerProcess.field) {
                            if (x == 1) {
                                total += x;
                            }
                        }
                        Logger.downloadPiece(peerId,peerId,total);
                        boolean shouldCombine = true;
                        for (byte y : PeerProcess.field) {
                            if (y == 0 || y == 2) {
                                shouldCombine = false;
                            }
                        }
                        if (shouldCombine) {
                            Logger.downloadComplete();
                            FileParser.defragment(MessageHandler.peerId);
                            SendMessage.sendMessage(MessageType.finish.getMessageTypeValue(), RunPeerMapping.get(PeerProcess.firstPeerId).getOutputStream());
                        }
                    }
                }
                // 2(b)
                else if(messageType==MessageType.bitfield.getMessageTypeValue()){
                    synchronized (MessageHandler.class) {
                        byte[] peerField = new byte[PeerProcess.field.length];
                        System.arraycopy(message, 5, peerField, 0, PeerProcess.field.length);
                        PeerProcess.peersBitfields.put(peerId, peerField);
                        List<Integer> interestingPieces = findInterestingPieces(peerId);
                        if (interestingPieces != null) {
                            currentPeerInterestingTracker.put(peerId, interestingPieces);
                        }
                        // 2(c)
                        if (currentPeerInterestingTracker.containsKey(peerId)) {
                            SendMessage.sendMessage(MessageType.interested.getMessageTypeValue(), outputStream);
                            // 3(a)
                            selectRandomPiece(peerId, outputStream);
                        } else //3 (b)
                            SendMessage.sendMessage(MessageType.not_interested.getMessageTypeValue(), outputStream);
                    }
                }
                else if(messageType==MessageType.interested.getMessageTypeValue()){
                    Logger.receiveInterestedMessage(peerId);
                    if (!otherPeerInterestingTracker.containsKey(peerId)) {
                        otherPeerInterestingTracker.put(peerId, 0);
                    }
                    synchronized (MessageHandler.class) {
                        if (!interested) {
                            unChokeIntervalScheduler = new IntervalScheduler("PreferredNeighbor", this);
                            optimalUnChokeIntervalScheduler = new IntervalScheduler("OptimisticNeighbor", this);
                            Date date = new Date();
                            Timer timer = new Timer();
                            // 2 (d) (e)
                            timer.schedule(unChokeIntervalScheduler, date, 1000 * CommonConfigParser.getUnchokingInterval());
                            timer.schedule(optimalUnChokeIntervalScheduler, date, 1000 * CommonConfigParser.getOptimisticUnchokingInterval());
                            interested = true;
                        }
                    }
                }
                else if(messageType==MessageType.not_interested.getMessageTypeValue()){
                    Logger.receiveNotInterestedMessage(peerId);
                    otherPeerInterestingTracker.remove(peerId);
                }
                // 3 (e)
                else if(messageType==MessageType.request.getMessageTypeValue()){
                    while (true) {
                        if (unchokedNeighbors.contains(peerId) || optimallyUnchokedNeighbor.contains(peerId))
                            break;
                    }
                    byte[] pieceId = new byte[4];
                    System.arraycopy(message, 5, pieceId, 0, 4);
                    sendPiece(pieceId, outputStream);
                }
                // 3 (f)
                else if(messageType==MessageType.have.getMessageTypeValue()){
                    synchronized (MessageHandler.class) {
                        byte[] pieceId = new byte[4];
                        System.arraycopy(message, 5, pieceId, 0, 4);
                        if (!PeerProcess.peersBitfields.containsKey(peerId)) {
                            PeerProcess.peersBitfields.put(peerId, new byte[FileParser.getTotalPieces()]);
                        }
                        peerBitfield = PeerProcess.peersBitfields.get(peerId);
                        peerBitfield[Utility.convertToInt(pieceId)] = 1;
                        Logger.receiveHaveMessage(peerId,Utility.convertToInt(pieceId));
                        isInterested(PeerProcess.peersBitfields.get(peerId), outputStream, peerId);
                    }
                }
                else if(messageType==MessageType.choke.getMessageTypeValue())
                    Logger.choked(peerId);
                else if(messageType==MessageType.unchoke.getMessageTypeValue())
                    Logger.unchoked(peerId);
                else if(messageType==MessageType.finish.getMessageTypeValue()) //4
                    PeerProcess.peersToProcess.remove(Integer.valueOf(peerId));
                else if(messageType==MessageType.all_finish.getMessageTypeValue()) {
                    Logger.applicationExit();
                    System.exit(0);
                }
                else
                    System.out.println("Message Type is not recognizable");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void findPreferredNeighbours() {
        if (otherPeerInterestingTracker != null && otherPeerInterestingTracker.size() > 0) {
            boolean flag = true;
            for (int i = 0; i < PeerProcess.field.length; i++) {
                if (PeerProcess.field[i] == 0) {
                    flag = false;
                    break;
                }
            }
            if (flag==false){
                evaluateDownloadSpeed();
            } else {
                ArrayList<Integer> neighbors = new ArrayList<>(otherPeerInterestingTracker.keySet());
                ArrayList<Integer> inits=new ArrayList<>();
                int neighborsSize = neighbors.size();
                for (int i = 0; i < CommonConfigParser.getNumberOfPreferredNeighbours(); i++) {
                    if (i < neighborsSize) {
                        Random random = new Random();
                        int randomSelect = random.nextInt(neighbors.size());
                        int id = neighbors.get(randomSelect);
                        inits.add(id);
                        if (!unchokedNeighbors.contains(id))
                            SendMessage.sendMessage(MessageType.unchoke.getMessageTypeValue(), RunPeerMapping.get(id).getOutputStream());
                        neighbors.remove(randomSelect);
                    }
                }
                for (Integer i : neighbors) {
                    SendMessage.sendMessage(MessageType.choke.getMessageTypeValue(), RunPeerMapping.get(i).getOutputStream());
                }
                if (!unchokedNeighbors.isEmpty()) {
                    unchokedNeighbors.clear();
                }

                Logger.changePreferredNeighbours(inits);

                for (int i1 : inits) {
                    unchokedNeighbors.add(i1);
                }
            }
            Utility.removeAllMapElements();
        }
    }

    public synchronized void evaluateDownloadSpeed() {
        HashMap<Integer, Double> downloadRates = new HashMap<>();
        for (Map.Entry<Integer, Integer> peer : otherPeerInterestingTracker.entrySet()) {
            double dr = (peer.getValue() * CommonConfigParser.getPieceSize() * 1.0) / CommonConfigParser.getUnchokingInterval();
            downloadRates.put(peer.getKey(), dr);
        }
        List<Map.Entry<Integer, Double>> list = new ArrayList<>(downloadRates.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        for (Map.Entry<Integer, Double> mapping : list) {
            System.out.println(mapping.getKey() + ": " + mapping.getValue());
        }
        int[] inits = new int[CommonConfigParser.getNumberOfPreferredNeighbours()];
        int count = 0;
        List<Map.Entry<Integer, Double>> deleteUnChokeList = new LinkedList<>();
        for (Map.Entry<Integer, Double> mapping : list) {
            if (count < inits.length) {
                inits[count] = mapping.getKey();
                if (!unchokedNeighbors.contains(mapping.getKey()))
                    SendMessage.sendMessage(MessageType.unchoke.getMessageTypeValue(), RunPeerMapping.get(mapping.getKey()).getOutputStream());
                deleteUnChokeList.add(mapping);
                count++;
            } else {
                break;
            }
        }
        for (Map.Entry<Integer, Double> integerDoubleEntry : deleteUnChokeList)
            list.remove(integerDoubleEntry);
        if (list.size() > 0) {
            for (Map.Entry<Integer, Double> mapping : list) {
                SendMessage.sendMessage(MessageType.choke.getMessageTypeValue(), RunPeerMapping.get(mapping.getKey()).getOutputStream());
            }
        }
        if (!unchokedNeighbors.isEmpty())
            unchokedNeighbors.clear();
        for (int val : inits) {
            unchokedNeighbors.add(val);
        }
    }

    public synchronized void findOptimisticallyUnchokedNeighbor() {
        if (otherPeerInterestingTracker != null && otherPeerInterestingTracker.size() > 0) {
            List<Integer> candidatePeers = new ArrayList<>();
            for (int peerId : otherPeerInterestingTracker.keySet()) {
                if (!unchokedNeighbors.contains(peerId)) {
                    candidatePeers.add(peerId);
                }
            }
            if (candidatePeers.size() > 0) {
                PeerProcess.optimalPeer = candidatePeers.get(new Random().nextInt(candidatePeers.size()));
                SendMessage.sendMessage(MessageType.unchoke.getMessageTypeValue(), RunPeerMapping.get(PeerProcess.optimalPeer).getOutputStream());
                if (!optimallyUnchokedNeighbor.isEmpty()) {
                    optimallyUnchokedNeighbor.clear();
                }
                optimallyUnchokedNeighbor.add(PeerProcess.optimalPeer);
                Logger.changeOptimisticallyUnchokedNeighbour(PeerProcess.optimalPeer);
            }
        }
    }

    public static void sendBitField(ObjectOutputStream out) {
        if (!isBitfieldEmpty()) {
            return;
        }
        byte[] message = new byte[PeerProcess.field.length + 5];
        byte[] size = Utility.convertToByteArray(PeerProcess.field.length);
        byte[] type = Utility.convertToByteArray(MessageType.bitfield.getMessageTypeValue());

        for (int i = 0; i < message.length; i++) {
            if (i < 4) {
                message[i] = size[i];
            } else if (i == 4) {
                message[i] = type[3];
            } else {
                message[i] = PeerProcess.field[i - 5];
            }
        }
        SendMessage.pushMessage(message, out);
    }

    public static boolean isBitfieldEmpty() {
        boolean flag = false;
        for (byte b : PeerProcess.field) {
            if (b == 1) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public void createRunPeerMapping(int peerId, RunPeer runPeer) {
        RunPeerMapping.put(peerId, runPeer);
    }

    public void sendRequestOrHaveMessage(int type, int index, ObjectOutputStream outputStream) {
        byte[] message = new byte[9];
        byte[] size = Utility.convertToByteArray(4);
        byte[] indexByteArray = Utility.convertToByteArray(index);
        System.arraycopy(size, 0, message, 0, 4);
        message[4] = Utility.convertToByteArray(type)[3];
        System.arraycopy(indexByteArray, 0, message, 5, 4);
        SendMessage.pushMessage(message, outputStream);
    }

    public List<Integer> findInterestingPieces(int peerId) {
        List<Integer> result = new LinkedList<>();
        byte[] peerList = PeerProcess.peersBitfields.get(peerId);
        for (int i = 0; i < PeerProcess.field.length; i++) {
            if (PeerProcess.field[i] == 0 && peerList[i] == 1) {
                result.add(i);
            }
        }
        if (result.size() != 0) {
            return result;
        }
        return null;
    }

    public void sendPiece(byte[] pieceIndex, ObjectOutputStream out) {
        byte[] pieceValue = FileParser.getPiece(Utility.convertToInt(pieceIndex));
        int size = pieceValue.length;
        byte[] message = new byte[5 + 4 + size];
        byte[] messageSize = Utility.convertToByteArray(5 + size);
        byte type = Utility.convertToByteArray(MessageType.piece.getMessageTypeValue())[3];
        System.arraycopy(messageSize, 0, message, 0, 4);
        message[4] = type;
        System.arraycopy(pieceIndex, 0, message, 5, 4);
        System.arraycopy(pieceValue, 0, message, 9, size);
        SendMessage.pushMessage(message, out);
    }

    public void selectRandomPiece(int peerId, ObjectOutputStream outputStream) {
        synchronized (MessageHandler.class) {
            Random random = new Random();
            while (currentPeerInterestingTracker.containsKey(peerId)) {
                int index = random.nextInt(currentPeerInterestingTracker.get(peerId).size());
                if (PeerProcess.field[currentPeerInterestingTracker.get(peerId).get(index)] == 0) {
                    PeerProcess.field[currentPeerInterestingTracker.get(peerId).get(index)] = 2;
                    sendRequestOrHaveMessage(MessageType.request.getMessageTypeValue(), currentPeerInterestingTracker.get(peerId).get(index), outputStream);
                    Utility.removeMapElement(currentPeerInterestingTracker.get(peerId).get(index));
                    break;
                }
            }
        }
    }

    public void isInterested(byte[] message, ObjectOutputStream outputStream, int peerID) {
        boolean flag = false;
        for (int i = 0; i < PeerProcess.field.length; i++) {
            if (message[i] == 1 && PeerProcess.field[i] == 0) {
                SendMessage.sendMessage(MessageType.interested.getMessageTypeValue(), outputStream);
                selectRandomPiece(peerID, outputStream);
                flag = true;
                break;
            }
        }
        if (flag == false)
            SendMessage.sendMessage(MessageType.not_interested.getMessageTypeValue(), outputStream);
    }
}
