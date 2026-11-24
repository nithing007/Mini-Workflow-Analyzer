import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkflowExecutor {
    private final WorkflowManager workflowManager;

    public WorkflowExecutor(WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public boolean execute(List<Task> tasks, List<Dependency> dependencies, List<Integer> executionOrder) {
        if (workflowManager == null || tasks == null || dependencies == null || executionOrder == null || tasks.isEmpty() || executionOrder.isEmpty()) {
            System.out.println("Invalid workflow data.");
            return false;
        }

        if (executionOrder.size() != tasks.size()) {
            System.out.println("Invalid execution order.");
            return false;
        }

        Map<Integer, Task> taskMap = new HashMap<>(tasks.size());
        Integer workflowId = null;

        for (Task task : tasks) {
            if (task == null || task.getTaskId() <= 0 || task.getWorkflowId() <= 0 || task.getTaskName() == null || task.getTaskName().trim().isEmpty()) {
                System.out.println("Invalid task data.");
                return false;
            }

            if (workflowId == null) {
                workflowId = task.getWorkflowId();
            } else if (workflowId != task.getWorkflowId()) {
                System.out.println("Tasks belong to different workflows.");
                return false;
            }

            if (taskMap.put(task.getTaskId(), task) != null) {
                System.out.println("Duplicate task ID: " + task.getTaskId());
                return false;
            }

            if (!"PENDING".equalsIgnoreCase(task.getStatus())) {
                System.out.println("Task is not in PENDING state: " + task.getTaskName());
                return false;
            }
        }

        Map<Integer, Integer> positions = new HashMap<>(executionOrder.size());

        for (int i = 0; i < executionOrder.size(); i++) {
            Integer taskId = executionOrder.get(i);

            if (taskId == null || taskId <= 0 || !taskMap.containsKey(taskId) || positions.put(taskId, i) != null) {
                System.out.println("Invalid execution order.");
                return false;
            }
        }

        Set<String> dependencyKeys = new HashSet<>();

        for (Dependency dependency : dependencies) {
            if (dependency == null) {
                System.out.println("Invalid dependency data.");
                return false;
            }

            int taskId = dependency.getTaskId();
            int prerequisiteId = dependency.getDependsOnTaskId();

            if (taskId <= 0 || prerequisiteId <= 0 || taskId == prerequisiteId || !positions.containsKey(taskId) || !positions.containsKey(prerequisiteId)) {
                System.out.println("Dependency references invalid task IDs.");
                return false;
            }

            String dependencyKey = taskId + "-" + prerequisiteId;

            if (!dependencyKeys.add(dependencyKey)) {
                System.out.println("Duplicate dependency detected.");
                return false;
            }

            if (positions.get(prerequisiteId) >= positions.get(taskId)) {
                System.out.println("Invalid dependency execution order. Task " + prerequisiteId + " must execute before task " + taskId + ".");
                return false;
            }
        }

        System.out.println("\nExecution started");

        for (int taskId : executionOrder) {
            Task task = taskMap.get(taskId);
            System.out.println("Executing: " + task.getTaskName());

            if (!workflowManager.updateTaskStatus(taskId, "RUNNING")) {
                System.out.println("Failed to start task: " + task.getTaskName());
                return false;
            }

            task.setStatus("RUNNING");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                markFailed(task);
                Thread.currentThread().interrupt();
                System.out.println("Execution interrupted: " + task.getTaskName());
                return false;
            }

            if (!workflowManager.updateTaskStatus(taskId, "COMPLETED")) {
                markFailed(task);
                System.out.println("Failed to complete task: " + task.getTaskName());
                return false;
            }

            task.setStatus("COMPLETED");
            System.out.println("Completed: " + task.getTaskName());
        }

        System.out.println("Execution completed successfully.");
        return true;
    }

    private void markFailed(Task task) {
        if (workflowManager.updateTaskStatus(task.getTaskId(), "FAILED")) {
            task.setStatus("FAILED");
        } else {
            System.out.println("Warning: Failed to update task status in database: " + task.getTaskName());
        }
    }
}
