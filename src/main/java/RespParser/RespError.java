package RespParser;

public class RespError implements RespObject {
    public RespError(String message) {
        this.message = message;
    }

    public String message;

    @Override
    public String getType(){
        return "string";
    }
}
