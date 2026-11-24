import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
public class WorkflowManager {
    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "RUNNING", "COMPLETED", "FAILED");

    public int createWorkflow(String workflowName, String description) {
        if (workflowName == null || workflowName.trim().isEmpty() || workflowName.trim().length() > 100 || (description != null && description.trim().length() > 255)) {
            System.out.println("Invalid workflow name or description length.");
            return -1;
        }

        String sql = "INSERT INTO workflows (workflow_name, description) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection()) {
            if (connection == null) {
                System.out.println("Database connection failed.");
                return -1;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, workflowName.trim());
                statement.setString(2, description == null || description.trim().isEmpty() ? null : description.trim());
                statement.executeUpdate();

                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating workflow: " + e.getMessage());
        }
        return -1;
    }

    public int createTask(int workflowId, String taskName) {
        if (workflowId <= 0 || taskName == null || taskName.trim().isEmpty() || taskName.trim().length() > 100) {
            System.out.println("Invalid workflow ID or task name.");
            return -1;
        }

        String sql = "INSERT INTO tasks (workflow_id, task_name, status) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection()) {
            if (connection == null) {
                System.out.println("Database connection failed.");
                return -1;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, workflowId);
                statement.setString(2, taskName.trim());
                statement.setString(3, "PENDING");
                statement.executeUpdate();

                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating task: " + e.getMessage());
        }
        return -1;
    }

    public boolean addDependency(int taskId, int dependsOnTaskId) {
        if (taskId <= 0 || dependsOnTaskId <= 0 || taskId == dependsOnTaskId) {
            System.out.println("Invalid task IDs or self-dependency.");
            return false;
        }

        String lockSql = "SELECT task_id FROM tasks WHERE task_id IN (?, ?) ORDER BY task_id FOR UPDATE";
        String cycleSql = "WITH RECURSIVE dependency_chain (task_id) AS (" +
                "SELECT CAST(? AS SIGNED) " +
                "UNION DISTINCT " +
                "SELECT d.depends_on_task_id FROM dependencies d " +
                "JOIN dependency_chain dc ON d.task_id = dc.task_id" +
                ") SELECT EXISTS (SELECT 1 FROM dependency_chain WHERE task_id = ?)";

        String insertSql = "INSERT INTO dependencies (workflow_id, task_id, depends_on_task_id) " +
                "SELECT t.workflow_id, ?, ? FROM tasks t " +
                "JOIN tasks d ON t.workflow_id = d.workflow_id " +
                "WHERE t.task_id = ? AND d.task_id = ?";

        try (Connection connection = DatabaseManager.getConnection()) {
            if (connection == null) {
                System.out.println("Database connection failed.");
                return false;
            }

            connection.setAutoCommit(false);

            try {
                int lockedTasks = 0;

                try (PreparedStatement statement = connection.prepareStatement(lockSql)) {
                    statement.setInt(1, taskId);
                    statement.setInt(2, dependsOnTaskId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            lockedTasks++;
                        }
                    }
                }

                if (lockedTasks != 2) {
                    connection.rollback();
                    System.out.println("Task IDs do not belong to an existing workflow.");
                    return false;
                }

                try (PreparedStatement statement = connection.prepareStatement(cycleSql)) {
                    statement.setInt(1, dependsOnTaskId);
                    statement.setInt(2, taskId);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next() && resultSet.getBoolean(1)) {
                            connection.rollback();
                            System.out.println("Dependency would create a circular workflow.");
                            return false;
                        }
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setInt(1, taskId);
                    statement.setInt(2, dependsOnTaskId);
                    statement.setInt(3, taskId);
                    statement.setInt(4, dependsOnTaskId);

                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        System.out.println("Tasks do not belong to the same workflow or dependency already exists.");
                        return false;
                    }
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error adding dependency: " + e.getMessage());
            return false;
        }
    }

    public List<Workflow> getAllWorkflows() {
        String sql = "SELECT workflow_id, workflow_name, description FROM workflows ORDER BY workflow_id";

        try (Connection connection = DatabaseManager.getConnection()) {
            if (connection == null) {
                System.out.println("Database connection failed.");
                return null;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                List<Workflow> workflows = new ArrayList<>();

                while (resultSet.next()) {
                    workflows.add(new Workflow(
                            resultSet.getInt("workflow_id"),
                            resultSet.getString("workflow_name"),
                            resultSet.getString("description")
                    ));
                }
                return workflows;
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving workflows: " + e.getMessage());
            return null;
        }
    }

    public List<Task> getTasksByWorkflow(int workflowId) {
        if (workflowId <= 0) {
            System.out.println("Invalid workflow ID.");
            return null;
        }

        String sql = "SELECT task_id, workflow_id, task_name, status FROM tasks WHERE workflow_id = ? ORDER BY task_id";

        try (Connection connection = DatabaseManager.getConnection()) {
            if (connection == null) {
                System.out.println("Database connection failed.");
                return null;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, workflowId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Task> tasks = new ArrayList<>();

                    while (resultSet.next()) {
                        tasks.add(new Task(
                                resultSet.getInt("task_id"),
                                resultSet.getInt("workflow_id"),
                                resultSet.getString("task_name"),
                                resultSet.getString("status")
                        ));
                    }
                    return tasks;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving tasks: " + e.getMessage());
            return null;
        }
    }

    public List<Dependency> getDependenciesByWorkflow(int workflowId) {
        if (workflowId <= 0) {
            System.out.println("Invalid workflow ID.");
            return null;
        }

        String sql = "SELECT dependency_id, task_id, depends_on_task_id FROM dependencies WHERE workflow_id = ? ORDER BY dependency_id";

        try (Connection connection = DatabaseManager.getConnection()) {
            if (connection == null) {
                System.out.println("Database connection failed.");
                return null;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, workflowId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    List<Dependency> dependencies = new ArrayList<>();

                    while (resultSet.next()) {
                        dependencies.add(new Dependency(
                                resultSet.getInt("dependency_id"),
                                resultSet.getInt("task_id"),
                                resultSet.getInt("depends_on_task_id")
                        ));
                    }
                    return dependencies;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving dependencies: " + e.getMessage());
            return null;
        }
    }

    public boolean updateTaskStatus(int taskId, String status) {
        if (taskId <= 0 || status == null || !VALID_STATUSES.contains(status.trim().toUpperCase())) {
            System.out.println("Invalid task ID or status.");
            return false;
        }

        String normalizedStatus = status.trim().toUpperCase();

        String sql = "UPDATE tasks SET " +
                "started_at = CASE WHEN ? = 'RUNNING' THEN CURRENT_TIMESTAMP WHEN ? = 'PENDING' THEN NULL ELSE started_at END, " +
                "completed_at = CASE WHEN ? IN ('COMPLETED', 'FAILED') THEN CURRENT_TIMESTAMP WHEN ? IN ('PENDING', 'RUNNING') THEN NULL ELSE completed_at END, " +
                "status = ? WHERE task_id = ?";

        try (Connection connection = DatabaseManager.getConnection()) {
            if (connection == null) {
                System.out.println("Database connection failed.");
                return false;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, normalizedStatus);
                statement.setString(2, normalizedStatus);
                statement.setString(3, normalizedStatus);
                statement.setString(4, normalizedStatus);
                statement.setString(5, normalizedStatus);
                statement.setInt(6, taskId);

                return statement.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error updating task status: " + e.getMessage());
            return false;
        }
    }
}
