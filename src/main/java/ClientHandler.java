import Commands.*;
import Context.ClientContext;
import DataStore.DataStore;
import RespParser.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    ClientContext clientContext;
    DataStore dataStore;

    ClientHandler(Socket clientSocket, DataStore dataStore) throws IOException {
        this.clientContext = new ClientContext(clientSocket, dataStore);
        this.dataStore = dataStore;
    }

    private void handleCommand(RespArray command) throws IOException, InterruptedException {
        String commandName = ((RespBulkString) command.values.get(0)).value;

        if (commandName.equalsIgnoreCase("PING")) {
            new PingCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("ECHO")){
            new EchoCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("SET")){
            new SetCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("GET")){
            new GetCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("RPUSH")){
            new RpushCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("LRANGE")){
            new LrangeCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("LPUSH")){
            new LpushCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("LLEN")){
            new LlenCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("LPOP")){
            new LpopCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("BLPOP")){
            new BlpopCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("TYPE")){
            new TypeCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("XADD")){
            new XaddCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("XRANGE")){
            new XrangeCommand().execute(command, clientContext);
        }
        else if(commandName.equalsIgnoreCase("INCR")){
            new IncrCommand().execute(command, clientContext);
        }
        else {
            clientContext.respWriter.write(new RespSimpleString("ERR unknown command " + commandName));
        }
    }

    public void run(){
        try {
            while (true) {
                RespArray command = (RespArray) clientContext.respParser.parse();
                if(command == null){
                    clientContext.clientSocket.close();
                    break;
                }
                handleCommand(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
