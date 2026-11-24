import java.util.Set;
public class Task {
    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "RUNNING", "COMPLETED", "FAILED");

    private int taskId;
    private int workflowId;
    private String taskName;
    private String status;

    public Task(int taskId, int workflowId, String taskName, String status) {
        this.taskId = taskId;
        this.workflowId = workflowId;
        this.taskName = (taskName != null && !taskName.trim().isEmpty()) ? taskName.trim() : "Unnamed Task";
        this.status = (status != null && VALID_STATUSES.contains(status.trim().toUpperCase()))
                ? status.trim().toUpperCase() : "PENDING";
    }

    public Task(int workflowId, String taskName) {
        this(0, workflowId, taskName, "PENDING");
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        if (taskId > 0) {
            this.taskId = taskId;
        }
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        if (workflowId > 0) {
            this.workflowId = workflowId;
        }
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        if (taskName != null && !taskName.trim().isEmpty()) {
            this.taskName = taskName.trim();
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status != null && VALID_STATUSES.contains(status.trim().toUpperCase())) {
            this.status = status.trim().toUpperCase();
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", workflowId=" + workflowId +
                ", taskName='" + taskName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}