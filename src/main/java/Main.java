import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main(String[] args){
        System.out.println("Logs from your program will appear here!");

        ConcurrentHashMap<String, ArrayList<String>> hm1 = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, ArrayList<String>> hm2 = new ConcurrentHashMap<>();
        LinkedHashSet<Long> lhs1 = new LinkedHashSet<Long>();
        ServerSocket serverSocket = null;

        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            while(true){
                Socket clientSocket = null;
                clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, hm1, hm2, lhs1).start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
