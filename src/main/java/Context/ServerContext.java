package Context;

public class ServerContext {
    public int port;
    public String role;
    public String masterReplId;
    public long masterReplOffset;
    public String masterIP;
    public int masterPort;

    public ServerContext(int port, String role, String masterReplId, long masterReplOffset, String masterIP, int masterPort) {
        this.port = port;
        this.role = role;

        this.masterReplId = masterReplId;
        this.masterReplOffset = masterReplOffset;
        this.masterIP = masterIP;
        this.masterPort = masterPort;
    }

    public ServerContext(){}

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMasterReplId() {
        return masterReplId;
    }

    public void setMasterReplId(String masterReplId) {
        this.masterReplId = masterReplId;
    }

    public long getMasterReplOffset() {
        return masterReplOffset;
    }

    public void setMasterReplOffset(long masterReplOffset) {
        this.masterReplOffset = masterReplOffset;
    }

    public String getMasterIP() {
        return masterIP;
    }

    public void setMasterIP(String masterIP) {
        this.masterIP = masterIP;
    }

    public int getMasterPort() {
        return masterPort;
    }

    public void setMasterPort(int masterPort) {
        this.masterPort = masterPort;
    }


}
