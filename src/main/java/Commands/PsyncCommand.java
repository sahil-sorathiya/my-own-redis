package Commands;

import Context.ClientContext;
import RespParser.RespArray;
import RespParser.RespError;
import RespParser.RespObject;
import RespParser.RespSimpleString;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class PsyncCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException, InterruptedException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly three argument passed, throw error
        //: RESPCONFIG <key> <value>
        if(command.size() != 3) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'respconfig' command"));
            return;
        }

        ctx.respWriter.write(new RespSimpleString("FULLRESYNC " + ctx.serverContext.getMasterReplId() + " " + ctx.serverContext.getMasterReplOffset()));

        String RDBInBase64 = "UkVESVMwMDEx+glyZWRpcy12ZXIFNy4yLjD6CnJlZGlzLWJpdHPAQPoFY3RpbWXCbQi8ZfoIdXNlZC1tZW3CsMQQAPoIYW9mLWJhc2XAAP/wbjv+wP9aog==";
        byte[] rdbBytes = Base64.getDecoder().decode(RDBInBase64);

        String header = "$" + rdbBytes.length + "\r\n";

        ctx.outputStream.write(header.getBytes(StandardCharsets.UTF_8));
        ctx.outputStream.write(rdbBytes);
    }
}
