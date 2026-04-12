# Fix: Add product – foreign key constraint error

If you see this error when adding a product from the admin:

```
Cannot add or update a child row: a foreign key constraint fails
('gestionshop'.'product', CONSTRAINT 'fk_product_formation' FOREIGN KEY ('formation_id') REFERENCES formation ('id'))
```

**Cause:** The shop database has an old foreign key from `product.formation_id` to a local `formation` table. The shop is not supposed to have a Formation table; formations are in **formation-service**. The shop only stores the formation ID and title snapshot.

**Fix:** Run the following SQL once on your **Gestionshop** (or gestionshop) database.

## Option 1: MySQL command line

```bash
mysql -u root -p
```

Then:

```sql
USE Gestionshop;   -- or: USE gestionshop; (depending on your DB name)

ALTER TABLE product DROP FOREIGN KEY fk_product_formation;
```

## Option 2: Run the script file

From the project root (or from `gestion shop/src/main/resources/db/`):

```bash
mysql -u root -p Gestionshop < src/main/resources/db/drop-product-formation-fk.sql
```

(Adjust the path and database name if needed.)

After dropping the constraint, add product again from the frontend; it should work (shop will validate the formation ID with formation-service and store only the ID + snapshot).
