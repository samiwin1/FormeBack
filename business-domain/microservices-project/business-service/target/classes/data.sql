INSERT INTO partner (id, name, contact_email, contact_phone) VALUES
(1, 'Tech Corp', 'info@techcorp.com', '+216 22 333 444'),
(2, 'Global Innovations', 'contact@globalinnovations.tn', '+216 55 666 777'),
(3, 'EduTech Solutions', 'support@edutech.tn', '+216 99 888 777');

INSERT INTO deal (id, title, description, partner_id, start_date, end_date) VALUES
(1, 'Summer Tech Promo', 'Huge discounts on technology certifications', 1, '2026-06-01', '2026-08-31'),
(2, 'B2B Innovation Package', 'Special enterprise bundle for startup accelerators', 2, '2026-01-01', '2026-12-31'),
(3, 'Student Excellence Program', 'Discounted certification paths for students', 3, '2026-09-01', '2027-06-30');

INSERT INTO pack (id, name, description, validity_months, active) VALUES
(1, 'Starter Pack', 'Basic access to foundational courses', 6, true),
(2, 'Pro Pack', 'Full access including advanced lab environments', 12, true),
(3, 'Enterprise Ultimate', 'Unlimited access with priority 24/7 support', 24, true);

INSERT INTO access_code (id, code, partner_id, deal_id, expiration_date, used) VALUES
(1, 'TECH-SUMMER-01', 1, 1, '2026-08-31', false),
(2, 'TECH-SUMMER-02', 1, 1, '2026-08-31', true),
(3, 'GLOBAL-B2B-A1', 2, 2, '2026-12-31', false),
(4, 'GLOBAL-B2B-B2', 2, 2, '2026-12-31', false),
(5, 'EDU-STUDENT-99', 3, 3, '2027-06-30', false);
