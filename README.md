# Hibernate E-Commerce System

Hibernate-based Java application that manages an e-commerce system:
categories, products, users, orders and order details.

## Technologies Used

- Java 21
- Hibernate ORM 6.3.1.Final (JPA annotations)
- MySQL 8 (runtime), H2 in-memory (tests only)
- Maven
- JUnit 5

## Entities & Relationships

| Entity | Key Fields | Relationships |
|---|---|---|
| `Category` | id, name (unique, not null), description | One-to-Many → `Product` |
| `Product` | id, name (not null), price (decimal, not null), stockQuantity, deleted (soft-delete flag) | Many-to-One → `Category` |
| `Users` | id, username (unique), password (hashed), email (unique), role (ADMIN/CUSTOMER) | One-to-Many → `Orders` |
| `Orders` | id, orderDate (not null), totalAmount (decimal, not null) | Many-to-One → `Users`, One-to-Many → `OrderDetails` |
| `OrderDetails` | id, quantity, unitPrice (decimal) | Many-to-One → `Orders`, Many-to-One → `Product` |

Cascading: `Category → Product`, `Users → Orders` and `Orders → OrderDetails`
all use `CascadeType.ALL`; all associations use lazy fetching.

## Prerequisites

- JDK 21
- Maven 3.9+
- MySQL 8 running on `localhost:3306`

## Setup

1. Create the database (the schema itself is generated automatically by
   Hibernate via `hibernate.hbm2ddl.auto=update`; `schema.sql` documents the
   structure and can also be applied manually):

   ```sql
   CREATE DATABASE ecommerce_db;
   ```

2. Provide the database password as an environment variable (or system
   property) — it is referenced from `hibernate.cfg.xml` as `${DB_PASSWORD}`:

   ```bash
   export DB_PASSWORD=your_mysql_root_password
   ```

## Running the Demo Application

```bash
mvn exec:java
# or run in-memory without MySQL:
mvn exec:java -Dhibernate.config=hibernate-h2.cfg.xml
```

If MySQL is not running on `localhost:3306`, `StoreApp` automatically falls back to the in-memory H2 configuration (`hibernate-h2.cfg.xml`).

The demo (`StoreApp`) runs in this order:

1. `AddCategory` — inserts the *Electronics* category (skips if it exists)
2. `AddProduct` — inserts two products into it (skips if they exist)
3. `AddUsers` — inserts a customer with a SHA-256 hashed password
4. `AddOrder` — places an order with two order details for that customer
5. `FetchCategory` — lists all categories
6. `FetchOrder` — loads the latest order together with its user and products
7. `FindProductsByCriteria` — CriteriaBuilder query for active products in a
   price range (between 50000 and 100000)
8. `FetchProductsByPage` — paginates the active product listing
   (`setFirstResult`/`setMaxResults`, 2 per page)
9. `SoftDeleteProduct` — soft-deletes *Dell XPS 15* by setting its
   `deleted` flag to `true` instead of removing the row

The demo is idempotent: it can be run again without constraint violations.
On subsequent runs the soft-deleted product is skipped (already flagged) and
no longer appears in the criteria/pagination listings.
`ModifyCategory` (merge-based update) is available in `crud/` and can be
invoked from `main` to demonstrate an UPDATE.

## Running the Tests

The tests use an in-memory H2 database, so no MySQL setup is needed:

```bash
mvn test
```

`CrudOperationsTest` covers: insert (Category / Product / User with hashed
password), creating an order with multiple order details, fetching an order
together with its user and products, updating via `merge`, delete with
cascade, the `Product.byCategoryName` named query, a CriteriaBuilder query,
soft delete via the `deleted` flag, and pagination with
`setFirstResult`/`setMaxResults`.

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/code/HibernateProject/
│   │       ├── crud/            # Add/Fetch/Modify/soft-delete operations
│   │       ├── entity/          # JPA entities
│   │       ├── util/            # PasswordHasher
│   │       ├── HibernateUtil.java
│   │       └── StoreApp.java    # main entry point
│   └── resources/
│       ├── hibernate.cfg.xml    # MySQL configuration
│       └── hibernate-h2.cfg.xml # H2 in-memory configuration (demo fallback)
└── test/
    ├── java/
    │   └── com/code/HibernateProject/
    │       └── CrudOperationsTest.java
    └── resources/
        └── hibernate-test.cfg.xml   # H2 in-memory configuration

pom.xml
schema.sql
```

## Notes

- Passwords are stored hashed (SHA-256 via `util.PasswordHasher`), never as
  plain text.
- Bonus features:
  - `Product.byCategoryName` named query (fetch products by category name)
  - Criteria query via `CriteriaBuilder` (`FindProductsByCriteria`)
  - Soft delete: `Product.deleted` flag set via `merge` instead of
    `session.remove()`; listings and criteria queries filter on
    `deleted = false`, while order history still resolves soft-deleted products
  - Pagination for product listings via `setFirstResult`/`setMaxResults`
    (`FetchProductsByPage`)
- Connection details (driver, URL, user) live in
  `src/main/resources/hibernate.cfg.xml`.
