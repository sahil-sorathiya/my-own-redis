import ClientHandler.ClientHandler;
import DataStore.DataStore;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args){
        System.out.println("Logs from your program will appear here!");

        DataStore dataStore = new DataStore();

        int port = 6379;
//        int port = 2727;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            while(true){
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, dataStore)).start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
