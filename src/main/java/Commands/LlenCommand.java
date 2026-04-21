package Commands;

import Context.ClientContext;
import RespParser.*;

import java.io.IOException;
import java.util.ArrayList;

public class LlenCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly two argument passed, throw error
        //: LLEN <key>
        if(command.size() != 2) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'llen' command"));
            return;
        }

        //: Extract key from command
        String key = ((RespBulkString) command.get(1)).value;

        //: Check presence of key
        RespObject val = ctx.dataStore.store.get(key);

        //: Check if key not exist in store, respond 0
        if(val == null){
            ctx.respWriter.write(new RespInteger(0));
            return;
        }

        //: Check if val is not type of RespArray, throw error
        if(!(val instanceof RespArray)){
            ctx.respWriter.write(new RespError("WRONGTYPE Operation against a key holding the wrong kind of value"));
            return;
        }

        //: Respond with List length
        ctx.respWriter.write(new RespInteger(((RespArray) val).values.size()));
    }
}
