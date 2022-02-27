import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlaybookRunner implements Runnable {
    private int timeout;
    private List<String> command;

    public PlaybookRunner(int timeout, List<String> command) {
        this.timeout = timeout;
        this.command = command;
    }

    public void run() {
        runPlaybook();
    }

    private void runPlaybook() {
        File vars = new File("/home/delfassy/projects/AnsibleRunnerForOvirtEngine/ansible-runner-service-project/env/extravars");
        try {
            vars.createNewFile();
            Files.writeString(vars.toPath(), "host_deploy_cluster_name: clusterTest1", StandardOpenOption.CREATE);
        } catch(Exception e) {
            e.printStackTrace();
        }
        Process ansibleProcess = null;
//        System.out.println("run playbook with command: " + command);

        try {
            ProcessBuilder ansibleProcessBuilder = new ProcessBuilder(command);
            ansibleProcess = ansibleProcessBuilder.start();
            System.out.println("started running: " + Thread.currentThread().getName());
            if (!ansibleProcess.waitFor(timeout, TimeUnit.MINUTES)) {
                throw new Exception("Timeout occurred while executing Ansible playbook.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("thread: " + Thread.currentThread().getName() + "ansible process: " + ansibleProcess.exitValue());
        vars.delete();
    }
}
