package Commands;

import ClientHandler.ClientHandler;
import Context.ClientContext;
import RespParser.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MultiCommand implements Command {
    @Override
    public void execute(RespObject c, ClientContext ctx) throws IOException, InterruptedException {
        ArrayList<RespObject> command = ((RespArray) c).values;

        //: If not exactly one argument passed, throw error
        //: MULTI
        if(command.size() != 1) {
            ctx.respWriter.write(new RespError("ERR wrong number of arguments for 'multi' command"));
            return;
        }

        ctx.respWriter.write(new RespSimpleString("OK"));

        ArrayList <RespArray> queue = new ArrayList<>();

        while (true) {
            RespArray commandd = (RespArray) ctx.respParser.parse();
            if(commandd == null){
                ctx.clientSocket.close();
                break;
            }

            String commanddName = ((RespBulkString) commandd.values.get(0)).value;

            if (commanddName.equalsIgnoreCase("EXEC")) {
                ClientHandler ch = new ClientHandler(ctx);

                //: Setting respWriter with new outputStream temporarily
                ByteArrayOutputStream newOutputStream = new ByteArrayOutputStream();
                ctx.respWriter = new RespWriter(newOutputStream);

                //: Executing each command
                newOutputStream.write(("*" + queue.size() + "\r\n").getBytes());
                for(RespArray ra: queue){
                    ch.handleCommand(ra);
                }

                //: Setting respWriter back to old outputStream
                ctx.respWriter = new RespWriter(ctx.outputStream);

                //: Respond with all outputs
                ctx.outputStream.write(newOutputStream.toByteArray());
                break;
            }
            else if (commanddName.equalsIgnoreCase("DISCARD")) {
                ctx.respWriter.write(new RespSimpleString("OK"));
                break;
            }
            else{
                queue.add(commandd);
                ctx.respWriter.write(new RespSimpleString("QUEUED"));
            }
        }
    }
}
