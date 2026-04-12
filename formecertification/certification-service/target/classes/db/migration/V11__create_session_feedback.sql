CREATE TABLE session_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    learner_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    issued_certification_id BIGINT NOT NULL,
    session_rating INT NOT NULL CHECK (session_rating BETWEEN 1 AND 5),
    evaluator_rating INT NOT NULL CHECK (evaluator_rating BETWEEN 1 AND 5),
    comment VARCHAR(500),
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_feedback_learner_cert UNIQUE (learner_id, issued_certification_id),
    INDEX idx_feedback_session (session_id),
    INDEX idx_feedback_learner (learner_id)
);

