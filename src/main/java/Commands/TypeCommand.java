package Commands;

import Context.ClientContext;
import RespParser.*;

import java.io.IOException;
import java.util.ArrayList;

public class TypeCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException, InterruptedException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly two arguments passed, throw error
        //: TYPE <key>
        if(command.size() != 2) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'type' command"));
            return;
        }

        //: Extract key from command
        String key = ((RespBulkString) command.get(1)).value;

        //: Check presence of key
        RespObject val = ctx.dataStore.store.get(key);

        //: If key not found
        if(val == null){
            ctx.respWriter.write(new RespBulkString("none"));
            return;
        }

        ctx.respWriter.write(new RespSimpleString(val.getType()));
    }
}
