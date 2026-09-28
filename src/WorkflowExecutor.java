import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkflowExecutor {
    private final WorkflowManager workflowManager;

    public WorkflowExecutor(WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public boolean execute(List<Task> tasks, List<Integer> executionOrder) {
        if (tasks == null || executionOrder == null || workflowManager == null) {
            System.out.println("Invalid workflow data.");
            return false;
        }

        if (tasks.isEmpty() || executionOrder.isEmpty()) {
            System.out.println("Workflow cannot be executed.");
            return false;
        }

        if (executionOrder.size() != tasks.size()) {
            System.out.println("Invalid execution order.");
            return false;
        }

        Set<Integer> taskIds = new HashSet<>();

        for (Task task : tasks) {
            if (task == null || !taskIds.add(task.getTaskId())) {
                System.out.println("Invalid or duplicate task.");
                return false;
            }
        }

        Set<Integer> executedIds = new HashSet<>();

        for (Integer taskId : executionOrder) {
            if (taskId == null || !taskIds.contains(taskId) || !executedIds.add(taskId)) {
                System.out.println("Invalid execution order.");
                return false;
            }
        }

        System.out.println("\nExecution started");

        for (int taskId : executionOrder) {
            Task task = findTask(tasks, taskId);

            System.out.println("Executing: " + task.getTaskName());

            if (!workflowManager.updateTaskStatus(taskId, "RUNNING")) {
                System.out.println("Failed to start task: " + task.getTaskName());
                return false;
            }

            task.setStatus("RUNNING");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                task.setStatus("FAILED");
                workflowManager.updateTaskStatus(taskId, "FAILED");
                Thread.currentThread().interrupt();
                System.out.println("Execution interrupted: " + task.getTaskName());
                return false;
            }

            if (!workflowManager.updateTaskStatus(taskId, "COMPLETED")) {
                task.setStatus("FAILED");
                workflowManager.updateTaskStatus(taskId, "FAILED");
                System.out.println("Failed to complete task: " + task.getTaskName());
                return false;
            }

            task.setStatus("COMPLETED");
            System.out.println("Completed: " + task.getTaskName());
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