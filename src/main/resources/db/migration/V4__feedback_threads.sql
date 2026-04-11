-- ============================================================
-- V4 — Feedback, Conversation Threads, Conversation Messages
-- ============================================================

-- ----------------------------------------------------------------
-- Feature 1: Learner feedback on AI responses
-- One feedback row per session per user (unique constraint).
-- References mentor_session so the frontend only needs sessionId.
-- ----------------------------------------------------------------
CREATE TABLE mentor_feedback (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    rating     VARCHAR(10) NOT NULL,       -- 'UP' or 'DOWN'
    comment    VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_session FOREIGN KEY (session_id) REFERENCES mentor_session (id),
    CONSTRAINT uq_feedback_per_session UNIQUE (session_id, user_id)
);

-- ----------------------------------------------------------------
-- Feature 2: Persistent conversation threads
-- ----------------------------------------------------------------
CREATE TABLE conversation_thread (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    title        VARCHAR(200) NOT NULL DEFAULT 'New Conversation',
    formation_id BIGINT,
    is_active    BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_thread_user (user_id),
    INDEX idx_thread_updated (user_id, updated_at)
);

CREATE TABLE conversation_message (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    thread_id  BIGINT      NOT NULL,
    role       VARCHAR(10) NOT NULL,   -- 'USER' or 'ASSISTANT'
    content    TEXT        NOT NULL,
    session_id BIGINT,                 -- FK to mentor_session for assistant messages
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_thread FOREIGN KEY (thread_id) REFERENCES conversation_thread (id),
    INDEX idx_message_thread (thread_id, created_at)
);
