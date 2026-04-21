package RespParser;

import java.io.*;
import java.util.ArrayList;

public class RespParser {
    private final RespInputStream rin;

    public RespParser(InputStream input) {
        this.rin = new RespInputStream(input);
    }

    public RespObject parse() throws IOException {
        int prefix = rin.read();
        switch (prefix) {
            case '+':
                return new RespSimpleString(rin.readLine());

            case '-':
                return new RespError(rin.readLine());

            case ':':
                return new RespInteger(Integer.parseInt(rin.readLine()));

            case '$':
                return parseBulkString();

            case '*':
                return parseArray();

            case -1:
                return null;

            default:
                throw new IOException("Unknown RESP type: " + (char) prefix);
        }
    }

    private RespBulkString parseBulkString() throws IOException {
        int length = Integer.parseInt(rin.readLine());

        if (length == -1) return new RespBulkString(null);

        String data = rin.readBytes(length);
        rin.read();
        rin.read();

        return new RespBulkString(data);
    }

    private RespArray parseArray() throws IOException {
        int count = Integer.parseInt(rin.readLine());
        if (count == -1) return new RespArray(null);

        ArrayList<RespObject> elements = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            elements.add(parse()); // recursion!
        }

        return new RespArray(elements);
    }
}