package RespParser;

public class RespBulkString implements RespObject {
    public RespBulkString(String value){
        this.value = value;
    }

    public String value;

    @Override
    public String getType(){
        return "string";
    }
}
