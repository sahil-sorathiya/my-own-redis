import ClientHandler.ClientHandler;
import Context.ServerContext;
import DataStore.DataStore;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args){
        System.out.println("Logs from your program will appear here!");

        DataStore dataStore = new DataStore();

        ServerContext serverContext = new ServerContext(6379, "master");
//        ServerContext serverContext = new ServerContext(2727, "master");

        //: Parsing arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port")) {
                if (i + 1 < args.length) {
                    try {
                        serverContext.setPort(Integer.parseInt(args[i + 1]));
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
            else if(args[i].equals("--replicaof")){
                if (i + 2 < args.length) {
                    try {
                        serverContext.setRole("slave");
                        i++;
                        i++;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port number.");
                        return;
                    }
                } else {
                    System.out.println("Missing/Invalid value for --replicaof");
                    return;
                }
            }
        }

        try {
            ServerSocket serverSocket = new ServerSocket(serverContext.getPort());
            serverSocket.setReuseAddress(true);
            while(true){
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, dataStore, serverContext)).start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
