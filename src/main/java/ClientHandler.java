import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    InputStream inputStream;
    OutputStream outputStream;
    // final String sep = "\\r\\n";
    final String sep = "\r\n";

    ConcurrentHashMap<String, ArrayList<String>> hm1;
    ConcurrentHashMap <String, ArrayList<String>> hm2;
    ConcurrentHashMap<String, String> hm3;
    LinkedHashSet <Long> lhs1;
    ConcurrentHashMap<String, ConcurrentSkipListMap<String, ConcurrentHashMap<String, String>>> hm4;

    ClientHandler(
            Socket clientSocket,
            ConcurrentHashMap<String, ArrayList<String>> hm1,
            ConcurrentHashMap<String, ArrayList<String>> hm2,
            ConcurrentHashMap<String, String> hm3,
            LinkedHashSet<Long> lhs1,
            ConcurrentHashMap<String, ConcurrentSkipListMap<String, ConcurrentHashMap<String, String>>> hm4
    ) throws IOException {
        this.clientSocket = clientSocket;
        this.inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        this.hm1 = hm1;
        this.hm2 = hm2;
        this.hm3 = hm3;
        this.lhs1 = lhs1;
        this.hm4 = hm4;
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
            return;
        }
        if(s.equalsIgnoreCase("set")){
            String key = command.get(1);
            String val = command.get(2);
            hm1.put(command.get(1), new ArrayList<>());
            hm1.get(key).add(val);
            if(command.size() > 3 && command.get(3).equalsIgnoreCase("px")){
                String ms = command.get(4);
                String timestamp = Instant.now().plusMillis(Integer.parseInt(ms)).toString();
                hm1.get(key).add(timestamp);
            }
            hm3.put(key, "string");
            outputStream.write(("+OK" + sep).getBytes());
            return;
        }
        if(s.equalsIgnoreCase("get")){
            String key = command.get(1);
            if(!hm1.containsKey(key)){
                outputStream.write(("$-1" + sep).getBytes());
                return;
            }
            ArrayList<String> val = hm1.get(key);

            if(val.size() > 1){
                Instant t1 = Instant.now();
                Instant t2 = Instant.parse(val.get(1));
                if(t1.isAfter(t2)){
                    hm1.remove(key);
                    hm3.remove(key);
                    outputStream.write(("$-1" + sep).getBytes());
                    return;
                }
            }
            outputStream.write(("$" + val.get(0).length() + sep + val.get(0) + sep).getBytes());
            return;
        }
        if(s.equalsIgnoreCase("rpush")){
            String key = command.get(1);
            List<String> vals = command.subList(2, command.size());

            if(!hm2.containsKey(key)){
                hm2.put(key, new ArrayList<>());
                hm3.put(key, "list");
            }
            for(String val: vals){
                hm2.get(key).add(val);
            }
            outputStream.write((":" + hm2.get(key).size() + sep).getBytes());
            return;
        }
        if(s.equalsIgnoreCase("lrange")){
            String key = command.get(1);
            if(!hm2.containsKey(key)){
                outputStream.write(("*0" + sep).getBytes());
                return;
            }
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
                return;
            }
            ArrayList <String> temp = new ArrayList<>();
            for(int i = l; i <= r && i < vals.size(); i++){
                temp.add("$" + vals.get(i).length() + sep + vals.get(i) + sep);
            }
            StringBuilder res = new StringBuilder("*" + temp.size() + sep);
            for(String t: temp){
                res.append(t);
            }
            outputStream.write(res.toString().getBytes());
            return;
        }
        if(s.equalsIgnoreCase("lpush")){
            String key = command.get(1);
            List<String> vals = command.subList(2, command.size());

            if(!hm2.containsKey(key)){
                hm2.put(key, new ArrayList<>());
                hm3.put(key, "list");
            }
            for(String val: vals){
                hm2.get(key).addFirst(val);
            }
            outputStream.write((":" + hm2.get(key).size() + sep).getBytes());
            return;
        }
        if(s.equalsIgnoreCase("llen")){
            String key = command.get(1);
            if(!hm2.containsKey(key)) {
                outputStream.write((":0"+sep).getBytes());
                return;
            }
            outputStream.write((":" + hm2.get(key).size() + sep).getBytes());
            return;
        }
        if(s.equalsIgnoreCase("lpop")){
            String key = command.get(1);
            int count = 0;
            if(command.size() == 3) count = Integer.parseInt(command.get(2));
            if(!hm2.containsKey(key)){
                outputStream.write(("$-1" + sep).getBytes());
                return;
            }
            if(count == 0){
                String val = hm2.get(key).removeFirst();
                if(hm2.get(key).isEmpty()) {
                    hm2.remove(key);
                    hm3.remove(key);
                }
                outputStream.write(("$" + val.length() + sep + val + sep).getBytes());
                return;
            }
            ArrayList <String> temp = new ArrayList<>();
            while(count > 0 && hm2.get(key).isEmpty()){
                count--;
                temp.add(hm2.get(key).removeFirst());
            }
            if(hm2.get(key).isEmpty()) {
                hm2.remove(key);
                hm3.remove(key);
            }
            StringBuilder res = new StringBuilder("*" + temp.size() + sep);
            for(String t: temp){
                res.append("$" + t.length() + sep + t + sep);
            }
            outputStream.write(res.toString().getBytes());
            return;
        }
        if(s.equalsIgnoreCase("blpop")){
            String key = command.get(1);
            double seconds = Double.parseDouble(command.get(2));

            if(!hm2.containsKey(key)){
                hm2.put(key, new ArrayList<>());
                hm3.put(key, "list");
            }

            ArrayList<String> temp = hm2.get(key);
            if(!temp.isEmpty()){
                String val = temp.removeFirst();
                outputStream.write(("*2" + sep + "$" + key.length() + sep + key + sep + "$" + val.length() + sep + val + sep).getBytes());
                return;
            }

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
                return;
            }

            Instant expiry = Instant.now().plusMillis((long) (seconds * 1000));
            synchronized (lhs1) {
                lhs1.add(Thread.currentThread().threadId());
            }
            boolean isExpired = false;
            while (temp.isEmpty() || lhs1.getFirst() != Thread.currentThread().threadId()) {
                Instant now = Instant.now();
                if (now.isAfter(expiry)) {
                    isExpired = true;
                    break;
                }
            }

            if (isExpired) {
                synchronized (lhs1) {
                    lhs1.remove(Thread.currentThread().threadId());
                }
                outputStream.write(("*-1" + sep).getBytes());
                return;
            }

            synchronized (lhs1) {
                lhs1.removeFirst();
            }
            String val = temp.removeFirst();
            outputStream.write(("*2" + sep + "$" + key.length() + sep + key + sep + "$" + val.length() + sep + val + sep).getBytes());
            return;
        }
        if(s.equalsIgnoreCase("type")){
            String key = command.get(1);
            if(!hm3.containsKey(key)){
                outputStream.write(("+none" + sep).getBytes());
            }
            else {
                outputStream.write(("+" + hm3.get(key) + sep).getBytes());
            }
        }
        if(s.equalsIgnoreCase("xadd")){
            String streamName = command.get(1);
            String streamId = command.get(2);

            if(!hm4.containsKey(streamName)){
                hm4.put(streamName, new ConcurrentSkipListMap<>());
            }

            ConcurrentSkipListMap<String, ConcurrentHashMap<String, String>> currentSLM = hm4.get(streamName);
            String lastEnteredStreamId = (currentSLM.isEmpty()) ? "0-0" : currentSLM.lastEntry().getKey();

            // if streamId == 0-0 throw error
            if(streamId.equals("0-0")){
                outputStream.write(("-ERR The ID specified in XADD must be greater than 0-0" + sep).getBytes());
                return;
            }

            // if lastEnteredStreamId >= streamId throw error
            if(lastEnteredStreamId.compareTo(streamId) >= 0){
                outputStream.write(("-ERR The ID specified in XADD is equal or smaller than the target stream top item" + sep).getBytes());
                return;
            }

            // store the key-vals
            currentSLM.put(streamId, new ConcurrentHashMap<>());
            for(int i = 3; i < command.size(); i += 2){
                currentSLM.get(streamId).put(command.get(i), command.get(i+1));
            }

            // store type of stream
            hm3.put(streamName, "stream");
            outputStream.write(("$" + streamId.length() + sep + streamId + sep).getBytes());

        }
        outputStream.write(("+PONG" + sep).getBytes());
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
