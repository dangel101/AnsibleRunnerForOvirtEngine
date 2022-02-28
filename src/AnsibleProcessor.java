import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class AnsibleProcessor implements Runnable {
    private AnsibleRunnerLogger runnerLogger;
    private String lastEvent = "";
    private final Path auditLog = Paths.get(AnsibleConstants.PROJECT_DIR + "project/" + "auditLogMessages");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    private final StringBuilder sb = new StringBuilder("\n" + ZonedDateTime.now().format(formatter) + "\n");
    private AnsibleRunnerLogger hostDeployLog;
    private String job_events;
    private final int timeout = 10;
    private static final int POLL_INTERVAL = 3000;


    public AnsibleProcessor(UUID uuid) throws IOException {
        this.runnerLogger = new AnsibleRunnerLogger(String.format("%1$s/artifacts/%2$s/host_deploy.log", AnsibleConstants.PROJECT_DIR, uuid));
        Files.writeString(auditLog, sb, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        this.hostDeployLog = new AnsibleRunnerLogger(String.format("%1$s/host-deploy-logs/host-deploy-%2$s-%3$s.log", AnsibleConstants.PROJECT_DIR, uuid, sb));
        this.job_events = String.format("%1$s/artifacts/%2$s/job_events/", AnsibleConstants.PROJECT_DIR, uuid);
    }

    @Override
    public void run() {
        try {
            artifactHandler(0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public Boolean playHasEnded() {
        File lastEvent = new File(this.job_events + this.lastEvent);
        String res = "";
        try {
            res = Files.readString(lastEvent.toPath());
        } catch (IOException e) {
            return false;
        }
        return res.contains("playbook_on_stats");
    }

    public void artifactHandler(int lastEventID) throws IOException, InterruptedException, TimeoutException {
        int iteration = 0;
        // retrieve timeout from engine constants.
        while (!playHasEnded()) {
            if (iteration > timeout * 60) {
                throw new TimeoutException(
                        "Play execution has reached timeout");
            }
            lastEventID = processEvents(lastEventID);
            iteration += POLL_INTERVAL / 1000;
        }
    }

    public List<String> getSortedEvents(int lastEventId) throws InterruptedException, IOException {
        Boolean artifactsIsPopulated = false;
        List<String> sortedEvents = new ArrayList<>();

        while (!artifactsIsPopulated) {
            Thread.sleep(1500);

            //ignoring incompleted json files, add to list only events that haven't been handles yet.
            if (Files.exists(Paths.get(this.job_events))) {
                sortedEvents = Stream.of(new File(this.job_events).listFiles())
                        .map(File::getName)
                        .distinct()
                        .filter(item -> !item.contains("partial"))
                        .filter(item -> (Integer.valueOf(item.split("-")[0])) > lastEventId)
                        .collect(Collectors.toList());
                artifactsIsPopulated = true;
            }
        }
        return sortedEvents;
    }

    private Boolean jsonIsValid(String content) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(content);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public int processEvents(int lastEventId) throws InterruptedException, IOException {
        List<String> sortedEvents = getSortedEvents(lastEventId);

        for (String event : sortedEvents) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                File from = new File(this.job_events + event);
                if (!jsonIsValid(Files.readString(Paths.get(this.job_events + event)))) {
                    return (Integer.valueOf(event.split("-")[0]) - 1);
                }
                JsonNode currentNode = mapper.readTree(from);
                String stdout = RunnerJsonNode.getStdout(currentNode);

                // might need special attention
                if (RunnerJsonNode.isEventVerbose(currentNode)) {
                    runnerLogger.log(stdout);
                    hostDeployLog.log(stdout);
                }

                // want to log only these kind of events:
                if (RunnerJsonNode.isEventStart(currentNode) || RunnerJsonNode.isEventOk(currentNode)
                        || RunnerJsonNode.playbookStats(currentNode) || RunnerJsonNode.isEventFailed(currentNode)) {

                    String taskName = "";
                    JsonNode eventNode = currentNode.get("event_data");

                    JsonNode taskNode = eventNode.get("task");
                    if (taskNode != null) {
                        taskName = taskNode.textValue();
                    }

                    if (RunnerJsonNode.isEventStart(currentNode) || RunnerJsonNode.playbookStats(currentNode)) {
                        runnerLogger.log(stdout);
                        hostDeployLog.log(stdout);
                    }

                    String action = "";
                    JsonNode eventAction = eventNode.get("task_action");
                    if (eventAction != null) {
                        action = eventAction.asText();
                    }

                    if (RunnerJsonNode.isEventOk(currentNode)) {
                        runnerLogger.log(currentNode);
                        hostDeployLog.log(currentNode);

                        String taskText = action.equals("debug")
                                ? RunnerJsonNode.formatDebugMessage(taskName, stdout)
                                : taskName;
                        Files.writeString(auditLog,
                                new StringBuilder("\n" + ZonedDateTime.now().format(formatter)) + " " + taskText + "\n",
                                StandardOpenOption.CREATE,
                                StandardOpenOption.APPEND);
                        System.out.println("Thread: " + Thread.currentThread().getName() + " Processing EVENT: " + taskName);
                    } else if (RunnerJsonNode.isEventFailed(currentNode)
                            || RunnerJsonNode.isEventUnreachable(currentNode)) {
                        runnerLogger.log(currentNode);
                        hostDeployLog.log(currentNode);
                        if (!RunnerJsonNode.ignore(currentNode)) {
                            throw new AnsibleRunnerCallException(
                                    String.format(
                                            "Task %1$s failed to execute. Please check logs for more details: %2$s",
                                            taskName,
                                            this.runnerLogger.getLogFile()));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.lastEvent = event;
        }
        return this.lastEvent.isEmpty() ? lastEventId : Integer.valueOf(this.lastEvent.split("-")[0]);
    }
}
