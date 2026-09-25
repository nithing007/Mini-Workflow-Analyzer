public class Dependency {
    private int dependencyId;
    private int taskId;
    private int dependsOnTaskId;

    public Dependency(int dependencyId, int taskId, int dependsOnTaskId) {
        this.dependencyId = dependencyId;
        this.taskId = taskId;
        this.dependsOnTaskId = dependsOnTaskId;
    }

    public Dependency(int taskId, int dependsOnTaskId) {
        this.taskId = taskId;
        this.dependsOnTaskId = dependsOnTaskId;
    }

    public int getDependencyId() {
        return dependencyId;
    }

    public void setDependencyId(int dependencyId) {
        this.dependencyId = dependencyId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getDependsOnTaskId() {
        return dependsOnTaskId;
    }

    public void setDependsOnTaskId(int dependsOnTaskId) {
        this.dependsOnTaskId = dependsOnTaskId;
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "dependencyId=" + dependencyId +
                ", taskId=" + taskId +
                ", dependsOnTaskId=" + dependsOnTaskId +
                '}';
    }
}