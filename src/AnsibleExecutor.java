import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class AnsibleExecutor {
    private Path inventoryFile = null;
    private VDS vds;
    private UUID uuid;


    public AnsibleExecutor(VDS vds) {
        this.vds = vds;
        this.uuid = UUID.randomUUID();
    }

//    public List<String> runCommand() {
//        AnsibleCommandBuilder command = createCommand(vds, uuid);
//        try {
//            inventoryFile = createInventoryFile(command);
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//        List<String> ansibleCommand = command.build();
//        System.out.println("ansible command: " + ansibleCommand);
//        return ansibleCommand;
//    }

//    public AnsibleCommandBuilder createCommand(VDS vds, UUID uuid) {
    public AnsibleCommandBuilder createCommand() {
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
            inventoryFile = createInventoryFile(command);
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
    private Path createInventoryFile(AnsibleCommandBuilder command) throws IOException {
        Path inventoryFile = null;
        if (command.inventoryFile() == null) {
            // If hostnames are empty we just don't pass any inventory file:
            if (CollectionUtils.isNotEmpty(command.hostnames())) {
//                System.out.println("Inventory hosts: " + command.hostnames());
                inventoryFile = Files.createTempFile("ansible-inventory", "");
                Files.write(inventoryFile, StringUtils.join(command.hostnames(), System.lineSeparator()).getBytes());
                command.inventoryFile(inventoryFile);
            }
        }
        return inventoryFile;
    }

    public UUID getUUID() {
        return uuid;
    }
}
