import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class Main {
    public static void main(String[] args){
        System.out.println("Logs from your program will appear here!");

        ConcurrentHashMap<String, ArrayList<String>> hm1 = new ConcurrentHashMap<>(); // For <key, <val, timestamp>> pairs
        ConcurrentHashMap<String, ArrayList<String>> hm2 = new ConcurrentHashMap<>(); // For <key, list>
        ConcurrentHashMap<String, String> hm3 = new ConcurrentHashMap<>(); // For <key, type> pairs
        LinkedHashSet<Long> lhs1 = new LinkedHashSet<Long>(); // Queue of threads blocked in "BLPOP" command
        ConcurrentHashMap<String, ConcurrentSkipListMap<String, ConcurrentHashMap<String, String>>> hm4 = new ConcurrentHashMap<>(); // For streams <streamName, <streamId, <key, val>>>
        ServerSocket serverSocket = null;

        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            while(true){
                Socket clientSocket = null;
                clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, hm1, hm2, hm3, lhs1, hm4).start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
