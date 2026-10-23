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
        this(0, taskId, dependsOnTaskId);
    }

    public int getDependencyId() {
        return dependencyId;
    }

    public void setDependencyId(int dependencyId) {
        if (dependencyId > 0) {
            this.dependencyId = dependencyId;
        }
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        if (taskId > 0) {
            this.taskId = taskId;
        }
    }

    public int getDependsOnTaskId() {
        return dependsOnTaskId;
    }

    public void setDependsOnTaskId(int dependsOnTaskId) {
        if (dependsOnTaskId > 0) {
            this.dependsOnTaskId = dependsOnTaskId;
        }
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