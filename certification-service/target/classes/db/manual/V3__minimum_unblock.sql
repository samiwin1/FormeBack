-- Minimal unblock migration requested by user.
-- 1) Internal FK integrity
-- 2) reschedule datetime/audit columns
-- 3) certificate_number unique

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

ALTER TABLE reschedule_requests
    CHANGE COLUMN proposed_date proposed_datetime DATETIME NOT NULL,
    ADD COLUMN requested_at DATETIME NULL,
    ADD COLUMN decided_at DATETIME NULL;

ALTER TABLE issued_certifications
    ADD COLUMN certificate_number VARCHAR(32) NULL,
    ADD CONSTRAINT uk_issued_cert_certificate_number UNIQUE (certificate_number);
