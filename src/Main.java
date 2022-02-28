import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.List;
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

        AnsibleCommandBuilder builder1 = test("192.168.100.204", "cluster1");
        AnsibleCommandBuilder builder2 = test("192.168.100.254", "cluster2");
        AnsibleCommandBuilder builder3 = test("192.168.100.146", "cluster3");
        AnsibleCommandBuilder builder4 = test("192.168.100.250", "cluster4");
        poolExecutor.getActiveCount();

        while (!uuids.isEmpty()) {
            poolExecutor.execute(new AnsibleProcessor(uuids.poll()));
        }
        poolExecutor.getActiveCount();

        poolExecutor.shutdown();
        poolExecutor.getActiveCount();

//        builder1.cleanup();
//        builder2.cleanup();
//        builder3.cleanup();
//        builder4.cleanup();

    }


    // creating a raw data for vds + executing run command
    public static synchronized AnsibleCommandBuilder test(String ip, String cluster) {
        VDS vds = createVds(ip);
        vds.setClusterName(cluster);
        AnsibleExecutor executor = new AnsibleExecutor(vds);
        AnsibleCommandBuilder commandBuilder  = executor.createCommand();
        List<String> command = commandBuilder.build();
        UUID uuid = executor.getUUID();
        uuids.add(uuid);
        poolExecutor.execute(new PlaybookRunner(timeout, command));
        return commandBuilder;
    }


    public static VDS createVds(String hostname) {
        VDS vds  = new VDS();
        vds.setHostName(hostname);
        vds.setClusterName("clusterTest");
        return vds;
    }
}
