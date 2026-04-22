import ClientHandler.ClientHandler;
import Context.ServerContext;
import DataStore.DataStore;
import Replica.Replica;
import RespParser.*;
import Utils.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args){
        System.out.println("Logs from program will appear here!");

        DataStore dataStore = new DataStore();

        ServerContext serverContext = new ServerContext(
                    6379,
                    "master",
                    "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb",
                    0,
                    "0.0.0.0",
                    0
                );


        try {
            Utils.parseArguments(args, serverContext);

            if(serverContext.getRole().equals("slave")){
                Replica replica = new Replica(serverContext);
                replica.performHandshake();
            }

            ServerSocket serverSocket = new ServerSocket(serverContext.getPort());
            serverSocket.setReuseAddress(true);

            while(true){
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, dataStore, serverContext)).start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
