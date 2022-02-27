import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * AnsibleCommandBuilder creates a ansible-playbook command.
 *
 * By default:
 * 1) We don't use any cluster.
 * 2) We use verbose mode level 1 (-v).
 * 3) Playbook directory is $PREFIX/usr/share/ovirt-ansible-roles/playbooks
 * 4) Private key used is $PREFIX/etc/pki/ovirt-engine/keys/engine_id_rsa
 * 5) Log file is $PREFIX/var/log/ovirt-engine/ansible/{prefix}-{timestamp}-{playbook-name}[-{suffix}].log
 * 6) Default inventory file is used.
 */
public class AnsibleCommandBuilder {

    public static final String ANSIBLE_COMMAND = "ansible-runner";
    public static final String ANSIBLE_EXECUTION_METHOD = "run";

    private AnsibleVerbosity verboseLevel;
    private Path privateKey;
    private String cluster;
    private List<String> hostnames;
//    private Map<String, Object> variables;
    private Set<String> variables;
    private String variableFilePath;
    private String limit;
    private Path inventoryFile;
    private String playbook;
    private boolean checkMode;

    // Logging:
    private File logFile;
    private String logFileDirectory;
    private String logFilePrefix;
    private String logFileSuffix;
    private String logFileName;
    /*
     * By default Ansible logs to syslog of the host where the playbook is being executed.
     * If this parameter is set to true the logging will be done to file which you can specify by log* methods.
     * If this parameters is set to false, the logging wil be done to syslog on hosts.
     */
    private boolean enableLogging;

    private Path playbookDir;

    // ENV variables
    private Map<String, String> envVars;

    private UUID uuid;

    public AnsibleCommandBuilder() {
        cluster = "unspecified";
        enableLogging = true;
        envVars = new HashMap<>();
        playbookDir = Paths.get(AnsibleConstants.PROJECT_DIR);
        variables = new HashSet<>();
//        variables = new HashMap<>();
    }

    public AnsibleCommandBuilder verboseLevel(AnsibleVerbosity verboseLevel) {
        this.verboseLevel = verboseLevel;
        return this;
    }

    public AnsibleCommandBuilder inventoryFile(Path inventoryFile) {
        this.inventoryFile = inventoryFile;
        return this;
    }

    public AnsibleCommandBuilder uuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public AnsibleCommandBuilder cluster(String cluster) {
        this.cluster = cluster;
        return this;
    }

    public AnsibleCommandBuilder hosts(VdsStatic... hosts) {
        this.hostnames =  Arrays.stream(hosts)
                .map(h -> formatHostPort(h.getHostName(), h.getSshPort()))
                .collect(Collectors.toList());
        return this;
    }

    public AnsibleCommandBuilder hosts(VDS... hosts) {
        this.hostnames =  Arrays.stream(hosts)
                .map(h -> formatHostPort(h.getHostName(), h.getSshPort()))
                .collect(Collectors.toList());
        return this;
    }

    protected String formatHostPort(String host, int port) {
        return ValidationUtils.isValidIpv6(host)
                ? String.format("[%1$s]:%2$s", host, port)
                : String.format("%1$s:%2$s", host, port);
    }

    public AnsibleCommandBuilder variable(String name, Object value) {
        this.variables.add(name+"="+value+" ");
//        this.variables.put(name, value);
        return this;
    }

    public AnsibleCommandBuilder logFileDirectory(String logFileDirectory) {
        this.logFileDirectory = logFileDirectory;
        return this;
    }

    public AnsibleCommandBuilder logFileName(String logFileName) {
        this.logFileName = logFileName;
        return this;
    }

    public AnsibleCommandBuilder logFilePrefix(String logFilePrefix) {
        this.logFilePrefix = logFilePrefix;
        return this;
    }

    public AnsibleCommandBuilder logFileSuffix(String logFileSuffix) {
        this.logFileSuffix = logFileSuffix;
        return this;
    }

    public AnsibleCommandBuilder playbook(String playbook) {
        this.playbook = AnsibleConstants.HOST_DEPLOY_PLAYBOOK;
        return this;
    }

    public AnsibleCommandBuilder variableFilePath(String variableFilePath) {
        this.variableFilePath = variableFilePath;
        return this;
    }

//    public AnsibleCommandBuilder stdoutCallback(String stdoutCallback) {
//        this.envVars.put(AnsibleEnvironmentConstants.ANSIBLE_STDOUT_CALLBACK, stdoutCallback);
//        return this;
//    }

    public String playbook() {
        return playbook;
    }

    public File logFile() {
        return logFile;
    }

    public AnsibleCommandBuilder enableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
        return this;
    }

    public Path inventoryFile() {
        return inventoryFile;
    }

    public Path playbookDir() {
        return playbookDir;
    }

    public List<String> hostnames() {
        return hostnames;
    }

    public String cluster() {
        return cluster;
    }

    public Path privateKey() {
        return privateKey;
    }

    public String logFileDirectory() {
        return logFileDirectory;
    }

    public String logFileName() {
        return logFileName;
    }

    public String logFilePrefix() {
        return logFilePrefix;
    }

    public String logFileSuffix() {
        return logFileSuffix;
    }

//    public String stdoutCallback() {
//        return envVars.get(AnsibleEnvironmentConstants.ANSIBLE_STDOUT_CALLBACK);
//    }

    public boolean enableLogging() {
        return enableLogging;
    }

//    public Map<String, Object> getVariables() {
//        return variables;
//    }

    public Set<String> getVariables() {
        return variables;
    }

    public void setExtraArgs() {
        //create extraargs file and add vars to file
//        File extraVars = new File(AnsibleConstants.EXTRA_VARS_DIR + "extravars");
//        variables.forEach((key, value) -> {
//            try {
//                FileUtils.writeStringToFile(extraVars, key + ": " + value + "\n", StandardCharsets.UTF_8, true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
    }

    /**
     *
     * The logFile is set up to: (TBD)
     *
     *  /var/log/ovirt-engine/${logDirectory:ansible}/
     *  ${logFilePrefix:ansible}-${timestamp}-${logFileName:playbook}[-${logFileSuffix}].log
     */
    public List<String> build() {
        List<String> ansibleCommand = new ArrayList<>();
        ansibleCommand.add(ANSIBLE_COMMAND);
        ansibleCommand.add(ANSIBLE_EXECUTION_METHOD);
        ansibleCommand.add(AnsibleConstants.PROJECT_DIR);
        ansibleCommand.add("-p");
        ansibleCommand.add(AnsibleConstants.HOST_DEPLOY_PLAYBOOK);
        ansibleCommand.add("--artifact-dir");
        ansibleCommand.add(AnsibleConstants.ARTIFACTS_DIR);
        ansibleCommand.add("--inventory");
        ansibleCommand.add(String.valueOf(this.inventoryFile));
        ansibleCommand.add("-i");
        ansibleCommand.add(String.valueOf(this.uuid));
//        ansibleCommand.add("--debug");
//        ansibleCommand.add("-vvvvv");
//        ansibleCommand.add("--cmdline_args");
//        ansibleCommand.add("-e");
//        ansibleCommand.add("/home/delfassy/projects/AnsibleRunnerImplementation/ansible-runner-service-project/env/host4.yml");

        return ansibleCommand;
    }
}