import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    InputStream inputStream;
    OutputStream outputStream;
//    final String sep = "\\r\\n";
    final String sep = "\r\n";

    HashMap <String, String> hm = new HashMap<>();

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

                String msg = new String(input).trim();

                int index = 0;
                int sepIndex = 0;
                ArrayList<String> command = new ArrayList<>();

                if (msg.charAt(index) == '*') {
                    index++;
                    sepIndex = msg.indexOf(sep, index);
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
                            index = index + stringLength + sep.length();
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

                String s = command.get(0);
                if(s.equalsIgnoreCase("echo")){
                    outputStream.write(("$" + command.get(1).length() + sep + command.get(1) + sep).getBytes());
                }
                else if(s.equalsIgnoreCase("set")){
                    hm.put(command.get(1), command.get(2));
                    outputStream.write(("+OK" + sep).getBytes());
                }
                else if(s.equalsIgnoreCase("get")){
                    String key = command.get(1);
                    if(hm.containsKey(key)){
                        String val = hm.get(key);
                        outputStream.write(("$" + val.length() + sep + val + sep).getBytes());
                    }
                    else {
                        outputStream.write(("$-1" + sep).getBytes());
                    }
                }
                else {
                    outputStream.write(("+PONG" + sep).getBytes());
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
