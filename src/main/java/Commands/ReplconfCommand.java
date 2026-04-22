package Commands;

import Context.ClientContext;
import RespParser.RespArray;
import RespParser.RespError;
import RespParser.RespObject;
import RespParser.RespSimpleString;

import java.io.IOException;
import java.util.ArrayList;

public class ReplconfCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException, InterruptedException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly three argument passed, throw error
        //: RESPCONFIG <key> <value>
        if(command.size() != 3) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'respconfig' command"));
            return;
        }

        ctx.respWriter.write(new RespSimpleString("OK"));
    }
}
