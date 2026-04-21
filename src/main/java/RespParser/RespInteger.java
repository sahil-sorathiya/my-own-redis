package RespParser;

public class RespInteger implements RespObject {
    public RespInteger(int value) {
        this.value = value;
    }

    public int value;

    @Override
    public String getType(){
        return "string";
    }
}
