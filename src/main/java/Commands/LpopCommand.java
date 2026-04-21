package Commands;

import Context.ClientContext;
import RespParser.*;

import java.io.IOException;
import java.util.ArrayList;

public class LpopCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly tow or three arguments passed, throw error
        //: LPOP <key> [<total-element-to-remove>]
        if(command.size() != 2 && command.size() != 3) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'lpop' command"));
            return;
        }

        //: Extract key from command
        String key = ((RespBulkString) command.get(1)).value;

        //: Check presence of key
        RespObject val = ctx.dataStore.store.get(key);

        //: Check if key not exist in store, insert the key with empty list first
        if(val == null){
            ctx.respWriter.write(new RespBulkString(null));
            return;
        }

        //: Check if val is not type of RespArray, throw error
        if(!(val instanceof RespArray)){
            ctx.respWriter.write(new RespError("WRONGTYPE Operation against a key holding the wrong kind of value"));
            return;
        }

        //: If two args are there
        int total = 1;
        if(command.size() == 3){
            try {
                total = Integer.parseInt(((RespBulkString)command.get(2)).value);
            } catch (NumberFormatException e) {
                ctx.respWriter.write(new RespError("ERR value is not an integer or out of range"));
            }
        }
        total = Math.min(total, ((RespArray) val).values.size());

        //: Remove "total" elements from first
        ArrayList<RespObject> list = new ArrayList<>();
        for(int i = 0; i < total; i++){
            RespObject element = ((RespArray) val).values.removeFirst();
            list.add(element);
        }

        //: Respond with removed elements
        //: If only one element is removed then send RespBulkString
        if(total == 1) {
            ctx.respWriter.write((RespBulkString) list.getFirst());
            return;
        }
        //: If not send RespArray
        ctx.respWriter.write(new RespArray(list));
    }
}
