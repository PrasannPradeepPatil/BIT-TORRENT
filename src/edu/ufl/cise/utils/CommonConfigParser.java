package edu.ufl.cise.utils;

import java.io.*;

/**
 * @author Aryan
 */
public class CommonConfigParser {
    public static final String commonConfigFileName="Common.cfg";
    private static int numberOfPreferredNeighbours;
    private static int unchokingInterval;
    private static int optimisticUnchokingInterval;
    private static String fileName;
    private static int fileSize;
    private static int pieceSize;

    public void read(){
        try{
            BufferedReader br=new BufferedReader(new FileReader(commonConfigFileName));
            String line="";
            while((line=br.readLine())!=null){
                String configName = line.split(" ")[0];
                String configValue = line.split(" ")[1];

                if(configName.equals("NumberOfPreferredNeighbors")){
                    numberOfPreferredNeighbours = Integer.parseInt(configValue);
                }
                else if(configName.equals("UnchokingInterval")){
                    unchokingInterval = Integer.parseInt(configValue);
                }
                else if(configName.equals("OptimisticUnchokingInterval")){
                    optimisticUnchokingInterval = Integer.parseInt(configValue);
                }
                else if(configName.equals("FileName")){
                    fileName = configValue;
                }
                else if(configName.equals("FileSize")){
                    fileSize = Integer.parseInt(configValue);
                }
                else if(configName.equals("PieceSize")){
                    pieceSize = Integer.parseInt(configValue);
                }
            }
            br.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static int getNumberOfPreferredNeighbours() {
        return numberOfPreferredNeighbours;
    }

    public static void setNumberOfPreferredNeighbours(int numberOfPreferredNeighbours) {
        CommonConfigParser.numberOfPreferredNeighbours = numberOfPreferredNeighbours;
    }

    public static int getUnchokingInterval() {
        return unchokingInterval;
    }

    public static void setUnchokingInterval(int unchokingInterval) {
        CommonConfigParser.unchokingInterval = unchokingInterval;
    }

    public static int getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }

    public static void setOptimisticUnchokingInterval(int optimisticUnchokingInterval) {
        CommonConfigParser.optimisticUnchokingInterval = optimisticUnchokingInterval;
    }

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String fileName) {
        CommonConfigParser.fileName = fileName;
    }

    public static int getFileSize() {
        return fileSize;
    }

    public static void setFileSize(int fileSize) {
        CommonConfigParser.fileSize = fileSize;
    }

    public static int getPieceSize() {
        return pieceSize;
    }

    public static void setPieceSize(int pieceSize) {
        CommonConfigParser.pieceSize = pieceSize;
    }
}
