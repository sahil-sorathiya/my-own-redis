package Commands;

import Context.ClientContext;
import RespParser.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class XaddCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException, InterruptedException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If less than five arguments passed or number of args are even, throw error
        //: XADD <stream-key> <stream-id> <key1> <val1> [<key2> <val2>] .. [<keyN> <valN>]
        if (command.size() < 5 || command.size() % 2 == 0) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'xadd' command"));
            return;
        }

        //: Extract stream-key & stream-id from command
        String streamKey = ((RespBulkString) command.get(1)).value;
        String streamIdString = ((RespBulkString) command.get(2)).value;

        //: Validate stream-id
        try {
            boolean isValidated = RespStreamId.validateStreamId(streamIdString);
            if (!isValidated) {
                ctx.respWriter.write(new RespError("ERR Invalid stream ID specified as stream command argument"));
            }
        } catch (NumberFormatException e) {
            ctx.respWriter.write(new RespError("ERR Invalid stream ID specified as stream command argument"));
            return;
        } catch (InvalidPropertiesFormatException e) {
            //: "0-0" Case
            ctx.respWriter.write(new RespError(e.getMessage()));
            return;
        }


        //: Extract stream from store and validate it
        RespStream s = null;
        RespObject temp = ctx.dataStore.store.getOrDefault(streamKey, null);

        if (temp != null){
            if (temp instanceof RespStream) {
                s = (RespStream) temp;
            }
            else {
                ctx.respWriter.write(new RespError("WRONGTYPE Operation against a key holding the wrong kind of value"));
                return;
            }
        }

        //: Auto generate whole stream-id
        RespStreamId streamId;
        if(streamIdString.equals("*")){
            if(s == null || s.streams.size() == 0){
                streamId = new RespStreamId(0,1);
            }
            else{
                RespStreamId lastStreamId = s.streams.lastKey();
                streamId = new RespStreamId(lastStreamId.timestamp(), lastStreamId.sequence() + 1);
            }
        }
        //: Auto generate partial stream-id
        else if(streamIdString.split("-")[1].equals("*")){
            if(s == null || s.streams.size() == 0){
                streamId = new RespStreamId(Long.parseLong(streamIdString.split("-")[0]), 0);
            }
            else{
                RespStreamId lastStreamId = s.streams.lastKey();
                if(Long.parseLong(streamIdString.split("-")[0]) > lastStreamId.timestamp()){
                    streamId = new RespStreamId(Long.parseLong(streamIdString.split("-")[0]), 0);
                }
                else{
                    streamId = new RespStreamId(Long.parseLong(streamIdString.split("-")[0]), lastStreamId.sequence() +1);
                }
            }
        }
        else {
            streamId = RespStreamId.parse(streamIdString);
        }

        //: Validate stream-id
        if(s != null && s.streams.size() != 0){
                RespStreamId lastStreamId = s.streams.lastKey();

                //: Timestamp is less than last entry's milliseconds
                if(streamId.timestamp() < lastStreamId.timestamp()){
                    ctx.respWriter.write(new RespError("ERR The ID specified in XADD is equal or smaller than the target stream top item"));
                    return;
                }

                //: Timestamp is equal but seq number is small
                if(streamId.timestamp() == streamId.sequence() && streamId.sequence() <= lastStreamId.sequence()){
                    ctx.respWriter.write(new RespError("ERR The ID specified in XADD is equal or smaller than the target stream top item"));
                    return;
                }
        }

        //: If stream-key not exists in store, create & push it
        ctx.dataStore.store.putIfAbsent(streamKey, new RespStream(new ConcurrentSkipListMap<>()));
        s = (RespStream) ctx.dataStore.store.get(streamKey);

        //: Put timestamp entry in store
        s.streams.put(streamId, new RespStreamEntry(new ConcurrentHashMap<>()));

        //: Save key value pairs
        for(int i = 3; i < command.size(); i+=2){
            String key = ((RespBulkString) command.get(i)).value;
            RespObject value = command.get(i+1);

            s.streams.get(streamId).streamEntry.put(key, value);
        }

        //: Respond with timestamp
        ctx.respWriter.write(new RespBulkString(streamId.toString()));
    }
}
