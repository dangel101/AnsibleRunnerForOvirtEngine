import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Test {
    private String ip;
    private String cluster;
    private UUID uuid;
    private static ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(6);
    //retrieve from engine consts
    private static final int timeout = 20;

    public Test(String ip, String cluster) {
        this.ip = ip;
        this.cluster = cluster;
        runTest();
    }

    public List<String> runTest() {
        List<UUID> uuids = new ArrayList<>();
        VDS vds = createVds(ip);
        vds.setClusterName(cluster);
        AnsibleExecutor executor = new AnsibleExecutor(vds);
        List<String> command  = executor.runCommand();
        uuid = executor.getUUID();
        uuids.add(uuid);
        poolExecutor.execute(new PlaybookRunner(timeout, command));
//        try {
//            Files.delete(Paths.get(AnsibleConstants.EXTRA_VARS_DIR+"extravars"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return command;
    }

    public VDS createVds(String hostname) {
        VDS vds  = new VDS();
        vds.setHostName(hostname);
        vds.setClusterName("clusterTest");
        return vds;
    }

    public UUID getUUID() {
        return uuid;
    }
}
