package Commands;

import Context.ClientContext;
import RespParser.RespArray;
import RespParser.RespBulkString;
import RespParser.RespError;
import RespParser.RespObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class BlpopCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException, InterruptedException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exact three argument passed, throw error
        //: BLPOP <key> <time>
        if(command.size() != 3) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'lpop' command"));
            return;
        }

        //: Extract key from command
        String key = ((RespBulkString) command.get(1)).value;

        //: Extract seconds from command
        long milliSeconds = 0;
        try {
            double seconds = Double.parseDouble(((RespBulkString)command.get(2)).value);
            milliSeconds = (long) (seconds * 1000);
        } catch (NumberFormatException e) {
            ctx.respWriter.write(new RespError("ERR value is not an integer or out of range"));
        }

        //: Check presence of key
        RespObject val = ctx.dataStore.store.get(key);

        //: Check if key not exist in store, ignore this
        if(val == null){
            //: We ignore if key not present, and client will wait for someone who will add that key
        }

        //: Check if val is not type of RespArray, throw error
        if(val != null && !(val instanceof RespArray)){
            ctx.respWriter.write(new RespError("WRONGTYPE Operation against a key holding the wrong kind of value"));
            return;
        }

        //: If key present, check for elements in list, if exists then pop it & return
        if(val != null && ((RespArray) val).values.size() != 0){
            RespObject element = ((RespArray) val).values.removeFirst();
            ArrayList <RespObject> list = new ArrayList<>();
            list.add(command.get(1));
            list.add(element);
            ctx.respWriter.write(new RespArray(list));
            return;
        }

        //: Reaching at this point means either key not present
        //: or key is present but there is no item in list
        //: Check that blpop map has queue for given key or not
        if(!ctx.dataStore.keyToBlpopQueue.containsKey(key)){
            ctx.dataStore.keyToBlpopQueue.put(key, new SynchronousQueue<>(true));
        }

        //: if seconds are non-zero, poll with timeout
        RespObject element;
        if(milliSeconds != 0){
            element = ctx.dataStore.keyToBlpopQueue.get(key).poll(milliSeconds, TimeUnit.MILLISECONDS);
        }
        //: else use take which will block this thread infinitely
        else {
            element = ctx.dataStore.keyToBlpopQueue.get(key).take();
        }

        //: If element is null means timeout happened
        if(element == null){
            ctx.respWriter.write(new RespArray(null));
            return;
        }

        //: Respond with element
        ArrayList <RespObject> list = new ArrayList<>();
        list.add(command.get(1));
        list.add(element);
        ctx.respWriter.write(new RespArray(list));
    }
}
