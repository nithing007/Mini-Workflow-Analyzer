import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class WorkflowManager {
    public int createWorkflow(String workflowName, String description) {
        String sql = "INSERT INTO workflows (workflow_name, description) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, workflowName);
            statement.setString(2, description);
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error creating workflow: " + e.getMessage());
        }
        return -1;
    }

    public int createTask(int workflowId, String taskName) {
        String sql = "INSERT INTO tasks (workflow_id, task_name, status) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, workflowId);
            statement.setString(2, taskName);
            statement.setString(3, "PENDING");
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        }
         catch (SQLException e) {
            System.out.println("Error creating task: " + e.getMessage());
        }
        return -1;
    }

    public boolean addDependency(int taskId, int dependsOnTaskId) {
        if (taskId == dependsOnTaskId) {
            return false;
        }

        String sql = "INSERT INTO dependencies (task_id, depends_on_task_id) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, taskId);
            statement.setInt(2, dependsOnTaskId);

            return statement.executeUpdate() > 0;

        } 
        catch (SQLException e) {
            System.out.println("Error adding dependency: " + e.getMessage());
            return false;
        }
    }

    public List<Workflow> getAllWorkflows() {
        List<Workflow> workflows = new ArrayList<>();

        String sql = "SELECT workflow_id, workflow_name, description FROM workflows";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                workflows.add(new Workflow(
                        resultSet.getInt("workflow_id"),
                        resultSet.getString("workflow_name"),
                        resultSet.getString("description")
                ));
            }
        } 
        catch (SQLException e) {
            System.out.println("Error retrieving workflows: " + e.getMessage());
        }
        return workflows;
    }

    public List<Task> getTasksByWorkflow(int workflowId) {
        List<Task> tasks = new ArrayList<>();

        String sql = "SELECT task_id, workflow_id, task_name, status " +
                     "FROM tasks WHERE workflow_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, workflowId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    tasks.add(new Task(
                            resultSet.getInt("task_id"),
                            resultSet.getInt("workflow_id"),
                            resultSet.getString("task_name"),
                            resultSet.getString("status")
                    ));
                }
            }
        } 
        catch (SQLException e) {
            System.out.println("Error retrieving tasks: " + e.getMessage());
        }
        return tasks;
    }

    public List<Dependency> getDependenciesByWorkflow(int workflowId) {
        List<Dependency> dependencies = new ArrayList<>();

        String sql = "SELECT d.dependency_id, d.task_id, d.depends_on_task_id " +
                     "FROM dependencies d " +
                     "JOIN tasks t ON d.task_id = t.task_id " +
                     "WHERE t.workflow_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, workflowId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    dependencies.add(new Dependency(
                            resultSet.getInt("dependency_id"),
                            resultSet.getInt("task_id"),
                            resultSet.getInt("depends_on_task_id")
                    ));
                }
            }
        } 
        catch (SQLException e) {
            System.out.println("Error retrieving dependencies: " + e.getMessage());
        }
        return dependencies;
    }

    public boolean updateTaskStatus(int taskId, String status) {
        String sql = "UPDATE tasks SET status = ? WHERE task_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, taskId);

            return statement.executeUpdate() > 0;
        }
         catch (SQLException e) {
            System.out.println("Error updating task status: " + e.getMessage());
            return false;
        }
    }
}