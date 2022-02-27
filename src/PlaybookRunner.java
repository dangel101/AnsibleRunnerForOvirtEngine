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
        Process ansibleProcess = null;

        try {
            ProcessBuilder ansibleProcessBuilder = new ProcessBuilder(command);
            ansibleProcess = ansibleProcessBuilder.start();
//            System.out.println("started running: " + Thread.currentThread().getName());
            if (!ansibleProcess.waitFor(timeout, TimeUnit.MINUTES)) {
                throw new Exception("Timeout occurred while executing Ansible playbook.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("thread: " + Thread.currentThread().getName() + " ansible process exit value: " + ansibleProcess.exitValue());
    }
}
