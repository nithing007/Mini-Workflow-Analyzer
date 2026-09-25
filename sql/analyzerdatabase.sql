CREATE DATABASE mini_workflow_analyzer;
USE mini_workflow_analyzer;
CREATE TABLE workflows (
    workflow_id INT PRIMARY KEY AUTO_INCREMENT,
    workflow_name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);
CREATE TABLE tasks (
    task_id INT PRIMARY KEY AUTO_INCREMENT,
    workflow_id INT NOT NULL,
    task_name VARCHAR(100) NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING',
    FOREIGN KEY (workflow_id) REFERENCES workflows(workflow_id)
);
CREATE TABLE dependencies (
    dependency_id INT PRIMARY KEY AUTO_INCREMENT,
    task_id INT NOT NULL,
    depends_on_task_id INT NOT NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(task_id),
    FOREIGN KEY (depends_on_task_id) REFERENCES tasks(task_id)
);
SHOW TABLES;
