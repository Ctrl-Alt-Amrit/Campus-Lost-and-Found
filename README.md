# Campus Lost and Found System

A simple JavaFX desktop application using Oracle Database and JDBC for reporting, searching, and claiming lost or found items on campus.

## Project Structure

```text
LostFoundApp/
├── database/
│   └── schema.sql
├── images/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── lostfound/
        │           ├── Main.java
        │           ├── controller/
        │           ├── dao/
        │           ├── model/
        │           ├── service/
        │           └── util/
        └── resources/
            ├── config/
            │   └── db.properties
            ├── styles/
            │   └── style.css
            └── view/
                ├── dashboard.fxml
                ├── detail.fxml
                ├── login.fxml
                ├── report.fxml
                └── search.fxml
```

## Features

- User registration and login with SHA-256 hashed passwords
- Report lost and found items
- Upload images to a local `images/` folder and store the relative path in Oracle
- Search items by title or description
- View item details with image preview
- Claim found items
- Admin panel for report review, claim approval/rejection, resolving items, removing invalid entries, and user management
- Dashboard with summary counts

## Prerequisites

- Java 17 or later
- Maven 3.9 or later
- Oracle Database 10g Express Edition or newer

## Database Setup

1. For a fresh database, run the SQL script in `database/schema.sql`.
2. For an existing database created from the older schema, run `database/admin_upgrade.sql` once.
3. Update `src/main/resources/config/db.properties` with your Oracle connection values.

Example connection:

```properties
db.url=jdbc:oracle:thin:@localhost:1521:XE
db.user=campus_lost_found
db.password=your_password
db.driver=oracle.jdbc.OracleDriver
```

## Run Steps

1. Install dependencies:

```bash
mvn clean install
```

2. Start the JavaFX application:

```bash
mvn javafx:run
```

## Default Accounts

The SQL script inserts one admin account and one student account. Their plain-text password is:

```text
Password123
```

```text
admin@campus.edu
student@campus.edu
```

## Notes

- Uploaded images are stored in the project-level `images/` folder.
- The app uses MVC with separate model, DAO, service, controller, and util layers.
- If you open FXML files in Scene Builder, the layouts are ready for visual editing.
