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
    private static final int MAX_CONSECUTIVE_DB_FAILURES = 3;

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

        if (workflowName == null || workflowName.trim().isEmpty()) {
            System.out.println("Workflow name cannot be empty.");
            return;
        }

        System.out.print("Enter workflow description: ");
        String description = readLine(scanner);

        if (description == null) {
            System.out.println("Input stream exhausted.");
            return;
        }

        int workflowId = workflowManager.createWorkflow(workflowName.trim(), description.trim());

        if (workflowId == -1) {
            System.out.println("Failed to create workflow.");
            return;
        }

        System.out.println("\nWorkflow created with ID: " + workflowId);

        Integer taskCount = readCount(scanner, "Enter number of tasks: ", MAX_TASKS);
        if (taskCount == null) {
            return;
        }

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

            if (taskName.isEmpty()) {
                System.out.println("Task name cannot be empty.");
                i--;
                continue;
            }

            int taskId = workflowManager.createTask(workflowId, taskName);

            if (taskId == -1) {
                System.out.println("Failed to create task.");
                return;
            }

            taskIds.add(taskId);
            validTaskIds.add(taskId);
            System.out.println("Task created with ID: " + taskId);
        }

        if (taskIds.isEmpty()) {
            System.out.println("\nNo tasks were added. Workflow cannot be executed.");
            return;
        }

        Integer dependencyCount = readCount(scanner, "\nEnter number of dependencies: ", MAX_DEPENDENCIES);
        if (dependencyCount == null) {
            return;
        }

        Set<String> addedDependencies = new HashSet<>();
        int consecutiveFailures = 0;

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

            boolean added = workflowManager.addDependency(taskId, dependsOnTaskId);

            if (!added) {
                consecutiveFailures++;
                System.out.println("Failed to add dependency.");
                if (consecutiveFailures >= MAX_CONSECUTIVE_DB_FAILURES) {
                    System.out.println("Multiple consecutive failures encountered. Skipping this dependency.");
                    consecutiveFailures = 0;
                } else {
                    i--;
                }
            } else {
                consecutiveFailures = 0;
                addedDependencies.add(dependencyKey);
                System.out.println("Dependency added successfully.");
            }
        }

        List<Task> tasks = workflowManager.getTasksByWorkflow(workflowId);
        List<Dependency> dependencies = workflowManager.getDependenciesByWorkflow(workflowId);

        if (tasks == null || dependencies == null) {
            System.out.println("Failed to load workflow data.");
            return;
        }

        System.out.println("\n=================================");
        System.out.println("        WORKFLOW ANALYSIS");
        System.out.println("=================================");

        boolean hasCycle = analyzer.hasCycle(tasks, dependencies);

        if (hasCycle) {
            System.out.println("Circular Dependency: YES");
            System.out.println("Workflow cannot be executed.");
            return;
        }

        System.out.println("Circular Dependency: NO");

        List<Integer> executionOrder = analyzer.getExecutionOrder(tasks, dependencies);

        if (executionOrder.isEmpty()) {
            System.out.println("No valid execution order found.");
            return;
        }

        System.out.println("\nExecution Order:");

        Map<Integer, Task> taskMap = new HashMap<>(tasks.size());
        for (Task task : tasks) {
            if (task != null) {
                taskMap.put(task.getTaskId(), task);
            }
        }

        for (int i = 0; i < executionOrder.size(); i++) {
            int taskId = executionOrder.get(i);
            Task task = taskMap.get(taskId);

            if (task == null) {
                System.out.println("Unable to find task with ID: " + taskId);
                return;
            }

            System.out.println((i + 1) + ". " + task.getTaskName());
        }

        System.out.println("\n=================================");
        System.out.println("        WORKFLOW EXECUTION");
        System.out.println("=================================");

        executor.execute(tasks, executionOrder);
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

            if (number < 0) {
                System.out.println("Enter zero or a positive number.");
            } else {
                System.out.println("Value is too large. Please enter a number between 0 and " + max + ".");
            }
        }
    }
}