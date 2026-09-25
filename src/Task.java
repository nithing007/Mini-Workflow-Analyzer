public class Task {
    private int taskId;
    private int workflowId;
    private String taskName;
    private String status;

    public Task(int taskId, int workflowId, String taskName, String status) {
        this.taskId = taskId;
        this.workflowId = workflowId;
        this.taskName = taskName;
        this.status = status;
    }

    public Task(int workflowId, String taskName) {
        this.workflowId = workflowId;
        this.taskName = taskName;
        this.status = "PENDING";
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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