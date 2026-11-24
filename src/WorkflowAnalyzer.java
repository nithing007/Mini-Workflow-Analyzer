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
    public enum AnalysisStatus {
        VALID,
        CYCLE_DETECTED,
        INVALID_DATA
    }

    public static class AnalysisResult {
        private final AnalysisStatus status;
        private final List<Integer> executionOrder;
        private final Map<Integer, List<Integer>> graph;

        private AnalysisResult(AnalysisStatus status, List<Integer> executionOrder, Map<Integer, List<Integer>> graph) {
            this.status = status;
            this.executionOrder = Collections.unmodifiableList(new ArrayList<>(executionOrder));
            Map<Integer, List<Integer>> immutableGraph = new LinkedHashMap<>();
            for (Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
                immutableGraph.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
            }
            this.graph = Collections.unmodifiableMap(immutableGraph);
        }

        public AnalysisStatus getStatus() {
            return status;
        }

        public List<Integer> getExecutionOrder() {
            return executionOrder;
        }

        public Map<Integer, List<Integer>> getGraph() {
            return graph;
        }

        public boolean hasCycle() {
            return status == AnalysisStatus.CYCLE_DETECTED;
        }

        public boolean isValid() {
            return status == AnalysisStatus.VALID;
        }
    }

    public boolean hasCycle(List<Task> tasks, List<Dependency> dependencies) {
        return analyze(tasks, dependencies).hasCycle();
    }

    public List<Integer> getExecutionOrder(List<Task> tasks, List<Dependency> dependencies) {
        return new ArrayList<>(analyze(tasks, dependencies).getExecutionOrder());
    }

    public Map<Integer, List<Integer>> buildGraph(List<Task> tasks, List<Dependency> dependencies) {
        AnalysisResult result = analyze(tasks, dependencies);
        if (result.getStatus() == AnalysisStatus.INVALID_DATA) {
            return Collections.emptyMap();
        }
        return result.getGraph();
    }

    public AnalysisResult analyze(List<Task> tasks, List<Dependency> dependencies) {
        if (tasks == null || dependencies == null || tasks.isEmpty()) {
            return invalidResult();
        }

        Set<Integer> taskIds = new HashSet<>(tasks.size());
        Map<Integer, List<Integer>> graph = new LinkedHashMap<>(tasks.size());
        Map<Integer, Integer> inDegree = new HashMap<>(tasks.size());

        for (Task task : tasks) {
            if (task == null || task.getTaskId() <= 0 || !taskIds.add(task.getTaskId())) {
                return invalidResult();
            }
            int id = task.getTaskId();
            graph.put(id, new ArrayList<>());
            inDegree.put(id, 0);
        }

        Set<Long> seenEdges = new HashSet<>(dependencies.size());

        for (Dependency dependency : dependencies) {
            if (dependency == null) {
                return invalidResult();
            }

            int dependentTaskId = dependency.getTaskId();
            int prerequisiteTaskId = dependency.getDependsOnTaskId();

            if (dependentTaskId <= 0 || prerequisiteTaskId <= 0 ||
                    dependentTaskId == prerequisiteTaskId ||
                    !taskIds.contains(dependentTaskId) ||
                    !taskIds.contains(prerequisiteTaskId)) {
                return invalidResult();
            }

            long edgeKey = ((long) prerequisiteTaskId << 32) | (dependentTaskId & 0xFFFFFFFFL);

            if (!seenEdges.add(edgeKey)) {
                return invalidResult();
            }

            graph.get(prerequisiteTaskId).add(dependentTaskId);
            inDegree.put(dependentTaskId, inDegree.get(dependentTaskId) + 1);
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
            return new AnalysisResult(AnalysisStatus.CYCLE_DETECTED, Collections.emptyList(), graph);
        }

        return new AnalysisResult(AnalysisStatus.VALID, executionOrder, graph);
    }

    private AnalysisResult invalidResult() {
        return new AnalysisResult(AnalysisStatus.INVALID_DATA, Collections.emptyList(), Collections.emptyMap());
    }
}
