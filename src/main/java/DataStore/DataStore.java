package DataStore;

import RespParser.RespObject;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

public class DataStore {
    public final ConcurrentHashMap<String, RespObject> store;
    public final ConcurrentHashMap<String, Instant> expiry;
    public final ConcurrentHashMap<String, SynchronousQueue<RespObject>> keyToBlpopQueue;

    public DataStore(){
        this.store = new ConcurrentHashMap<>();
        this.expiry = new ConcurrentHashMap<>();
        this.keyToBlpopQueue = new ConcurrentHashMap<>();
    }
}
