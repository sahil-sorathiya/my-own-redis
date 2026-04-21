package RespParser;

import java.util.concurrent.ConcurrentHashMap;

public class RespStreamEntry implements RespObject {
    public ConcurrentHashMap<String, RespObject> streamEntry;

    public RespStreamEntry(ConcurrentHashMap<String, RespObject> streamEntry) {
        this.streamEntry = streamEntry;
    }

    @Override
    public String getType() {
        return "stream-entry";
    }
}
