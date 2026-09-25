import java.util.List;
public class WorkflowExecutor {
    private final WorkflowManager workflowManager;

    public WorkflowExecutor(WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public boolean execute(
            List<Task> tasks,
            List<Integer> executionOrder) {

        if (executionOrder == null || executionOrder.isEmpty()) {
            System.out.println("Workflow cannot be executed.");
            return false;
        }

        System.out.println("\nExecution started");

        for (int taskId : executionOrder) {

            Task task = findTask(tasks, taskId);

            if (task == null) {
                System.out.println("Task not found: " + taskId);
                return false;
            }

            System.out.println("Executing: " + task.getTaskName());

            boolean updated = workflowManager.updateTaskStatus(
                    taskId,
                    "RUNNING"
            );

            if (!updated) {
                System.out.println(
                        "Failed to start task: " + task.getTaskName()
                );
                return false;
            }

            task.setStatus("RUNNING");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                workflowManager.updateTaskStatus(taskId, "FAILED");
                return false;
            }

            updated = workflowManager.updateTaskStatus(
                    taskId,
                    "COMPLETED"
            );

            if (!updated) {
                System.out.println(
                        "Failed to complete task: " + task.getTaskName()
                );
                return false;
            }

            task.setStatus("COMPLETED");

            System.out.println(
                    "Completed: " + task.getTaskName()
            );
        }
        System.out.println("Execution completed successfully.");
        return true;
    }

    private Task findTask(List<Task> tasks, int taskId) {

        for (Task task : tasks) {
            if (task.getTaskId() == taskId) {
                return task;
            }
        }
        return null;
    }
}