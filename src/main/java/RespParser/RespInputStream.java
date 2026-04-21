package RespParser;

import java.io.*;

public class RespInputStream {
    public final BufferedInputStream in;

    public RespInputStream(InputStream in) {
        this.in = new BufferedInputStream(in);
    }

    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int prev = -1;
        int curr;

        while (true) {
            curr = this.read();
            if (prev == '\r' && curr == '\n') {
                sb.setLength(sb.length() - 1); // remove \r
                break;
            }
            sb.append((char) curr);
            prev = curr;
        }

        return sb.toString();
    }

    public int read() throws IOException {
        int c = in.read();
//        System.out.println("Read : " + c + " === " + (char) c);
        return c;
    }

    public String readBytes(int length) throws IOException {
        StringBuilder sb = new StringBuilder();
        while(length != 0){
            length--;
            sb.append((char) this.read());
        }

        return sb.toString();
    }
}