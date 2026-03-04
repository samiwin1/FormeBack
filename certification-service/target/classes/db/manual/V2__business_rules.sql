-- Manual migration script for certification-service business-rule alignment.
-- Execute in MySQL 8+ after backing up data.

ALTER TABLE oral_sessions
    ADD INDEX idx_oral_sessions_scheduled_at (scheduled_at);

ALTER TABLE oral_exam_assignments
    ADD INDEX idx_oral_exam_assignments_learner_id (learner_id),
    ADD INDEX idx_oral_exam_assignments_oral_session_id (oral_session_id),
    ADD COLUMN attempt_number INT NULL,
    ADD COLUMN created_at DATETIME NULL,
    ADD COLUMN updated_at DATETIME NULL;

ALTER TABLE reschedule_requests
    CHANGE COLUMN proposed_date proposed_datetime_1 DATETIME NOT NULL,
    ADD COLUMN proposed_datetime_2 DATETIME NULL,
    ADD COLUMN requested_at DATETIME NULL,
    ADD COLUMN decided_at DATETIME NULL,
    ADD COLUMN approved_datetime DATETIME NULL,
    ADD COLUMN admin_comment TEXT NULL;

ALTER TABLE issued_certifications
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ISSUED',
    ADD COLUMN certificate_number VARCHAR(32) NULL,
    ADD COLUMN expires_at DATETIME NULL,
    ADD INDEX idx_issued_certifications_learner_id (learner_id);

ALTER TABLE oral_sessions
    ADD CONSTRAINT fk_oral_sessions_certification
        FOREIGN KEY (certification_id) REFERENCES certification_catalog(id);

ALTER TABLE oral_exam_assignments
    ADD CONSTRAINT fk_oral_exam_assignments_oral_session
        FOREIGN KEY (oral_session_id) REFERENCES oral_sessions(id);

ALTER TABLE reschedule_requests
    ADD CONSTRAINT fk_reschedule_requests_assignment
        FOREIGN KEY (assignment_id) REFERENCES oral_exam_assignments(id);

ALTER TABLE issued_certifications
    ADD CONSTRAINT fk_issued_certifications_certification
        FOREIGN KEY (certification_id) REFERENCES certification_catalog(id);

ALTER TABLE issued_certifications
    ADD CONSTRAINT uk_issued_cert_learner_cert_formation UNIQUE (learner_id, certification_id, formation_id),
    ADD CONSTRAINT uk_issued_cert_certificate_number UNIQUE (certificate_number);
