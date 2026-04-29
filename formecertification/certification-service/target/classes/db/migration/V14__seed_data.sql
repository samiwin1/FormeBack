INSERT INTO certifications (title, description, duration_minutes, active) VALUES
('AWS Certified Cloud Practitioner', 'Validation of overall understanding of the AWS Cloud platform', 90, true),
('Cisco Certified Network Associate', 'Demonstrate knowledge and skills related to network fundamentals', 120, true),
('Microsoft Azure Fundamentals', 'Basic knowledge of cloud services and how those services are provided with Microsoft Azure', 90, true);

INSERT INTO oral_sessions (certification_id, evaluator_id, session_date, start_time, end_time, max_participants, current_participants, meeting_link, status) VALUES
(1, 101, '2026-05-15', '09:00:00', '11:00:00', 10, 0, 'https://zoom.us/j/111111', 'SCHEDULED'),
(2, 102, '2026-05-20', '14:00:00', '16:00:00', 8, 2, 'https://zoom.us/j/222222', 'SCHEDULED'),
(3, 103, '2026-06-05', '10:00:00', '12:00:00', 15, 5, 'https://zoom.us/j/333333', 'SCHEDULED');
