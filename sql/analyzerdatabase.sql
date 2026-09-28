CREATE DATABASE IF NOT EXISTS mini_workflow_analyzer;
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
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT chk_task_status
        CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    CONSTRAINT uq_task_workflow
        UNIQUE (workflow_id, task_id),
    CONSTRAINT fk_task_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES workflows(workflow_id)
        ON DELETE CASCADE
);

CREATE TABLE dependencies (
    dependency_id INT PRIMARY KEY AUTO_INCREMENT,
    workflow_id INT NOT NULL,
    task_id INT NOT NULL,
    depends_on_task_id INT NOT NULL,
    CONSTRAINT chk_no_self_dependency
        CHECK (task_id <> depends_on_task_id),
    CONSTRAINT uq_task_dependency
        UNIQUE (task_id, depends_on_task_id),
    CONSTRAINT fk_dependency_task
        FOREIGN KEY (workflow_id, task_id)
        REFERENCES tasks(workflow_id, task_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_dependency_parent
        FOREIGN KEY (workflow_id, depends_on_task_id)
        REFERENCES tasks(workflow_id, task_id)
        ON DELETE CASCADE
);

SHOW TABLES;