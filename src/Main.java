import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

public class Main {
    private static final int MAX_TASKS = 10000;
    private static final int MAX_DEPENDENCIES = 50000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        WorkflowManager workflowManager = new WorkflowManager();
        WorkflowAnalyzer analyzer = new WorkflowAnalyzer();
        WorkflowExecutor executor = new WorkflowExecutor(workflowManager);

        System.out.println("=================================");
        System.out.println("     MINI WORKFLOW ANALYZER");
        System.out.println("=================================");

        System.out.print("Enter workflow name: ");
        String workflowName = readLine(scanner);
        if (workflowName == null || workflowName.trim().isEmpty() || workflowName.trim().length() > 100) {
            System.out.println("Workflow name must contain 1-100 characters.");
            return;
        }

        System.out.print("Enter workflow description: ");
        String description = readLine(scanner);
        if (description == null) {
            System.out.println("Input stream exhausted.");
            return;
        }
        if (description.trim().length() > 255) {
            System.out.println("Description cannot exceed 255 characters.");
            return;
        }

        Integer taskCount = readCount(scanner, "Enter number of tasks: ", MAX_TASKS);
        if (taskCount == null) {
            return;
        }
        if (taskCount == 0) {
            System.out.println("At least one task is required.");
            return;
        }

        int workflowId = workflowManager.createWorkflow(workflowName.trim(), description.trim());
        if (workflowId <= 0) {
            System.out.println("Failed to create workflow.");
            return;
        }

        System.out.println("\nWorkflow created with ID: " + workflowId);

        List<Integer> taskIds = new ArrayList<>();
        Set<Integer> validTaskIds = new HashSet<>();

        for (int i = 1; i <= taskCount; i++) {
            System.out.print("Enter task " + i + " name: ");
            String taskName = readLine(scanner);
            if (taskName == null) {
                System.out.println("\nInput stream exhausted.");
                return;
            }

            taskName = taskName.trim();
            if (taskName.isEmpty() || taskName.length() > 100) {
                System.out.println("Task name must contain 1-100 characters.");
                i--;
                continue;
            }

            int taskId = workflowManager.createTask(workflowId, taskName);
            if (taskId <= 0) {
                System.out.println("Failed to create task. Workflow setup aborted.");
                return;
            }

            taskIds.add(taskId);
            validTaskIds.add(taskId);
            System.out.println("Task created with ID: " + taskId);
        }

        Integer dependencyCount = readCount(scanner, "\nEnter number of dependencies: ", MAX_DEPENDENCIES);
        if (dependencyCount == null) {
            return;
        }
        if (dependencyCount > (long) taskCount * (taskCount - 1)) {
            System.out.println("Dependency count exceeds the maximum possible unique dependencies.");
            return;
        }

        Set<String> addedDependencies = new HashSet<>();

        for (int i = 1; i <= dependencyCount; i++) {
            System.out.println("\nDependency " + i);

            Integer taskId = readInt(scanner, "Enter task ID that depends on another task: ");
            if (taskId == null) {
                return;
            }

            Integer dependsOnTaskId = readInt(scanner, "Enter dependency task ID: ");
            if (dependsOnTaskId == null) {
                return;
            }

            if (!validTaskIds.contains(taskId) || !validTaskIds.contains(dependsOnTaskId)) {
                System.out.println("Invalid task ID. Enter IDs from this workflow.");
                i--;
                continue;
            }

            if (taskId.equals(dependsOnTaskId)) {
                System.out.println("A task cannot depend on itself.");
                i--;
                continue;
            }

            String dependencyKey = taskId + "-" + dependsOnTaskId;
            if (addedDependencies.contains(dependencyKey)) {
                System.out.println("This dependency already exists.");
                i--;
                continue;
            }

            if (!workflowManager.addDependency(taskId, dependsOnTaskId)) {
                System.out.println("Failed to add dependency. Workflow setup aborted.");
                return;
            }

            addedDependencies.add(dependencyKey);
            System.out.println("Dependency added successfully.");
        }

        List<Task> tasks = workflowManager.getTasksByWorkflow(workflowId);
        List<Dependency> dependencies = workflowManager.getDependenciesByWorkflow(workflowId);

        if (tasks == null || dependencies == null) {
            System.out.println("Failed to load workflow data.");
            return;
        }

        if (tasks.isEmpty() || tasks.size() != taskCount) {
            System.out.println("Workflow contains missing or invalid task data.");
            return;
        }

        if (!validateWorkflowData(tasks, dependencies)) {
            System.out.println("Invalid workflow data detected.");
            return;
        }

        System.out.println("\n=================================");
        System.out.println("        WORKFLOW ANALYSIS");
        System.out.println("=================================");

        boolean hasCycle = analyzer.hasCycle(tasks, dependencies);
        List<Integer> executionOrder = analyzer.getExecutionOrder(tasks, dependencies);

        if (hasCycle) {
            System.out.println("Circular Dependency: YES");
            System.out.println("Workflow cannot be executed.");
            return;
        }

        System.out.println("Circular Dependency: NO");

        if (executionOrder.size() != tasks.size()) {
            System.out.println("No valid execution order found.");
            return;
        }

        Map<Integer, Task> taskMap = new HashMap<>(tasks.size());
        for (Task task : tasks) {
            taskMap.put(task.getTaskId(), task);
        }

        System.out.println("\nExecution Order:");
        for (int i = 0; i < executionOrder.size(); i++) {
            Task task = taskMap.get(executionOrder.get(i));
            if (task == null) {
                System.out.println("Invalid execution order.");
                return;
            }
            System.out.println((i + 1) + ". " + task.getTaskName());
        }

        System.out.println("\n=================================");
        System.out.println("        WORKFLOW EXECUTION");
        System.out.println("=================================");

        executor.execute(tasks, dependencies, executionOrder);
    }

    private static boolean validateWorkflowData(List<Task> tasks, List<Dependency> dependencies) {
        if (tasks == null || dependencies == null) {
            return false;
        }

        Set<Integer> taskIds = new HashSet<>();
        for (Task task : tasks) {
            if (task == null || task.getTaskId() <= 0 || !taskIds.add(task.getTaskId())) {
                return false;
            }
        }

        Set<String> dependencyKeys = new HashSet<>();
        for (Dependency dependency : dependencies) {
            if (dependency == null) {
                return false;
            }

            int taskId = dependency.getTaskId();
            int prerequisiteId = dependency.getDependsOnTaskId();

            if (taskId <= 0 || prerequisiteId <= 0 || taskId == prerequisiteId ||
                    !taskIds.contains(taskId) || !taskIds.contains(prerequisiteId)) {
                return false;
            }

            String key = taskId + "-" + prerequisiteId;
            if (!dependencyKeys.add(key)) {
                return false;
            }
        }
        return true;
    }

    private static String readLine(Scanner scanner) {
        try {
            if (!scanner.hasNextLine()) {
                return null;
            }
            return scanner.nextLine();
        } catch (IllegalStateException | NoSuchElementException e) {
            return null;
        }
    }

    private static Integer readInt(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String input = readLine(scanner);
            if (input == null) {
                System.out.println("\nInput stream exhausted.");
                return null;
            }

            input = input.trim();
            if (input.isEmpty()) {
                System.out.println("Invalid input. Enter a valid integer.");
                continue;
            }

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a valid integer.");
            }
        }
    }

    private static Integer readCount(Scanner scanner, String message, int max) {
        while (true) {
            Integer number = readInt(scanner, message);
            if (number == null) {
                return null;
            }

            if (number >= 0 && number <= max) {
                return number;
            }

            System.out.println(number < 0
                    ? "Enter zero or a positive number."
                    : "Value is too large. Enter a number between 0 and " + max + ".");
        }
    }
}