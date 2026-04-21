package Commands;

import Context.ClientContext;
import RespParser.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.concurrent.ConcurrentNavigableMap;

public class XrangeCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException, InterruptedException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly four arguments passed, throw error
        //: XRANGE <stream-key> <stream-id1> <stream-id2>
        if(command.size() != 4) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'xrange' command"));
            return;
        }

        //: Extract stream-key & stream-ids from command
        String streamKey = ((RespBulkString) command.get(1)).value;
        String rawId1 = ((RespBulkString) command.get(2)).value;
        String rawId2 = ((RespBulkString) command.get(3)).value;

        if(rawId1.equals("-")) rawId1 = "0-1";
        if(rawId2.equals("+")) rawId2 = Long.MAX_VALUE + "-" + Long.MAX_VALUE;

        String streamId1String = rawId1.contains("-") ? rawId1 : rawId1 + "-0";
        String streamId2String = rawId2.contains("-") ? rawId2 : rawId2 + "-" + Long.MAX_VALUE;

        //: Validate seq numbers
        try{
            RespStreamId.validateStreamId(streamId1String);
            RespStreamId.validateStreamId(streamId2String);
        } catch (NumberFormatException e){
            ctx.respWriter.write(new RespError("ERR Invalid stream ID specified as stream command argument"));
            return;
        } catch (InvalidPropertiesFormatException e){
            //: "0-0" Case
            ctx.respWriter.write(new RespError(e.getMessage()));
            return;
        }

        RespStreamId streamId1 = RespStreamId.parse(streamId1String);
        RespStreamId streamId2 = RespStreamId.parse(streamId2String);

        //: Validate stream key
        RespObject temp = ctx.dataStore.store.getOrDefault(streamKey, null);
        if(temp != null && !(temp instanceof RespStream)){
            ctx.respWriter.write(new RespError("WRONGTYPE Operation against a key holding the wrong kind of value"));
            return;
        }

        //: Key not found, return empty array
        if(temp == null){
            ctx.respWriter.write(new RespArray(new ArrayList<>()));
            return;
        }
        RespStream s = (RespStream) temp;

        ConcurrentNavigableMap<RespStreamId, RespStreamEntry> result = s.streams.subMap(streamId1, true, streamId2, true);

        RespArray response = new RespArray(new ArrayList<>());

        for (var entry : result.entrySet()) {
            RespStreamId id = entry.getKey();
            RespStreamEntry en = entry.getValue();

            ArrayList <RespObject> tempList1 = new ArrayList<>();
            ArrayList <RespObject> tempList2 = new ArrayList<>();
            for(var pair : en.streamEntry.entrySet()){
                String key = pair.getKey();
                RespObject value = pair.getValue();
                tempList2.add(new RespBulkString(key));
                tempList2.add(value);
            }

            tempList1.add(new RespBulkString(id.toString()));
            tempList1.add(new RespArray(tempList2));

            response.values.add(new RespArray(tempList1));
        }

        //: Respond with resultant array
        ctx.respWriter.write(response);
    }
}
