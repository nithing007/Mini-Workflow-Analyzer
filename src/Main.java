import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        WorkflowManager workflowManager = new WorkflowManager();
        WorkflowAnalyzer analyzer = new WorkflowAnalyzer();
        WorkflowExecutor executor = new WorkflowExecutor(workflowManager);

        System.out.println("=================================");
        System.out.println("     MINI WORKFLOW ANALYZER");
        System.out.println("=================================");

        System.out.print("Enter workflow name: ");
        String workflowName = scanner.nextLine().trim();

        if (workflowName.isEmpty()) {
            System.out.println("Workflow name cannot be empty.");
            return;
        }

        System.out.print("Enter workflow description: ");
        String description = scanner.nextLine().trim();

        int workflowId = workflowManager.createWorkflow(workflowName, description);

        if (workflowId == -1) {
            System.out.println("Failed to create workflow.");
            return;
        }

        System.out.println("\nWorkflow created with ID: " + workflowId);

        int taskCount = readNonNegativeInt(scanner, "Enter number of tasks: ");
        List<Integer> taskIds = new ArrayList<>();

        for (int i = 1; i <= taskCount; i++) {
            System.out.print("Enter task " + i + " name: ");
            String taskName = scanner.nextLine().trim();

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
            System.out.println("Task created with ID: " + taskId);
        }

        if (taskIds.isEmpty()) {
            System.out.println("\nNo tasks were added. Workflow cannot be executed.");
            return;
        }

        int dependencyCount = readNonNegativeInt(scanner, "\nEnter number of dependencies: ");
        Set<String> addedDependencies = new HashSet<>();

        for (int i = 1; i <= dependencyCount; i++) {
            System.out.println("\nDependency " + i);

            int taskId = readInt(scanner, "Enter task ID that depends on another task: ");
            int dependsOnTaskId = readInt(scanner, "Enter dependency task ID: ");

            if (!taskIds.contains(taskId) || !taskIds.contains(dependsOnTaskId)) {
                System.out.println("Invalid task ID. Enter IDs from this workflow.");
                i--;
                continue;
            }

            if (taskId == dependsOnTaskId) {
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
                System.out.println("Failed to add dependency.");
                i--;
            } else {
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

        for (int i = 0; i < executionOrder.size(); i++) {
            int taskId = executionOrder.get(i);
            Task task = findTask(tasks, taskId);

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

    private static int readInt(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a valid integer.");
            }
        }
    }

    private static int readNonNegativeInt(Scanner scanner, String message) {
        while (true) {
            int number = readInt(scanner, message);

            if (number >= 0) {
                return number;
            }

            System.out.println("Enter zero or a positive number.");
        }
    }

    private static Task findTask(List<Task> tasks, int taskId) {
        for (Task task : tasks) {
            if (task.getTaskId() == taskId) {
                return task;
            }
        }
        return null;
    }
}