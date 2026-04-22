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

    }

    void sendPingCommand() throws Exception {
        String command = "PING";

        String request = Utils.formatCommand(command);

        // Send to master
        this.out.write(request.getBytes());
        this.out.flush();

        // Read server response
        RespParser parser = new RespParser(in);
        RespObject response = parser.parse();

        if(!(response instanceof RespSimpleString)){
            throw new Exception("Invalid response to ping command");
        }

        System.out.println(((RespSimpleString) response).value);

    }

}
