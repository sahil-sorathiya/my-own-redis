package Commands;

import Context.ClientContext;
import RespParser.*;

import java.io.IOException;
import java.util.ArrayList;

public class LpushCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If less than three arguments passed, throw error
        //: LPUSH <key> <element1> [<element2>] ... [<elementN>]
        if(command.size() < 3) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'lpush' command"));
            return;
        }

        //: Extract key from command
        String key = ((RespBulkString) command.get(1)).value;

        //: Check presence of key
        RespObject val = ctx.dataStore.store.get(key);

        //: Check if key not exist in store, insert the key with empty list first
        if(val == null){
            ctx.dataStore.store.put(key, new RespArray(new ArrayList<RespObject>()));
            val = ctx.dataStore.store.get(key);
        }

        //: Check if val is not type of RespArray, throw error
        if(!(val instanceof RespArray)){
            ctx.respWriter.write(new RespError("WRONGTYPE Operation against a key holding the wrong kind of value"));
            return;
        }

        //: Append all items to list
        for(int i = 2; i < command.size(); i++){
            //: BLPOP : If blpopQueue exists for given key, try to offer key directly to consumer
            boolean isOfferAccepted = false;
            if(ctx.dataStore.keyToBlpopQueue.containsKey(key)){
                isOfferAccepted = ctx.dataStore.keyToBlpopQueue.get(key).offer(command.get(i));
            }

            //: If offer rejected that means no consumer was waiting for this data
            //: Store it in a dataStore
            if(isOfferAccepted == false){
                ((RespArray) val).values.addFirst(command.get(i));
            }
        }

        //: Respond with List length
        ctx.respWriter.write(new RespInteger(command.size() - 2));
    }
}
