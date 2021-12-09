package edu.ufl.cise.logger;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author Aryan
 */
public class Logger {
    private static FileWriter logs;
    private static int peerId;

    public Logger(int peerId) {
        try{
            Logger.peerId = peerId;
            logs = new FileWriter("log_peer_"+peerId+".log");
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized static void attemptConnection(int peer2Id){
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logs.append("["+timestamp+"]: Peer "+peerId+" makes a connection to Peer "+peer2Id+"\n");
            logs.flush();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized static void connectionMade(int peer2Id){
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logs.append("["+timestamp+"]: Peer "+peerId+" is connected from Peer "+peer2Id+"\n");
            logs.flush();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized static void changePreferredNeighbours(List<Integer> neighbours){
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logs.append("["+timestamp+"]: Peer "+peerId+" has the preferred neighbors "+neighbours+"\n");
            logs.flush();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized static void changeOptimisticallyUnchokedNeighbour(int peer2Id){
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logs.append("["+timestamp+"]: Peer "+peerId+" has the optimistically unchoked neighbor "+peer2Id+"\n");
            logs.flush();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized static void unchoked(int peer2Id){
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logs.append("["+timestamp+"]: Peer "+peerId+" is unchoked by "+peer2Id+"\n");
            logs.flush();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized static void choked(int peer2Id){
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logs.append("["+timestamp+"]: Peer "+peerId+" is choked by "+peer2Id+"\n");
            logs.flush();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized static void receiveHaveMessage(int peer2Id, int index){
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logs.append("["+timestamp+"]: Peer "+peerId+" received the ‘have’ message from "+peer2Id+" for the piece "+index+"\n");
            logs.flush();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized static void receiveInterestedMessage(int peer2Id){
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logs.append("["+timestamp+"]: Peer "+peerId+" received the ‘interested’ message from "+peer2Id+"\n");
            logs.flush();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized static void receiveNotInterestedMessage(int peer2Id){
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logs.append("["+timestamp+"]: Peer "+peerId+" received the ‘not interested’ message from "+peer2Id+"\n");
            logs.flush();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized static void downloadPiece(int peer2Id, int index, int totalPieces){
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logs.append("["+timestamp+"]: Peer "+peerId+" has downloaded the piece "+index+" from "+peer2Id+". Now" +
                    "the number of pieces it has is "+totalPieces+"\n");
            logs.flush();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized static void downloadComplete(){
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logs.append("["+timestamp+"]: Peer "+peerId+" has downloaded the complete file"+"\n");
            logs.flush();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized static void applicationExit(){
        try{
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            logs.append("["+timestamp+"]: Peer "+peerId+" is exiting since all the other peers have received the file"+"\n");
            logs.flush();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}