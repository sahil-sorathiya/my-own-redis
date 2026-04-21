package RespParser;

import java.io.*;
import java.util.ArrayList;

public class RespWriter {
    private final OutputStream out;

    public RespWriter(OutputStream out) {
        this.out = out;
    }

    public void write(RespObject obj) throws IOException {
        if (obj instanceof RespSimpleString s) {
            writeSimpleString(s.value);

        } else if (obj instanceof RespError e) {
            writeError(e.message);

        } else if (obj instanceof RespInteger i) {
            writeInteger(i.value);

        } else if (obj instanceof RespBulkString b) {
            writeBulkString(b.value);

        } else if (obj instanceof RespArray a) {
            writeArray(a.values);

        } else {
            throw new IOException("Unknown RESP object");
        }
    }

    public void writeSimpleString(String value) throws IOException {
        out.write(("+"
                + value
                + "\r\n").getBytes());
    }

    public void writeError(String message) throws IOException {
        out.write(("-"
                + message
                + "\r\n").getBytes());
    }

    public void writeInteger(long value) throws IOException {
        out.write((":"
                + value
                + "\r\n").getBytes());
    }

    public void writeBulkString(String value) throws IOException {
        if (value == null) {
            out.write("$-1\r\n".getBytes());
            return;
        }

        byte[] data = value.getBytes();

        out.write(("$" + data.length + "\r\n").getBytes());
        out.write(data);
        out.write("\r\n".getBytes());
    }

    public void writeArray(ArrayList<RespObject> values) throws IOException {
        if (values == null) {
            out.write("*-1\r\n".getBytes());
            return;
        }

        out.write(("*" + values.size() + "\r\n").getBytes());

        for (RespObject obj : values) {
            write(obj); // recursion
        }
    }
}
