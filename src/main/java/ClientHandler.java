import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    InputStream inputStream;
    OutputStream outputStream;
//    final String sep = "\\r\\n";
    final String sep = "\r\n";

    ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
    }

    public void run(){
        try {
            while (true) {
                byte[] input = new byte[1024];
                int byteCount = inputStream.read(input);

                System.out.println("byteCount : " + byteCount);

                String msg = new String(input).trim();
                System.out.println("Incoming message is : " + msg);

                int index = 0;
                int sepIndex = 0;
                ArrayList<String> command = new ArrayList<>();

                if (msg.charAt(index) == '*') {
                    index++;
                    sepIndex = msg.indexOf(sep, index);
                    System.out.println("sepIndex : " + sepIndex + " " + sep);
                    int arrayLength = Integer.parseInt(msg.substring(index, sepIndex));
                    index = sepIndex + sep.length();

                    for (int i = 0; i < arrayLength; i++) {
                        if (msg.charAt(index) == '+') {
                            index++;
                            sepIndex = msg.indexOf(sep, index);
                            command.add(msg.substring(index, sepIndex));
                            index = sepIndex + sep.length();
                        } else if (msg.charAt(index) == '$') {
                            index++;
                            sepIndex = msg.indexOf(sep, index);
                            int stringLength = Integer.parseInt(msg.substring(index, sepIndex));
                            index = sepIndex + sep.length();
                            command.add(msg.substring(index, index + stringLength));
                            index = index + stringLength + 4;
                        }
                    }
                }
                else if (msg.charAt(index) == '+') {
                    index++;
                    sepIndex = msg.indexOf(sep, index);
                    command.add(msg.substring(index, sepIndex));
                    index = sepIndex + sep.length();
                } else if (msg.charAt(index) == '$') {
                    index++;
                    sepIndex = msg.indexOf(sep, index);
                    int stringLength = Integer.parseInt(msg.substring(index, sepIndex));
                    index = sepIndex + sep.length();
                    command.add(msg.substring(index, index + stringLength));
                    index = index + stringLength + 4;
                }

                System.out.println("Index : " + index);
                for(String c: command){
                    System.out.println(c);
                }

                if(command.get(0).equalsIgnoreCase("echo")){
                    outputStream.write(new String("$" + command.get(1).length() + sep + command.get(1) + sep).getBytes());
                }
                else {
                    outputStream.write(new String("+PONG" + sep).getBytes());
                }

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
