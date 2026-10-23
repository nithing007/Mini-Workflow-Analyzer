public class Workflow {
    private int workflowId;
    private String workflowName;
    private String description;

    public Workflow(int workflowId, String workflowName, String description) {
        this.workflowId = workflowId;
        this.workflowName = (workflowName != null && !workflowName.trim().isEmpty())
                ? workflowName.trim() : "Unnamed Workflow";
        this.description = description != null ? description.trim() : null;
    }

    public Workflow(String workflowName, String description) {
        this(0, workflowName, description);
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        if (workflowId > 0) {
            this.workflowId = workflowId;
        }
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        if (workflowName != null && !workflowName.trim().isEmpty()) {
            this.workflowName = workflowName.trim();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }

    @Override
    public String toString() {
        return "Workflow{" +
                "workflowId=" + workflowId +
                ", workflowName='" + workflowName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}