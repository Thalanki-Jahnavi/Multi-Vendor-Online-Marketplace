# OneCart – Multi-Vendor Online Marketplace

OneCart is a web-based multi-vendor e-commerce platform that enables multiple vendors to sell their products through a single marketplace. Customers can browse products, purchase items from different vendors, and manage their orders. Vendors can manage their own stores, products, and sales, while administrators oversee the entire platform.

The application is being developed using Spring Boot, MySQL, Thymeleaf, HTML, CSS, and JavaScript.

## Table of Contents

- Project Overview
- Objectives
- Features
- User Roles
- Technology Stack
- System Architecture
- Project Structure
- Database Design
- Installation and Setup
- Development Roadmap
- Future Enhancements

## Project Objectives

- Develop a centralized online marketplace for multiple vendors.
- Provide a secure and convenient shopping experience for customers.
- Allow vendors to manage their stores and products independently.
- Implement role-based authentication and authorization.
- Provide administrators with complete marketplace management capabilities.
- Build a responsive and user-friendly interface.

## Features

### 1. Guest

Guests can access the following public pages:

- Home
- Sign Up
- Login
- About Us
- Contact Us

Guests can browse publicly available products and vendor stores on the homepage. Authentication is required to access restricted shopping features.

### 2. Customer

- Customer registration and login.
- Personalized customer dashboard.
- Product browsing, searching, and filtering.
- Product details and vendor store information.
- Shopping cart and wishlist management.
- Checkout and order placement.
- Order history and order tracking.
- Profile and address management.

### 3. Vendor

- Vendor registration and login.
- Vendor dashboard.
- Store profile management.
- Product creation, editing, and deletion.
- Inventory and stock management.
- Order management.
- Sales and earnings overview.
- Vendor profile management.

Vendors can manage only their own stores, products, and orders.

### 4. Admin

- Administrative dashboard.
- Customer management.
- Vendor management and approval.
- Product and category management.
- Order and payment management.
- Marketplace monitoring and reporting.
- Platform settings.

Administrators have access to all administrative features, subject to the application's security rules.

## Technology Stack

| Component | Technology |
|---|---|
| Backend | Java, Spring Boot |
| Frontend | HTML, CSS, JavaScript, Thymeleaf |
| Database | MySQL |
| Security | Spring Security |
| ORM | Hibernate |
| Data Access | Spring Data JPA |
| Build Tool | Maven |
| Development IDE | Spring Tool Suite (STS) |

## System Architecture

OneCart follows a layered Spring Boot MVC architecture.

```text
Presentation Layer
HTML, CSS, JavaScript, Thymeleaf
             |
             v
Controller Layer
             |
             v
Service Layer
             |
             v
Repository Layer
Spring Data JPA
             |
             v
Database Layer
MySQL
```

### Architecture Components

- Controller Layer: Handles HTTP requests and responses.
- Service Layer: Implements business logic and validation.
- Repository Layer: Performs database operations.
- Entity Layer: Maps Java objects to database tables.
- DTO Layer: Transfers data between application layers.
- Security Layer: Handles authentication, authorization, and access control.
- Presentation Layer: Renders responsive UI screens using Thymeleaf templates.

## Project Structure

```text
OneCart/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/vendor/
│       │       ├── config/
│       │       ├── controller/
│       │       ├── dto/
│       │       ├── entity/
│       │       ├── repository/
│       │       ├── service/
│       │       └── MultiVendorECommerceApplication.java
│       │
│       └── resources/
│           ├── templates/
│           │   ├── public/
│           │   ├── customer/
│           │   ├── vendor/
│           │   └── admin/
│           │
│           ├── static/
│           │   ├── css/
│           │   ├── js/
│           │   └── images/
│           │
│           └── application.properties/
│
├── pom.xml
└── README.md
```

## Database Design

OneCart uses MySQL as its relational database.

The existing database schema is the source of truth for entity mapping and application development. The following are the conceptual data areas supported by the marketplace:

| Data Area | Purpose |
|---|---|
| User and Role | Account registration and authentication |
| Vendor and Store | Vendor profiles and store information |
| Product and Category | Product catalog and categorization |
| Cart and Wishlist | Customer shopping preferences |
| Order and Order Item | Purchase and order management |
| Payment | Payment records |
| Address and Shipment | Delivery information |
| Review and Rating | Customer feedback |

The final entity relationships and database tables will be based on the existing MySQL schema.

## Installation and Setup

### Prerequisites

- Java JDK compatible with the selected Spring Boot version.
- Spring Tool Suite (STS).
- MySQL Server.
- Maven.

### 1. Clone the repository

```bash
git clone <your-repository-url>
cd OneCart
```

### 2. Create the database

Create the MySQL database and import the existing OneCart schema.

```sql
CREATE DATABASE onecart;
```

### 3. Configure database connection

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/onecart
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
```

Replace the database name and credentials with your actual values. Ensure that the existing schema matches the application's JPA entities before starting the application.

### 4. Run the application

1. Open the project in Spring Tool Suite.
2. Update the Maven dependencies.
3. Verify the MySQL connection.
4. Run `MultiVendorECommerceApplication.java` as a Spring Boot application.

The application will be available at:

```text
http://localhost:8080
```

## Development Roadmap

| Phase | Development Task |
|---|---|
| 1 | Project setup, architecture, and database integration |
| 2 | Homepage and public pages |
| 3 | Customer and vendor registration and login |
| 4 | Customer dashboard and shopping features |
| 5 | Vendor dashboard and store management |
| 6 | Admin dashboard and marketplace management |
| 7 | Integration testing, security testing, and bug fixing |

Each screen is developed with its frontend, backend, and database integration before moving to the next feature.

## Security and Access Control

OneCart uses Spring Security to protect restricted pages and enforce role-based authorization.

- Guests can access only permitted public pages.
- Customers can access their own shopping features and dashboard.
- Vendors can manage only their own stores and products.
- Administrators can access administrative management features.
- Passwords are stored using secure password hashing.
- Restricted routes are protected against unauthorized access.

## Future Enhancements

- Online payment gateway integration.
- Product reviews and ratings.
- Email notifications for order updates.
- Advanced product search and filtering.
- Sales analytics and reporting.
- Product recommendations.
- Improved inventory and order tracking.

## Project Status

OneCart is currently under development. The application is being built incrementally, beginning with the project architecture, database integration, homepage, and authentication system.

The goal is to deliver a complete, responsive, and secure multi-vendor marketplace with separate customer, vendor, and admin experiences.

## License

This project is developed for educational and portfolio purposes. A formal license can be added when the project is published.

