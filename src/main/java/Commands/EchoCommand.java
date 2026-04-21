package Commands;

import Context.*;
import RespParser.*;

import java.io.IOException;

public class EchoCommand implements Command {

    @Override
    public void execute(RespObject command, ClientContext ctx) throws IOException {
        //: If not exactly two arguments passed, throw error
        //: ECHO <message>
        if(((RespArray)command).values.size() != 2){
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'echo' command"));
        }
        ctx.respWriter.write(((RespArray)command).values.get(1));
    }
}
