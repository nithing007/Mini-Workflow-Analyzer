import java.util.*;

public class WorkflowAnalyzer {
    public boolean hasCycle(List<Task> tasks, List<Dependency> dependencies) {
        Map<Integer, List<Integer>> graph = buildGraph(tasks, dependencies);
        Map<Integer, Integer> state = new HashMap<>();

        for (Task task : tasks) {
            state.put(task.getTaskId(), 0);
        }

        for (Task task : tasks) {
            if (state.get(task.getTaskId()) == 0) {
                if (detectCycle(task.getTaskId(), graph, state)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Integer> getExecutionOrder(List<Task> tasks, List<Dependency> dependencies) {
        Map<Integer, List<Integer>> graph = buildGraph(tasks, dependencies);
        Map<Integer, Integer> indegree = new HashMap<>();

        for (Task task : tasks) {
            indegree.put(task.getTaskId(), 0);
        }

        for (Dependency dependency : dependencies) {
            int taskId = dependency.getTaskId();
            int dependsOnTaskId = dependency.getDependsOnTaskId();

            if (!indegree.containsKey(taskId) || !indegree.containsKey(dependsOnTaskId)) {
                return new ArrayList<>();
            }

            indegree.put(taskId, indegree.get(taskId) + 1);
        }

        Queue<Integer> queue = new LinkedList<>();

        for (Task task : tasks) {
            if (indegree.get(task.getTaskId()) == 0) {
                queue.offer(task.getTaskId());
            }
        }

        List<Integer> executionOrder = new ArrayList<>();

        while (!queue.isEmpty()) {
            int currentTask = queue.poll();
            executionOrder.add(currentTask);

            for (int nextTask : graph.get(currentTask)) {
                indegree.put(nextTask, indegree.get(nextTask) - 1);

                if (indegree.get(nextTask) == 0) {
                    queue.offer(nextTask);
                }
            }
        }

        if (executionOrder.size() != tasks.size()) {
            return new ArrayList<>();
        }

        return executionOrder;
    }

    public Map<Integer, List<Integer>> buildGraph(List<Task> tasks, List<Dependency> dependencies) {
        Map<Integer, List<Integer>> graph = new HashMap<>();

        for (Task task : tasks) {
            graph.put(task.getTaskId(), new ArrayList<>());
        }

        for (Dependency dependency : dependencies) {
            int taskId = dependency.getTaskId();
            int dependsOnTaskId = dependency.getDependsOnTaskId();

            if (graph.containsKey(dependsOnTaskId) && graph.containsKey(taskId)) {
                if (!graph.get(dependsOnTaskId).contains(taskId)) {
                    graph.get(dependsOnTaskId).add(taskId);
                }
            }
        }

        return graph;
    }

    private boolean detectCycle(int taskId, Map<Integer, List<Integer>> graph, Map<Integer, Integer> state) {
        state.put(taskId, 1);

        for (int nextTask : graph.get(taskId)) {
            if (state.get(nextTask) == 1) {
                return true;
            }

            if (state.get(nextTask) == 0 && detectCycle(nextTask, graph, state)) {
                return true;
            }
        }

        state.put(taskId, 2);
        return false;
    }
}