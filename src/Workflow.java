public class Workflow {
    private int workflowId;
    private String workflowName;
    private String description;

    public Workflow(int workflowId, String workflowName, String description) {
        this.workflowId = workflowId;
        this.workflowName = workflowName;
        this.description = description;
    }

    public Workflow(String workflowName, String description) {
        this.workflowName = workflowName;
        this.description = description;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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