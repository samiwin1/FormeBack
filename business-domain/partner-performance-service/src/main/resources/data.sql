INSERT IGNORE INTO performance_alerts (id, partner_id, type, severity, message, status, created_at) VALUES
(1, 1, 'LOW_REDEMPTION', 'MEDIUM', 'Partner Tech Corp has 15% voucher redemption rate this month vs 40% average.', 'OPEN', '2026-04-10T10:00:00'),
(2, 2, 'NO_SALES', 'HIGH', 'Global Innovations has zero deals initiated in the last 60 days.', 'OPEN', '2026-04-12T09:30:00'),
(3, 3, 'TARGET_MET', 'LOW', 'EduTech Solutions reached their quarterly sales target early.', 'RESOLVED', '2026-03-25T14:00:00');
