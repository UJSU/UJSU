CREATE TABLE db_changes_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(50),
    operation ENUM('INSERT', 'UPDATE', 'DELETE'),
    record_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);