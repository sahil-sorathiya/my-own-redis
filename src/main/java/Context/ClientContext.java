package Context;

import DataStore.DataStore;
import RespParser.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Instant;

public class ClientContext {
    public final Socket clientSocket;
    public InputStream inputStream;
    public OutputStream outputStream;
    public RespParser respParser;
    public RespWriter respWriter;

    public DataStore dataStore;

    public ClientContext(Socket clientSocket, DataStore dataStore) throws IOException {
        this.clientSocket = clientSocket;
        this.inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        this.respParser = new RespParser(inputStream);
        this.respWriter = new RespWriter(outputStream);
        this.dataStore = dataStore;
    }
}
