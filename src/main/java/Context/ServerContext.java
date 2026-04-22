package Context;

public class ServerContext {
    public int port;
    public String role;

    public ServerContext(int port, String role) {
        this.port = port;
        this.role = role;
    }

    public ServerContext(){}

    public int getPort() {
        return port;
    }

    public String getRole() {
        return role;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
