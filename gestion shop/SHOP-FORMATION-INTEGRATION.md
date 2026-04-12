# Shop ↔ Formation service integration

Use this document when integrating the shop (gestion shop / shop-service) with the formation-service or when changing product/cart/order logic.

---

## Prompt for AI / context

When working on integration, you can paste this:

```
The shop backend (gestion shop / shop-service) does NOT store Formation entities. Formations are managed by a separate formation-service (Eureka: formation-service, port 8083). The shop only stores formationId (Long) and formationTitleSnapshot (String) on Product, CartItem, and OrderItem. To get or validate formation data, the shop calls formation-service via FormationClient (GET http://formation-service/api/formations/{id}). Product = one formation (by id) + price + currency; creation/update validates formationId with formation-service and sets formationTitleSnapshot. Read SHOP-FORMATION-INTEGRATION.md in this project for full details.
```

---

## 1. Architecture

- **formation-service** (external microservice): Owns all **Formation** data. Exposes REST at `/api/formations` (create, read, update, delete formations). Registers in Eureka as `formation-service`, runs on port **8083**.
- **shop-service** (this app, "gestion shop"): Sells **Products**. A product is a **sellable course**: it references one formation by ID and adds **price** and **currency**. Registers as `shop-service`, runs on port **8084**.
- **api-gateway** (port **8082**): Routes `/api/formations/**` to **formation-service**. Routes `/api/shop/**`, `/api/payments/**` to **shop-service**.

**Important:** Formations are **not** stored in the shop DB. The shop only stores:
- `formationId` (Long) — ID of the formation in formation-service.
- `formationTitleSnapshot` (String) — Copy of the formation title at product/cart/order creation, for display and history.

---

## 2. How the shop uses Formation

| Concept | Where | Meaning |
|--------|--------|--------|
| **Product** | shop-service | One product = one formation (by `formationId`) + price + currency. Has `formationId` and `formationTitleSnapshot`. When creating/updating a product, shop calls formation-service to validate the ID and get the title for the snapshot. |
| **CartItem** | shop-service | References a Product; stores `formationId` and `formationTitleSnapshot` (filled from the product when adding to cart). |
| **OrderItem** | shop-service | Same: `formationId` + `formationTitleSnapshot` (copied from cart at checkout). |

There is **no** Formation entity, repository, or table in shop-service. Do not add a Formation JPA entity here.

---

## 3. Calling formation-service from shop-service

- **Client:** `tn.esprit.shop.shopservice.client.FormationClient`
- **Method:** `FormationResponse getFormationById(Long id)` — calls `GET http://formation-service/api/formations/{id}` via Eureka.
- **When:** ProductService uses it when adding or updating a product: it checks the formation exists and sets `formationTitleSnapshot` from the response.
- **DTO:** `FormationResponse` (id, title, description, category, level) — only fields needed for validation and snapshot.

---

## 4. Typical flows

1. **Create a formation**  
   Call formation-service (via gateway): `POST http://localhost:8082/api/formations` with formation body. No shop involvement.

2. **Create a product (course in shop)**  
   Call shop-service: e.g. `POST .../product/addProduct` with body containing `formationId` (and price, currency, etc.). Shop calls formation-service to validate `formationId` and set `formationTitleSnapshot`. Product is stored with `formationId` + `formationTitleSnapshot`.

3. **Add to cart**  
   Client sends CartItem with product (and quantity, etc.). CartItemService sets `formationId` and `formationTitleSnapshot` from the product if missing.

4. **Checkout**  
   Order items are created from cart items; each OrderItem gets `formationId` and `formationTitleSnapshot` from the corresponding CartItem.

5. **List formations (e.g. for dropdown when creating product)**  
   Call formation-service via gateway: `GET http://localhost:8082/api/formations`. Do not call shop for the list of formations.

---

## 5. API Gateway (relevant routes)

- `Path=/api/formations/**` → **formation-service** (no path rewrite).
- `Path=/api/shop/**` → **shop-service** (rewrite to `/gestionshop/...`).

Formation CRUD and listing go to formation-service. Product/cart/order go to shop-service.

---

## 6. Ports and Eureka

| Service | Port | Eureka name |
|---------|------|-------------|
| api-gateway | 8082 | api-gateway |
| formation-service | 8083 | formation-service |
| shop-service (gestion shop) | 8084 | shop-service |

Formation-service must be running and registered in Eureka for product create/update (and thus formation validation) to work.

---

## 7. Summary for an AI doing integration

- Formations live **only** in **formation-service**. Shop has **no** Formation entity.
- Shop stores **formationId** + **formationTitleSnapshot** on Product, CartItem, and OrderItem.
- To validate or get formation data from shop, use **FormationClient** and call formation-service.
- Formation list/create/update/delete → use formation-service (via gateway). Product/cart/order → use shop-service.
