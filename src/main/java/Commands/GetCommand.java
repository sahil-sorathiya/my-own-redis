package Commands;

import Context.ClientContext;
import DataStore.DataStore;
import RespParser.RespArray;
import RespParser.RespBulkString;
import RespParser.RespError;
import RespParser.RespObject;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

public class GetCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly two argument passed, throw error
        //: GET <key>
        if(command.size() != 2) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'get' command"));
            return;
        }

        //: Extract key from command
        String key = ((RespBulkString) command.get(1)).value;

        //: Check presence of key
        RespObject val = ctx.dataStore.store.get(key);

        //: If key not found
        if(val == null){
            ctx.respWriter.write(new RespBulkString(null));
            return;
        }

        //: Check if val is not type of RespBulkString, throw error
        if(!(val instanceof RespBulkString)){
            ctx.respWriter.write(new RespError("WRONGTYPE Operation against a key holding the wrong kind of value"));
            return;
        }

        //: If key is expired
        if(Instant.now().isAfter(ctx.dataStore.expiry.get(key))){
            ctx.dataStore.store.remove(key);
            ctx.dataStore.expiry.remove(key);
            ctx.respWriter.write(new RespBulkString(null));
            return;
        }

        //: Respond with value if key is present and not expired
        ctx.respWriter.write((RespBulkString) val);
    }
}
