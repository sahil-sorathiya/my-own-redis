package Commands;

import Context.*;
import RespParser.*;

import java.io.IOException;

public class PingCommand implements Command {

    @Override
    public void execute(RespObject command, ClientContext ctx) throws IOException {
        //: If not exactly one argument passed, throw error
        //: PING
        if(((RespArray)command).values.size() != 1) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'ping' command"));
            return;
        }
        ctx.respWriter.write(new RespSimpleString("PONG"));
    }
}
