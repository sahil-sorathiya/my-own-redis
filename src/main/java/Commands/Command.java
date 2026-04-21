package Commands;

import Context.ClientContext;
import RespParser.RespObject;

import java.io.IOException;

public interface Command {
    void execute(RespObject command, ClientContext ctx) throws IOException, InterruptedException;
}
