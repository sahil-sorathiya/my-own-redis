package Commands;

import Context.ClientContext;
import RespParser.*;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;

public class LrangeCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly four arguments passed, throw error
        //: LRANGE <key> <left> <right>
        if(command.size() != 4) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'lrange' command"));
            return;
        }

        //: Extract key and range from command
        String key = ((RespBulkString) command.get(1)).value;
        String left = ((RespBulkString) command.get(2)).value;
        String right = ((RespBulkString) command.get(3)).value;

        //: Validating left and right
        int l = 0;
        int r = 0;
        try {
            l = Integer.parseInt(left);
            r = Integer.parseInt(right);
        } catch (NumberFormatException e) {
            ctx.respWriter.write(new RespError("ERR value is not an integer or out of range"));
        }

        //: Check presence of key
        RespObject val = ctx.dataStore.store.get(key);

        //: If key not found
        if(val == null){
            ctx.respWriter.write(new RespArray(new ArrayList<>()));
            return;
        }

        //: Check if val is not type of RespBulkString, throw error
        if(!(val instanceof RespArray)){
            ctx.respWriter.write(new RespError("WRONGTYPE Operation against a key holding the wrong kind of value"));
            return;
        }

        //: Bounding the ranges
        int size = ((RespArray) val).values.size();
        if(l < 0) l = Math.max(0, size + l);
        if(r < 0) r = Math.max(0, size + r);
        System.out.println(l + " " + r);
        l = Math.min(l, size);
        r = Math.min(r+1, size);
        if(l > r) l = r;

        //: Respond with list
        ArrayList<RespObject> list = new ArrayList<>(((RespArray) val).values.subList(l, r));
        ctx.respWriter.write(new RespArray(list));

    }
}
