package RespParser;

public class RespSimpleString implements RespObject {
    public RespSimpleString(String value){
        this.value = value;
    }

    public String value;

    @Override
    public String getType(){
        return "string";
    }
}
