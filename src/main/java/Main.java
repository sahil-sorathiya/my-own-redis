import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args){
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            clientSocket = serverSocket.accept();
            // Handle client in new thread
            new ClientHandler(clientSocket).start();

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}

class ClientHandler extends Thread {
    private final Socket socket;
    ClientHandler(Socket socket){
        this.socket = socket;
    }

    public void run(){
        try (
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String message;

            // Continuous listening loop
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);

                // Echo back to client
                writer.println("+PONG\\r\\n");

//                if (message.equalsIgnoreCase("exit")) {
//                    System.out.println("Client disconnected.");
//                    break;
//                }
            }

            socket.close();

        } catch (IOException ex) {
            System.out.println("Client handler exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
