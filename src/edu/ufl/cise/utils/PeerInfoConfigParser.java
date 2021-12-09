package edu.ufl.cise.utils;

import edu.ufl.cise.model.Peer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Aryan
 */
public class PeerInfoConfigParser {

    public static final String peerInfoConfigFileName = "PeerInfo.cfg";
    public static final ArrayList<Peer> peerInfo = new ArrayList<Peer>();

    public void read(){
        try{
            BufferedReader br=new BufferedReader(new FileReader(peerInfoConfigFileName));
            String line="";
            while((line=br.readLine())!=null){
                String[] values = line.split(" ");
                Peer obj = new Peer(Integer.parseInt(values[0]), values[1], Integer.parseInt(values[2]), Integer.parseInt(values[3]) != 0);
                peerInfo.add(obj);
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public ArrayList<Peer> getPeerInfo() {
        return peerInfo;
    }
}
