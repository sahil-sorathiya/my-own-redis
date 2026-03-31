import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args){
        System.out.println("Logs from your program will appear here!");

        ServerSocket serverSocket = null;

        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            while(true){
                Socket clientSocket = null;
                clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

class ClientHandler extends Thread {
    private final Socket clientSocket;
    ClientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    public void run(){
        try {
            clientSocket.getOutputStream().write("+PONG\r\n".getBytes());
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
