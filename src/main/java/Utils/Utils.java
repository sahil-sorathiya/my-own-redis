package Utils;

import Context.ServerContext;

public class Utils {
    public static String formatCommand(String userInput){
        String[] command = userInput.split(" ");

        StringBuilder request = new StringBuilder();
        request.append('*');
        request.append(command.length);
        request.append('\r');
        request.append('\n');

        for(int i = 0; i < command.length; i++){
            request.append('$');
            request.append(command[i].length());
            request.append('\r');
            request.append('\n');
            request.append(command[i]);
            request.append('\r');
            request.append('\n');
        }

        return request.toString();
    }

    public static void parseArguments(String[] args, ServerContext serverContext){
        //: Parsing arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port")) {
                if (i + 1 < args.length) {
                    try {
                        serverContext.setPort(Integer.parseInt(args[i + 1]));
                        i++;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port number.");
                        return;
                    }
                } else {
                    System.out.println("Missing value for --port");
                    return;
                }
            }
            else if(args[i].equals("--replicaof")){
                if (i + 1 < args.length) {
                    try {
                        serverContext.setRole("slave");
                        serverContext.setMasterIP(args[i+1].split(" ")[0]);
                        serverContext.setMasterPort(Integer.parseInt(args[i+1].split(" ")[1]));
                        i++;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port number.");
                        return;
                    }
                } else {
                    System.out.println("Missing/Invalid value for --replicaof");
                    return;
                }
            }
//            System.out.println(serverContext.getMasterIP());
//            System.out.println(serverContext.getMasterPort());
//            System.out.println(serverContext.getPort());
//            System.out.println(serverContext.getRole());
        }
    }
}
