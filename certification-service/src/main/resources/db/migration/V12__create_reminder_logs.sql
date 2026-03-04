CREATE TABLE reminder_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NULL,
    reminder_type VARCHAR(50) NULL DEFAULT 'SESSION_24H',
    session_id BIGINT NULL,
    learner_id BIGINT NULL,
    stage VARCHAR(10) NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_reminder_assignment (assignment_id, reminder_type),
    UNIQUE KEY uq_reminder_session_learner (session_id, learner_id, stage)
);
