import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class WorkflowAnalyzer {

    private static class AnalysisResult {
        final boolean hasCycle;
        final List<Integer> executionOrder;
        final Map<Integer, List<Integer>> graph;

        AnalysisResult(boolean hasCycle, List<Integer> executionOrder, Map<Integer, List<Integer>> graph) {
            this.hasCycle = hasCycle;
            this.executionOrder = executionOrder;
            this.graph = graph;
        }
    }

    private List<Task> cachedTasks;
    private List<Dependency> cachedDependencies;
    private AnalysisResult cachedResult;

    public boolean hasCycle(List<Task> tasks, List<Dependency> dependencies) {
        return analyze(tasks, dependencies).hasCycle;
    }

    public List<Integer> getExecutionOrder(List<Task> tasks, List<Dependency> dependencies) {
        return new ArrayList<>(analyze(tasks, dependencies).executionOrder);
    }

    public Map<Integer, List<Integer>> buildGraph(List<Task> tasks, List<Dependency> dependencies) {
        Map<Integer, List<Integer>> graph = new LinkedHashMap<>();

        if (tasks == null) {
            return graph;
        }

        for (Task task : tasks) {
            if (task != null) {
                graph.putIfAbsent(task.getTaskId(), new ArrayList<>());
            }
        }

        if (dependencies == null) {
            return graph;
        }

        for (Dependency dependency : dependencies) {
            if (dependency == null) {
                continue;
            }
            int taskId = dependency.getTaskId();
            int dependsOnTaskId = dependency.getDependsOnTaskId();

            if (graph.containsKey(dependsOnTaskId) && graph.containsKey(taskId)) {
                List<Integer> dependents = graph.get(dependsOnTaskId);
                if (!dependents.contains(taskId)) {
                    dependents.add(taskId);
                }
            }
        }

        return graph;
    }

    private AnalysisResult analyze(List<Task> tasks, List<Dependency> dependencies) {
        if (tasks == cachedTasks && dependencies == cachedDependencies && cachedResult != null) {
            return cachedResult;
        }

        if (tasks == null || dependencies == null || tasks.isEmpty()) {
            AnalysisResult result = new AnalysisResult(false, Collections.emptyList(), Collections.emptyMap());
            cache(tasks, dependencies, result);
            return result;
        }

        Set<Integer> taskIds = new HashSet<>(tasks.size());
        Map<Integer, List<Integer>> graph = new LinkedHashMap<>(tasks.size());
        Map<Integer, Integer> inDegree = new HashMap<>(tasks.size());

        for (Task task : tasks) {
            if (task == null) {
                AnalysisResult result = new AnalysisResult(false, Collections.emptyList(), Collections.emptyMap());
                cache(tasks, dependencies, result);
                return result;
            }
            int id = task.getTaskId();
            if (!taskIds.add(id)) {
                AnalysisResult result = new AnalysisResult(false, Collections.emptyList(), Collections.emptyMap());
                cache(tasks, dependencies, result);
                return result;
            }
            graph.put(id, new ArrayList<>());
            inDegree.put(id, 0);
        }

        Set<Long> seenEdges = new HashSet<>(dependencies.size());
        boolean hasSelfDependency = false;

        for (Dependency dependency : dependencies) {
            if (dependency == null) {
                AnalysisResult result = new AnalysisResult(false, Collections.emptyList(), Collections.emptyMap());
                cache(tasks, dependencies, result);
                return result;
            }

            int dependentTaskId = dependency.getTaskId();
            int prereqTaskId = dependency.getDependsOnTaskId();

            if (dependentTaskId == prereqTaskId) {
                hasSelfDependency = true;
                continue;
            }

            if (!taskIds.contains(dependentTaskId) || !taskIds.contains(prereqTaskId)) {
                AnalysisResult result = new AnalysisResult(false, Collections.emptyList(), Collections.emptyMap());
                cache(tasks, dependencies, result);
                return result;
            }

            long edgeKey = (((long) prereqTaskId & 0xFFFFFFFFL) << 32) | (dependentTaskId & 0xFFFFFFFFL);
            if (seenEdges.add(edgeKey)) {
                graph.get(prereqTaskId).add(dependentTaskId);
                inDegree.put(dependentTaskId, inDegree.get(dependentTaskId) + 1);
            }
        }

        if (hasSelfDependency) {
            AnalysisResult result = new AnalysisResult(true, Collections.emptyList(), graph);
            cache(tasks, dependencies, result);
            return result;
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (Task task : tasks) {
            int id = task.getTaskId();
            if (inDegree.get(id) == 0) {
                queue.offer(id);
            }
        }

        List<Integer> executionOrder = new ArrayList<>(tasks.size());

        while (!queue.isEmpty()) {
            int current = queue.poll();
            executionOrder.add(current);

            for (int neighbor : graph.get(current)) {
                int updatedInDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, updatedInDegree);
                if (updatedInDegree == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        if (executionOrder.size() != tasks.size()) {
            AnalysisResult result = new AnalysisResult(true, Collections.emptyList(), graph);
            cache(tasks, dependencies, result);
            return result;
        }

        AnalysisResult result = new AnalysisResult(false, executionOrder, graph);
        cache(tasks, dependencies, result);
        return result;
    }

    private void cache(List<Task> tasks, List<Dependency> dependencies, AnalysisResult result) {
        this.cachedTasks = tasks;
        this.cachedDependencies = dependencies;
        this.cachedResult = result;
    }
}