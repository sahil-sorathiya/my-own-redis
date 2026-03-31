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
        try (
//                InputStream input = clientSocket.getInputStream();
//                OutputStream output = clientSocket.getOutputStream();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        ) {
//            String line;
//
//            // Keep reading until client disconnects
////            while ((line = reader.readLine()) != null) {
//            while (true) {
////                System.out.println("Received: " + line);
//                String response = "+PONG\r\n";
//                output.write(response.getBytes());
//                output.flush();
//            }

            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            while (true) {
                byte[] input = new byte[1024];
                int byteCount = inputStream.read(input);
                String inputString = new String(input).trim();
                outputStream.write("+PONG\r\n".getBytes());
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}
