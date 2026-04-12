-- Initial schema for certification service
-- This migration assumes JPA has already created the base tables via ddl-auto=update
-- We're adding indexes, constraints, and missing columns for production readiness

-- ============================================================================
-- CERTIFICATION CATALOG
-- ============================================================================
ALTER TABLE certification_catalog
    ADD INDEX IF NOT EXISTS idx_certification_catalog_status (status);

-- ============================================================================
-- ORAL SESSIONS
-- ============================================================================
ALTER TABLE oral_sessions
    ADD INDEX IF NOT EXISTS idx_oral_sessions_scheduled_at (scheduled_at),
    ADD INDEX IF NOT EXISTS idx_oral_sessions_status (status),
    ADD INDEX IF NOT EXISTS idx_oral_sessions_evaluator_id (evaluator_id);

ALTER TABLE oral_sessions
    ADD CONSTRAINT IF NOT EXISTS fk_oral_sessions_certification
        FOREIGN KEY (certification_id) REFERENCES certification_catalog(id)
        ON DELETE RESTRICT;

-- ============================================================================
-- ORAL EXAM ASSIGNMENTS
-- ============================================================================
ALTER TABLE oral_exam_assignments
    ADD INDEX IF NOT EXISTS idx_oral_exam_assignments_learner_id (learner_id),
    ADD INDEX IF NOT EXISTS idx_oral_exam_assignments_oral_session_id (oral_session_id),
    ADD INDEX IF NOT EXISTS idx_oral_exam_assignments_status (status),
    ADD INDEX IF NOT EXISTS idx_oral_exam_assignments_formation_id (formation_id);

-- Add columns if they don't exist
ALTER TABLE oral_exam_assignments
    ADD COLUMN IF NOT EXISTS attempt_number INT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE oral_exam_assignments
    ADD CONSTRAINT IF NOT EXISTS fk_oral_exam_assignments_oral_session
        FOREIGN KEY (oral_session_id) REFERENCES oral_sessions(id)
        ON DELETE CASCADE;

-- ============================================================================
-- RESCHEDULE REQUESTS
-- ============================================================================
-- Rename old column if it exists
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'reschedule_requests' 
    AND COLUMN_NAME = 'proposed_date');

SET @sql = IF(@col_exists > 0,
    'ALTER TABLE reschedule_requests CHANGE COLUMN proposed_date proposed_datetime_1 DATETIME NOT NULL',
    'SELECT "Column proposed_date does not exist, skipping rename"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add new columns if they don't exist
ALTER TABLE reschedule_requests
    ADD COLUMN IF NOT EXISTS proposed_datetime_2 DATETIME NULL,
    ADD COLUMN IF NOT EXISTS requested_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS decided_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS approved_datetime DATETIME NULL,
    ADD COLUMN IF NOT EXISTS admin_comment TEXT NULL;

ALTER TABLE reschedule_requests
    ADD INDEX IF NOT EXISTS idx_reschedule_requests_status (status),
    ADD INDEX IF NOT EXISTS idx_reschedule_requests_learner_id (learner_id);

ALTER TABLE reschedule_requests
    ADD CONSTRAINT IF NOT EXISTS fk_reschedule_requests_assignment
        FOREIGN KEY (assignment_id) REFERENCES oral_exam_assignments(id)
        ON DELETE CASCADE;

-- ============================================================================
-- ISSUED CERTIFICATIONS
-- ============================================================================
ALTER TABLE issued_certifications
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ISSUED',
    ADD COLUMN IF NOT EXISTS certificate_number VARCHAR(32) NULL,
    ADD COLUMN IF NOT EXISTS expires_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS pdf_path VARCHAR(255) NULL;

ALTER TABLE issued_certifications
    ADD INDEX IF NOT EXISTS idx_issued_certifications_learner_id (learner_id),
    ADD INDEX IF NOT EXISTS idx_issued_certifications_formation_id (formation_id),
    ADD INDEX IF NOT EXISTS idx_issued_certifications_status (status);

ALTER TABLE issued_certifications
    ADD CONSTRAINT IF NOT EXISTS fk_issued_certifications_certification
        FOREIGN KEY (certification_id) REFERENCES certification_catalog(id)
        ON DELETE RESTRICT;

-- Add unique constraints
ALTER TABLE issued_certifications
    ADD CONSTRAINT IF NOT EXISTS uk_issued_cert_learner_cert_formation 
        UNIQUE (learner_id, certification_id, formation_id);

ALTER TABLE issued_certifications
    ADD CONSTRAINT IF NOT EXISTS uk_issued_cert_certificate_number 
        UNIQUE (certificate_number);

-- ============================================================================
-- NOTIFICATIONS
-- ============================================================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notifications_user_id (user_id),
    INDEX idx_notifications_is_read (is_read),
    INDEX idx_notifications_created_at (created_at)
);

-- ============================================================================
-- SESSION FEEDBACK
-- ============================================================================
CREATE TABLE IF NOT EXISTS session_feedback (
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
