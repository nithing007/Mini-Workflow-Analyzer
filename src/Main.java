import java.util.List;
import java.util.Scanner;

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
        String workflowName = scanner.nextLine();

        System.out.print("Enter workflow description: ");
        String description = scanner.nextLine();

        int workflowId = workflowManager.createWorkflow(
                workflowName,
                description
        );

        if (workflowId == -1) {
            System.out.println("Failed to create workflow.");
            scanner.close();
            return;
        }

        System.out.println("\nWorkflow created with ID: " + workflowId);

        System.out.print("Enter number of tasks: ");
        int taskCount = scanner.nextInt();
        scanner.nextLine();

        List<Integer> taskIds = new java.util.ArrayList<>();

        for (int i = 1; i <= taskCount; i++) {

            System.out.print("Enter task " + i + " name: ");
            String taskName = scanner.nextLine();

            int taskId = workflowManager.createTask(
                    workflowId,
                    taskName
            );

            if (taskId == -1) {
                System.out.println("Failed to create task.");
                scanner.close();
                return;
            }

            taskIds.add(taskId);

            System.out.println(
                    "Task created with ID: " + taskId
            );
        }

        System.out.print(
                "\nEnter number of dependencies: "
        );

        int dependencyCount = scanner.nextInt();

        for (int i = 1; i <= dependencyCount; i++) {

            System.out.println(
                    "\nDependency " + i
            );

            System.out.print(
                    "Enter task ID that depends on another task: "
            );

            int taskId = scanner.nextInt();

            System.out.print(
                    "Enter dependency task ID: "
            );

            int dependsOnTaskId = scanner.nextInt();

            boolean added = workflowManager.addDependency(
                    taskId,
                    dependsOnTaskId
            );

            if (!added) {
                System.out.println(
                        "Failed to add dependency."
                );
            } else {
                System.out.println(
                        "Dependency added successfully."
                );
            }
        }

        List<Task> tasks =
                workflowManager.getTasksByWorkflow(workflowId);

        List<Dependency> dependencies =
                workflowManager.getDependenciesByWorkflow(workflowId);

        System.out.println("\n=================================");
        System.out.println("        WORKFLOW ANALYSIS");
        System.out.println("=================================");

        boolean hasCycle =
                analyzer.hasCycle(tasks, dependencies);

        if (hasCycle) {

            System.out.println(
                    "Circular Dependency: YES"
            );

            System.out.println(
                    "Workflow cannot be executed."
            );

        } 
        else {

            System.out.println(
                    "Circular Dependency: NO"
            );

            List<Integer> executionOrder =
                    analyzer.getExecutionOrder(
                            tasks,
                            dependencies
                    );

            System.out.println(
                    "\nExecution Order:"
            );

            for (int i = 0; i < executionOrder.size(); i++) {

                int taskId = executionOrder.get(i);

                Task task = findTask(tasks, taskId);

                System.out.println(
                        (i + 1) + ". " +
                        task.getTaskName()
                );
            }

            System.out.println(
                    "\n================================="
            );

            System.out.println(
                    "        WORKFLOW EXECUTION"
            );

            System.out.println(
                    "================================="
            );

            executor.execute(
                    tasks,
                    executionOrder
            );
        }
        scanner.close();
    }

    private static Task findTask(
            List<Task> tasks,
            int taskId) {

        for (Task task : tasks) {

            if (task.getTaskId() == taskId) {
                return task;
            }
        }
        return null;
    }
}