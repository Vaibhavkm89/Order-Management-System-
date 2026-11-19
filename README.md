# Project 1 - Order Management System (Java JDBC + MySQL)

## Requirements
- JDK 17+
- MySQL Server and IntelliJ IDEA

## Setup
1. Open MySQL Workbench or MySQL command line.
2. Run `database.sql` to create `ecommerce_db`.
3. Set the `MYSQL_PASSWORD` environment variable to your MySQL root password. If your MySQL account has no password, set it to an empty string.
4. Open the `OrderManagementJDBC` folder in IntelliJ.
5. Load the Maven project and wait for MySQL Connector/J to download.
6. Run `OrderManagement.java`.

The application reads the database password from `MYSQL_PASSWORD`; credentials should not be stored in source code.

The application creates the `Orders` table automatically if it is missing.

## Testing
Choose option 1 to insert three sample rows:
- 101 Aarav Keyboard quantity 3 price 799 Pending
- 102 Diya Mouse quantity 2 price 499 Shipped
- 103 Kabir Monitor quantity 4 price 8999 Cancelled

Then:
1. Run option 1 again: duplicate IDs should be skipped.
2. Option 2, enter `Pending`: order 101 should appear (quantity > 2).
3. Option 3, enter ID `102`, extra quantity `3`: quantity becomes 5.
4. Option 4, enter ID `101`, new status `Shipped`: updated details appear.
5. Option 5: displays `SUM(quantity * unit_price)`.
6. Option 6, enter ID `103`: it is deleted because its status is Cancelled.
7. Try deleting ID `101`: deletion is refused unless its status is Cancelled.

## Main concepts
`Connection`, `PreparedStatement`, `ResultSet`, `executeUpdate()`, `executeQuery()`, primary key duplicate handling, try-with-resources, and SQL aggregate `SUM`.
