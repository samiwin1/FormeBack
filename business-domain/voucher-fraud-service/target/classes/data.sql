INSERT INTO fraud_alerts (id, voucher_code, partner_id, alert_type, severity_level, detection_details, detected_at, status) VALUES
(1, 'GLOBAL-B2B-A1', 2, 'MULTIPLE_REDEMPTION_ATTEMPT', 4, 'Multiple redemption attempts from different IP addresses within 10 minutes.', '2026-04-12 15:30:00', 'INVESTIGATION_PENDING'),
(2, 'TECH-SUMMER-01', 1, 'BRUTE_FORCE', 5, '30 failed attempts to guess voucher code suffix for Tech Summer promo.', '2026-04-13 09:15:00', 'CONFIRMED'),
(3, 'EDU-STUDENT-99', 3, 'DUPLICATE_USAGE', 2, 'Same student ID attempted to use the same voucher code twice.', '2026-04-11 11:20:00', 'DISMISSED'),
(4, 'GLOBAL-B2B-B2', 2, 'UNAUTHORIZED_TRANSFER', 3, 'Voucher code assigned to corporate email was verified from a public email domain.', '2026-04-14 08:00:00', 'INVESTIGATION_PENDING');
