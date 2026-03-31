import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    InputStream inputStream;
    OutputStream outputStream;
//    final String sep = "\\r\\n";
    final String sep = "\r\n";

    HashMap <String, ArrayList<String>> hm = new HashMap<>();
    HashMap <String, ArrayList<String>> hm2 = new HashMap<>();

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
                }
                else if (msg.charAt(index) == '$') {
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
                    String key = command.get(1);
                    String val = command.get(2);
                    hm.put(command.get(1), new ArrayList<>());
                    hm.get(key).add(val);
                    if(command.size() > 3 && command.get(3).equalsIgnoreCase("px")){
                        String ms = command.get(4);
                        String timestamp = Instant.now().plusMillis(Integer.parseInt(ms)).toString();
                        hm.get(key).add(timestamp);
                    }
                    outputStream.write(("+OK" + sep).getBytes());
                }
                else if(s.equalsIgnoreCase("get")){
                    String key = command.get(1);
                    if(hm.containsKey(key)){
                        ArrayList<String> val = hm.get(key);
                        if(val.size() > 1){
                            Instant t1 = Instant.now();
                            Instant t2 = Instant.parse(val.get(1));
                            if(t1.isAfter(t2)){
                                hm.remove(key);
                                outputStream.write(("$-1" + sep).getBytes());
                            }
                            else{
                                outputStream.write(("$" + val.get(0).length() + sep + val.get(0) + sep).getBytes());
                            }
                        }
                        else{
                            outputStream.write(("$" + val.get(0).length() + sep + val.get(0) + sep).getBytes());
                        }
                    }
                    else {
                        outputStream.write(("$-1" + sep).getBytes());
                    }
                }
                else if(s.equalsIgnoreCase("rpush")){
                    String key = command.get(1);
                    List<String> vals = command.subList(2, command.size());

                    if(!hm2.containsKey(key)){
                        hm2.put(key, new ArrayList<>());
                    }
                    for(String val: vals){
                        hm2.get(key).add(val);
                    }
                    outputStream.write((":" + hm2.get(key).size() + sep).getBytes());
                }
                else if(s.equalsIgnoreCase("lrange")){
                    String key = command.get(1);
                    if(!hm2.containsKey(key)){
                        outputStream.write(("*0" + sep).getBytes());
                    }
                    else{
                        ArrayList<String> vals = hm2.get(key);
                        int l = Integer.parseInt(command.get(2));
                        int r = Integer.parseInt(command.get(3));

                        if(l < 0) {
                            if(l < -1*vals.size()) l = 0;
                            else l = vals.size() + l;
                        }

                        if(r < 0){
                            if(r < -1*vals.size()) r = 0;
                            else r = vals.size() + r;
                        }

                        if(l >= vals.size() || l > r){
                            outputStream.write(("*0" + sep).getBytes());
                        }
                        else{
                            ArrayList <String> temp = new ArrayList<>();
                            for(int i = l; i <= r && i < vals.size(); i++){
                                temp.add("$" + vals.get(i).length() + sep + vals.get(i) + sep);
                            }
                            StringBuilder res = new StringBuilder("*" + temp.size() + sep);
                            for(String t: temp){
                                res.append(t);
                            }
                            outputStream.write(res.toString().getBytes());
                        }
                    }

                }
                else if(s.equalsIgnoreCase("lpush")){
                    String key = command.get(1);
                    List<String> vals = command.subList(2, command.size());

                    if(!hm2.containsKey(key)){
                        hm2.put(key, new ArrayList<>());
                    }
                    for(String val: vals){
                        hm2.get(key).addFirst(val);
                    }
                    outputStream.write((":" + hm2.get(key).size() + sep).getBytes());
                }
                else if(s.equalsIgnoreCase("llen")){
                    String key = command.get(1);
                    if(!hm2.containsKey(key)) outputStream.write((":0"+sep).getBytes());
                    else outputStream.write((":" + hm2.get(key).size() + sep).getBytes());
                }
                else if(s.equalsIgnoreCase("lpop")){
                    String key = command.get(1);
                    if(!hm2.containsKey(key)){
                        outputStream.write(("$-1" + sep).getBytes());
                    }
                    else{
                        String val = hm2.get(key).removeFirst();
                        outputStream.write(("$" + val.length() + val + sep).getBytes());
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
