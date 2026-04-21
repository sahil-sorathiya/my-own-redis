package RespParser;

import java.util.*;

public class RespArray implements RespObject {
    public RespArray(ArrayList<RespObject> values) {
        this.values = values;
    }

    public ArrayList<RespObject> values;

    @Override
    public String getType(){
        return "list";
    }
}
