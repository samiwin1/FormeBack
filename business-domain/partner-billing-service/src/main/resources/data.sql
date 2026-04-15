INSERT INTO partner_invoices (id, partner_id, invoice_number, amount, period_start, period_end, status, details, issued_at, paid_at) VALUES
(1, 1, 'INV-20260401', 1250.50, '2026-03-01', '2026-03-31', 'PAID', 'Tech Corp Monthly Commission for Q1', '2026-04-01 09:00:00', '2026-04-05 14:00:00'),
(2, 2, 'INV-20260402', 3400.00, '2026-03-01', '2026-03-31', 'PENDING', 'Global Innovations Enterprise Package Billing', '2026-04-01 09:05:00', NULL),
(3, 3, 'INV-20260403', 850.75, '2026-03-01', '2026-03-31', 'OVERDUE', 'EduTech Solutions Monthly Assessment Fees', '2026-04-01 09:10:00', NULL),
(4, 1, 'INV-20260301', 1100.00, '2026-02-01', '2026-02-28', 'PAID', 'Tech Corp Monthly Commission for Feb', '2026-03-01 09:00:00', '2026-03-10 10:30:00');
