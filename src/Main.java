import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


//create a temp inventory file for each play execution.
public class Main {
    private static Queue<UUID> uuids = new LinkedList<>();
    private static ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(6);
    //retrieve from engine consts
    private static final int timeout = 20;

    public static void main(String[] args) throws Exception {
        //each call for playbook- generate random uuid, add it to list.
        //run playbook, and ansibleprocessor runs while there are items on the list.
        //clean up created temp files!

        test("192.168.100.204", "cluster1");
        test("192.168.100.254", "cluster2");
        test("192.168.100.146", "cluster3");
        test("192.168.100.250", "cluster4");

        while (!uuids.isEmpty()) {
            poolExecutor.execute(new AnsibleProcessor(uuids.poll()));
        }
        poolExecutor.shutdown();
    }

    private static void test(String ip, String cluster) {
        VDS vds = createVds(ip);
        vds.setClusterName(cluster);
        final Object executorLock = new Object();
        synchronized (executorLock) {
            UUID uuid = UUID.randomUUID();
            poolExecutor.execute(new AnsibleExecutor(vds, timeout, uuid));
            uuids.add(uuid);
        }
    }

    public static VDS createVds(String hostname) {
        VDS vds  = new VDS();
        vds.setHostName(hostname);
        vds.setClusterName("clusterTest");
        return vds;
    }
}
