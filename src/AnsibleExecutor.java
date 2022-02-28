import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AnsibleExecutor implements Runnable {
    private VDS vds;
    private UUID uuid;
    private int timeout;

    public AnsibleExecutor(VDS vds, int timeout, UUID uuid) {
        this.vds = vds;
        this.uuid = uuid;
        this.timeout = timeout;
    }

    private AnsibleCommandBuilder createCommand() {
        AnsibleCommandBuilder command = new AnsibleCommandBuilder(vds, uuid)
                .hosts(vds)
                .variable("host_deploy_cluster_name", vds.getClusterName())
                .variable("ansible_port", vds.getSshPort())
                .uuid(uuid)
                .logFileDirectory(AnsibleConstants.HOST_DEPLOY_LOG_DIRECTORY)
                .logFilePrefix("ovirt-host-deploy-ansible")
                .logFileName(vds.getHostName())
                .playbook(AnsibleConstants.HOST_DEPLOY_PLAYBOOK);

        try {
            createInventoryFile(command);
        } catch(Exception e) {
            e.printStackTrace();
        }

        List<String> ansibleCommand = command.build();
        System.out.println("ansible command: " + ansibleCommand);

        return command;
    }


    /**
     * Create a temporary inventory file if user didn't specify it.
     */
    private void createInventoryFile(AnsibleCommandBuilder command) throws IOException {
        Path inventoryFile;
        if (command.inventoryFile() == null) {
//             If hostnames are empty we just don't pass any inventory file:
            if (CollectionUtils.isNotEmpty(command.hostnames())) {
                try {
                    inventoryFile = Files.createTempFile("ansible-inventory", "");
                    Files.write(inventoryFile, StringUtils.join(command.hostnames(), System.lineSeparator()).getBytes());
                    command.inventoryFile(inventoryFile);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    private void runPlaybook(List<String> command) {
        Process ansibleProcess = null;
        try {
            ProcessBuilder ansibleProcessBuilder = new ProcessBuilder(command);
            ansibleProcess = ansibleProcessBuilder.start();
            if (!ansibleProcess.waitFor(timeout, TimeUnit.MINUTES)) {
                throw new Exception("Timeout occurred while executing Ansible playbook.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("thread: " + Thread.currentThread().getName() + " ansible process exit value: " + ansibleProcess.exitValue());
    }

    @Override
    public void run() {
        AnsibleCommandBuilder commandBuilder = createCommand();
        List<String> command = commandBuilder.build();
        runPlaybook(command);
        commandBuilder.cleanup();
    }
}
