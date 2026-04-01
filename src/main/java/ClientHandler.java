import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    InputStream inputStream;
    OutputStream outputStream;
    // final String sep = "\\r\\n";
    final String sep = "\r\n";

    ConcurrentHashMap<String, ArrayList<String>> hm1;
    ConcurrentHashMap <String, ArrayList<String>> hm2;
    LinkedHashSet <Long> lhs1;

    ClientHandler(Socket clientSocket, ConcurrentHashMap<String, ArrayList<String>> hm1, ConcurrentHashMap<String, ArrayList<String>> hm2, LinkedHashSet<Long> lhs1) throws IOException {
        this.clientSocket = clientSocket;
        this.inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        this.hm1 = hm1;
        this.hm2 = hm2;
        this.lhs1 = lhs1;
    }

    private String getInput() throws IOException {
        byte[] input = new byte[1024];
        int byteCount = inputStream.read(input);

        return new String(input).trim();
    }

    private ArrayList <String> parseInput(String msg){
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

        return command;
    }

    private void excecuteCommand(ArrayList<String> command) throws IOException{
        String s = command.get(0);
        if(s.equalsIgnoreCase("echo")){
            outputStream.write(("$" + command.get(1).length() + sep + command.get(1) + sep).getBytes());
        }
        else if(s.equalsIgnoreCase("set")){
            String key = command.get(1);
            String val = command.get(2);
            hm1.put(command.get(1), new ArrayList<>());
            hm1.get(key).add(val);
            if(command.size() > 3 && command.get(3).equalsIgnoreCase("px")){
                String ms = command.get(4);
                String timestamp = Instant.now().plusMillis(Integer.parseInt(ms)).toString();
                hm1.get(key).add(timestamp);
            }
            outputStream.write(("+OK" + sep).getBytes());
        }
        else if(s.equalsIgnoreCase("get")){
            String key = command.get(1);
            if(hm1.containsKey(key)){
                ArrayList<String> val = hm1.get(key);
                if(val.size() > 1){
                    Instant t1 = Instant.now();
                    Instant t2 = Instant.parse(val.get(1));
                    if(t1.isAfter(t2)){
                        hm1.remove(key);
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

            Thread.currentThread().threadId();
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
            int count = 0;
            if(command.size() == 3) count = Integer.parseInt(command.get(2));
            if(!hm2.containsKey(key)){
                outputStream.write(("$-1" + sep).getBytes());
            }
            else if(count == 0){
                String val = hm2.get(key).removeFirst();
                outputStream.write(("$" + val.length() + sep + val + sep).getBytes());
            }
            else{
                ArrayList <String> temp = new ArrayList<>();
                while(count > 0 && hm2.get(key).size() > 0){
                    count--;
                    temp.add(hm2.get(key).removeFirst());
                }
                StringBuilder res = new StringBuilder("*" + temp.size() + sep);
                for(String t: temp){
                    res.append("$" + t.length() + sep + t + sep);
                }
                outputStream.write(res.toString().getBytes());
            }
        }
        else if(s.equalsIgnoreCase("blpop")){
            String key = command.get(1);
            int seconds = Integer.parseInt(command.get(2));

            if(!hm2.containsKey(key)){
                outputStream.write(("*-1" + sep).getBytes());
            }
            else{
                ArrayList<String> temp = hm2.get(key);
                if(temp.isEmpty()){
                    if(seconds == 0){
                        synchronized (lhs1){
                            lhs1.add(Thread.currentThread().threadId());
                        }
                        while(temp.isEmpty() || lhs1.getFirst() != Thread.currentThread().threadId());
                        synchronized (lhs1){
                            lhs1.removeFirst();
                        }
                        String val = temp.removeFirst();
                        outputStream.write(("*2" + sep + "$" + key.length() + sep + key + sep + "$" + val.length() + sep + val + sep).getBytes());
                    }
                    else{

                    }
                }
                else {
                    String val = temp.removeFirst();
                    outputStream.write(("*2" + sep + "$" + key.length() + sep + key + sep + "$" + val.length() + sep + val + sep).getBytes());
                }
            }
        }
        else {
            outputStream.write(("+PONG" + sep).getBytes());
        }
        return;
    }

    public void run(){
        try {
            while (true) {
                String msg = getInput();
                ArrayList<String> command = parseInput(msg);
                excecuteCommand(command);
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
