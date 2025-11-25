INSERT INTO log (title, error_message, error_level, username, time_stamp, created_date)
VALUES ('Connection Timeout', 'Database took too long', 'ERROR', 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO log (title, error_message, error_level, username, time_stamp, created_date)
VALUES ('Low Memory', 'Memory is running low', 'WARN', 'monitor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO log (title, error_message, error_level, username, time_stamp, created_date)
VALUES ('Invalid Login', 'User entered wrong credentials', 'ERROR', 'user123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO log (title, error_message, error_level, username, time_stamp, created_date)
VALUES ('Debug Mode', 'Debug log for development', 'DEBUG', 'devops', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO log (title, error_message, error_level, username, time_stamp, created_date)
VALUES ('Payment Processed', 'User payment completed successfully', 'INFO', 'billing', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO log (title, error_message, error_level, username, time_stamp, created_date)
VALUES ('High CPU Usage', 'CPU crossed 90% utilization', 'WARN', 'system_monitor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
