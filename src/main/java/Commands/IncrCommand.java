package Commands;

import Context.ClientContext;
import RespParser.*;

import java.io.IOException;
import java.util.ArrayList;

public class IncrCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException, InterruptedException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly two argument passed, throw error
        //: INCR <key>
        if(command.size() != 2) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'incr' command"));
            return;
        }
        //: Extract key from command
        String key = ((RespBulkString) command.get(1)).value;

        //: If key doesn't exist, add <key, 1>
        if(!ctx.dataStore.store.containsKey(key)){
            ctx.dataStore.store.put(key, new RespBulkString("1"));
            ctx.respWriter.write(new RespInteger(1));
            return;
        }

        //: Key exists but value is not RespBulkString
        if(!(ctx.dataStore.store.get(key) instanceof RespBulkString)){
            ctx.respWriter.write(new RespError("WRONGTYPE Operation against a key holding the wrong kind of value"));
            return;
        }

        //: key exists but value not of numeric type
        try{
            int val = Integer.parseInt(((RespBulkString) ctx.dataStore.store.get(key)).value);

            ctx.dataStore.store.put(key, new RespBulkString(String.valueOf(val+1)));

            ctx.respWriter.write(new RespInteger(val+1));
            return;

        } catch (NumberFormatException e){
            ctx.respWriter.write(new RespError("ERR value is not an integer or out of range"));
            return;
        }

    }
}
