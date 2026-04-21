package Commands;

import Context.ClientContext;
import RespParser.RespArray;
import RespParser.RespError;
import RespParser.RespObject;

import java.io.IOException;
import java.util.ArrayList;

public class ExecCommand implements Command {

    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException, InterruptedException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly one argument passed, throw error
        //: EXEC
        if(command.size() != 1) {
            ctx.respWriter.write(new RespError("EXECABORT Transaction discarded because of: wrong number of arguments for 'exec' command"));
            return;
        }

        ctx.respWriter.write(new RespError("ERR EXEC without MULTI"));
    }
}
