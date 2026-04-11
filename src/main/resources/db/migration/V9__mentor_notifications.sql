-- Mentor-service internal notifications
CREATE TABLE mentor_notification (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_user_id BIGINT        NULL,        -- NULL = admin broadcast; userId = specific learner
    type           VARCHAR(40)   NOT NULL,
    title          VARCHAR(200)  NOT NULL,
    message        TEXT,
    reference_id   BIGINT        NULL,        -- demandId for context / navigation
    is_read        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_mn_target  (target_user_id),
    INDEX idx_mn_unread  (target_user_id, is_read)
);

-- Track fulfilled status on formation_demand
ALTER TABLE formation_demand ADD COLUMN fulfilled BOOLEAN NOT NULL DEFAULT FALSE;
