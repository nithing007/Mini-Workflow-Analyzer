# Mini Workflow Analyzer

A Java-based workflow analysis system built with **Java, JDBC, and MySQL**.

The project models workflows, tasks, and task dependencies, then analyzes dependencies to detect circular workflows and determine a valid execution order.

## Tech Stack

- Java
- JDBC
- MySQL
- MySQL Connector/J

## Core Features

- Workflow and task management
- Task dependency management
- Circular dependency detection
- Topological sorting
- Workflow execution
- MySQL database integration

## Project Structure

```text
src/
├── DatabaseManager.java
├── Dependency.java
├── Task.java
├── Workflow.java
├── WorkflowAnalyzer.java
├── WorkflowExecutor.java
└── WorkflowManager.java