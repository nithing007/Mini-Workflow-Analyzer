# Mini Workflow Analyzer

A Java-based workflow management system that models task dependencies, detects circular dependencies, and generates valid execution sequences using **Topological Sorting (Kahn's Algorithm)**. Built with Java, JDBC, and MySQL.

## Features

- **Workflow Management:** Create and manage workflows and tasks.
- **Dependency Management:** Define prerequisite relationships between tasks.
- **Cycle Detection:** Identify circular dependencies that prevent execution.
- **Topological Sorting:** Generate valid task execution sequences.
- **Workflow Execution:** Execute tasks in dependency order.
- **Status Tracking:** Track task states (`PENDING`, `RUNNING`, `COMPLETED`, `FAILED`).
- **Database Integration:** Persist workflow data using MySQL and JDBC.

## Tech Stack

- Java 11+
- JDBC
- MySQL 8.0+
- MySQL Connector/J 8.0.33

## Project Structure

```text
Mini-Workflow-Analyzer/
├── config/
│   └── db.properties.example
├── lib/
│   └── mysql-connector-j-8.0.33.jar
├── sql/
│   └── analyzerdatabase.sql
├── src/
│   ├── Main.java
│   ├── DatabaseManager.java
│   ├── Dependency.java
│   ├── Task.java
│   ├── Workflow.java
│   ├── WorkflowAnalyzer.java
│   ├── WorkflowExecutor.java
│   └── WorkflowManager.java
├── .gitignore
└── README.md
```

## Architecture

| Component | Responsibility |
|---|---|
| `Main.java` | User interaction and application flow |
| `WorkflowManager.java` | Workflow, task, dependency, and database operations |
| `WorkflowAnalyzer.java` | Dependency graph construction, cycle detection, and topological sorting |
| `WorkflowExecutor.java` | Execution validation and task status management |
| `DatabaseManager.java` | JDBC connection management |
| `Workflow.java`, `Task.java`, `Dependency.java` | Application data models |

## Algorithm

The project uses **Kahn's Algorithm** for cycle detection and topological sorting.

1. Calculate the in-degree of every task.
2. Add tasks with zero in-degree to a queue.
3. Process tasks and reduce the in-degree of their dependent tasks.
4. Add newly available tasks to the queue.
5. If all tasks are processed, a valid execution order exists; otherwise, a cycle is present.

| Complexity | Value |
|---|---|
| Time | O(V + E) |
| Space | O(V + E) |

`V` = Number of tasks, `E` = Number of dependencies.

## Prerequisites

- JDK 11 or later
- MySQL Server 8.0 or later
- MySQL Connector/J 8.0.33

## Setup and Execution

**1. Clone the repository**

```bash
git clone https://github.com/nithing007/Mini-Workflow-Analyzer.git
cd Mini-Workflow-Analyzer
```

**2. Create the database**

Execute the SQL script using MySQL:

```bash
mysql -u root -p < sql/analyzerdatabase.sql
```

**3. Configure database credentials**

Copy `config/db.properties.example` to `config/db.properties` and update your MySQL credentials.

```properties
db.url=jdbc:mysql://localhost:3306/mini_workflow_analyzer
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

**4. Compile the application**

```bash
javac -cp "lib/*" -d out src/*.java
```

**5. Run the application**

Windows:

```bash
java -cp "out;lib/*" Main
```

Linux / macOS:

```bash
java -cp "out:lib/*" Main
```

## Future Enhancements

- Real task execution handlers
- Workflow retry and resume support
- JUnit unit and integration testing
- Maven or Gradle build automation
- GitHub Actions CI/CD integration
- Workflow execution history and reporting

## Author

**Nithin G**

[GitHub](https://github.com/nithing007)