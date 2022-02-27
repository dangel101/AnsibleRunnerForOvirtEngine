public class VdsStatic{
    public static final int DEFAULT_SSH_PORT = 22;
    private static final String DEFAULT_SSH_USERNAME = "root";

    private Guid id;
    private String name;
    private String hostName;
    private int port;
    private Guid clusterId;
    private int sshPort;
    private String sshUsername;

    public VdsStatic() {
        sshPort = DEFAULT_SSH_PORT;
        sshUsername = DEFAULT_SSH_USERNAME;
        name = "";
    }

    public VdsStatic(String hostName, int port, int sshPort, Guid clusterId, Guid vdsId, String vdsName) {
        this();
        this.hostName = hostName;
        this.port = port;
        this.name = vdsName;
        this.port = port;
        if (sshPort > 0) {
            this.sshPort = sshPort;
        }
        this.clusterId = clusterId;
        this.id = vdsId;

    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String value) {
        hostName = value;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int value) {
        port = value;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int value) {
        sshPort = value;
    }

    public String getSshUsername() {
        return sshUsername;
    }

    public void setSshUsername(String value) {
        sshUsername = value;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid value) {
        clusterId = value;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }
}