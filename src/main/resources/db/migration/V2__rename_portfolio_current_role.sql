-- Hibernate emits unquoted identifiers; `current_role` is reserved on MariaDB/MySQL (CURRENT_ROLE).
ALTER TABLE learner_portfolio CHANGE COLUMN `current_role` present_role VARCHAR(100);
