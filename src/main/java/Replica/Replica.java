package Replica;

import Context.ServerContext;
import RespParser.*;
import Utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Replica {
    ServerContext serverContext;
    Socket socket;
    OutputStream out;
    InputStream in;

    public Replica(ServerContext serverContext) throws IOException {
        this.socket = new Socket(serverContext.getMasterIP(), serverContext.getMasterPort());
        System.out.println("Connected to server");

        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
        this.serverContext = serverContext;
    }

    public void performHandshake() throws Exception {
        sendPingCommand();
        sendReplConfCommand("listening-port", String.valueOf(serverContext.getPort()));
        sendReplConfCommand("capa", "psync2");
        sendPsyncCommand("?", "-1");
    }

    boolean sendPingCommand() throws IOException {
        String command = "PING";

        String request = Utils.formatCommand(command);

        // Send to master
        this.out.write(request.getBytes());
        this.out.flush();

        // Read server response
        RespParser parser = new RespParser(in);
        RespObject response = parser.parse();

        if(!(response instanceof RespSimpleString)) return false;
        return ((RespSimpleString) response).value.equals("PONG");

    }

    boolean sendReplConfCommand(String key, String val) throws IOException {
        String command = "REPLCONF" + " " + key + " " + val;
        String request = Utils.formatCommand(command);

        // Send to master
        this.out.write(request.getBytes());
        this.out.flush();

        // Read server response
        RespParser parser = new RespParser(in);
        RespObject response = parser.parse();

        if(!(response instanceof RespSimpleString)) return false;
        return ((RespSimpleString) response).value.equals("OK");
    }

    boolean sendPsyncCommand(String key, String val) throws IOException {
        String command = "PSYNC" + " " + key + " " + val;
        String request = Utils.formatCommand(command);

        // Send to master
        this.out.write(request.getBytes());
        this.out.flush();

        // Read server response
        RespParser parser = new RespParser(in);
        RespObject response1 = parser.parse();
        RespObject response2 = parser.parse();

        if(!(response1 instanceof RespSimpleString)) return false;
        return true;
    }

}
