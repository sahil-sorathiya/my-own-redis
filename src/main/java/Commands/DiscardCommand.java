package Commands;

import Context.ClientContext;
import RespParser.RespArray;
import RespParser.RespError;
import RespParser.RespObject;

import java.io.IOException;
import java.util.ArrayList;

public class DiscardCommand implements Command {

    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException, InterruptedException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly one argument passed, throw error
        //: DISCARD
        if(command.size() != 1) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'discard' command"));
            return;
        }

        ctx.respWriter.write(new RespError("ERR DISCARD without MULTI"));
    }
}
