public class VDS {
    private VdsStatic vdsStatic;
    private VdsDynamic vdsDynamic;
    private String clusterName;

    public VDS() {
        vdsStatic = new VdsStatic();
        vdsDynamic = new VdsDynamic();
    }

    public Guid getClusterId() {
        return vdsStatic.getClusterId();
    }

    public void setClusterId(Guid value) {
        vdsStatic.setClusterId(value);
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String value) {
        clusterName = value;
    }

    public Guid getId() {
        return vdsStatic.getId();
    }

    public void setId(Guid value) {
        vdsStatic.setId(value);
        vdsDynamic.setId(value);
    }

    public String getName() {
        return vdsStatic.getName();
    }

    public void setVdsName(String value) {
        vdsStatic.setName(value);
    }

    public String getHostName() {
        return vdsStatic.getHostName();
    }

    public void setHostName(String value) {
        vdsStatic.setHostName(value);
    }

    public int getPort() {
        return vdsStatic.getPort();
    }

    public void setPort(int value) {
        vdsStatic.setPort(value);
    }

    public int getSshPort() {
        return vdsStatic.getSshPort();
    }

    public void setSshPort(int value) {
        vdsStatic.setSshPort(value);
    }

}