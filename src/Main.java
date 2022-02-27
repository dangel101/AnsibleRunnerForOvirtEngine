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

        test("192.168.100.204", "cluster1");
        test("192.168.100.254", "cluster2");
        test("192.168.100.146", "cluster3");
        test("192.168.100.250", "cluster4");


        final Object executeLock = new Object();
        while (!uuids.isEmpty()) {
            poolExecutor.execute(new AnsibleProcessor(uuids.poll()));
        }
//        while (!uuids.isEmpty()) {
//            synchronized (executeLock)  {
//                UUID currentUuid = uuids.poll();
//                System.out.println("working on uuid: " + currentUuid);
//                poolExecutor.execute(new AnsibleProcessor(currentUuid));
//            }
//        }
        poolExecutor.shutdown();
    }


    // creating a raw data for vds + executing run command
    public static synchronized void test(String ip, String cluster) {
        VDS vds = createVds(ip);
        vds.setClusterName(cluster);
        AnsibleExecutor executor = new AnsibleExecutor(vds);
        AnsibleCommandBuilder commandBuilder  = executor.createCommand();
        List<String> command = commandBuilder.build();
//        List<String> command  = executor.createCommand();
        UUID uuid = executor.getUUID();

        // remove temp extra vars file!
        uuids.add(uuid);
        poolExecutor.execute(new PlaybookRunner(timeout, command));
//        return vds.getHostName();
//        try {
//            Files.delete(Paths.get(AnsibleConstants.EXTRA_VARS_DIR+"extravars"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static VDS createVds(String hostname) {
        VDS vds  = new VDS();
        vds.setHostName(hostname);
        vds.setClusterName("clusterTest");
        return vds;
    }
}
