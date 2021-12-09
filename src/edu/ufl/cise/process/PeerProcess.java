package edu.ufl.cise.process;

import edu.ufl.cise.logger.Logger;
import edu.ufl.cise.model.Peer;
import edu.ufl.cise.utils.CommonConfigParser;
import edu.ufl.cise.utils.FileParser;
import edu.ufl.cise.utils.PeerInfoConfigParser;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PeerProcess {

    public static Map<Integer, byte[]> peersBitfields = new ConcurrentHashMap<>();
    public static List<Integer> peersToProcess = new LinkedList<>();
    public static int firstPeerId;
    public static byte[] field;
    public static int optimalPeer;

    private PeerProcess(int peerId) {
        try {
            // Part 1 (a)
            CommonConfigParser commonConfigParser=new CommonConfigParser();
            commonConfigParser.read();
            PeerInfoConfigParser peerInfoConfigParser=new PeerInfoConfigParser();
            peerInfoConfigParser.read();
            List<Peer> peerInfo = peerInfoConfigParser.getPeerInfo();
            FileParser.fragment(peerId);
            new Logger(peerId);
            field = new byte[FileParser.getTotalPieces()];
            firstPeerId = peerInfo.get(0).getPeerId();
            if (peerId == firstPeerId) {
                for (Peer peer : peerInfo) {
                    if (!peer.isFilePresent()) {
                        peersToProcess.add(peer.getPeerId());
                    }
                }
            }
            int myIndex = FileParser.getPeerIndexById(peerId);
            if (!peerInfo.get(myIndex).isFilePresent()) {
                Arrays.fill(field, (byte) 0);
            } else {
                Arrays.fill(field, (byte) 1);
            }
            PeerListener peerListener = new PeerListener(peerId, peerInfo);
            peerListener.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new PeerProcess(Integer.parseInt(args[0]));
    }

}
