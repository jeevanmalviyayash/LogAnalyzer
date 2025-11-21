INSERT INTO log_entry (title, description, level, username, timestamp, created_date)
VALUES ('Connection Timeout', 'Database took too long', 'ERROR', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO log_entry (title, description, level, username, timestamp, created_date)
VALUES ('Low Memory', 'Memory is running low', 'WARN', 'monitor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO log_entry (title, description, level, username, timestamp, created_date)
VALUES ('Invalid Login', 'User entered wrong credentials', 'ERROR', 'user123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO log_entry (title, description, level, username, timestamp, created_date)
VALUES ('Debug Mode', 'Debug log for development', 'DEBUG', 'devops', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);