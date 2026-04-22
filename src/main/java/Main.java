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

        //: Parsing arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port")) {
                if (i + 1 < args.length) {
                    try {
                        port = Integer.parseInt(args[i + 1]);
                        i++;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port number.");
                        return;
                    }
                } else {
                    System.out.println("Missing value for --port");
                    return;
                }
            }
        }

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
