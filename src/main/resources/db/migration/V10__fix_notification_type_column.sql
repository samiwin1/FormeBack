-- Fix: convert mentor_notification.type from ENUM to VARCHAR(40)
-- so Hibernate schema validation passes with @JdbcTypeCode(SqlTypes.VARCHAR)
ALTER TABLE mentor_notification MODIFY COLUMN type VARCHAR(40) NOT NULL;
