CREATE TABLE formation_demand (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT       NOT NULL,
    requested_role     VARCHAR(100),
    detected_skills_gap TEXT,
    created_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_formation_demand_role (requested_role),
    INDEX idx_formation_demand_user (user_id)
);
