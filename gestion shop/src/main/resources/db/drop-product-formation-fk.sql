-- Fix: Shop must NOT have a foreign key from product to a local formation table.
-- Formations live in formation-service; product.formation_id is just a reference (Long).
-- Run this once on the Gestionshop database if you get:
--   "Cannot add or update a child row: a foreign key constraint fails
--    ('gestionshop'.'product', CONSTRAINT 'fk_product_formation' ...)"

-- Drop the foreign key if it exists (MySQL)
ALTER TABLE product DROP FOREIGN KEY fk_product_formation;
