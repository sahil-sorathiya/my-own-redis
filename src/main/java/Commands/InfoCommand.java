package Commands;

import Context.ClientContext;
import Context.ServerContext;
import RespParser.RespArray;
import RespParser.RespBulkString;
import RespParser.RespError;
import RespParser.RespObject;

import java.io.IOException;
import java.util.ArrayList;

public class InfoCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException, InterruptedException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly two argument passed, throw error
        //: INFO <key>
        if(command.size() != 2) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'info' command"));
            return;
        }

        ctx.respWriter.write(
                new RespBulkString(
                "role:" + ctx.serverContext.getRole()
                    + "\nmaster_replid:" + ctx.serverContext.getMasterReplId()
                        + "\nmaster_repl_offset:" + ctx.serverContext.getMasterReplOffset()
                ));
    }
}
