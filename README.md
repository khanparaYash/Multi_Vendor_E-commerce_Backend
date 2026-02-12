# 🛍️ Spring Boot Multi-Vendor E-commerce Backend

A robust and scalable backend for a Multi-Vendor E-commerce marketplace built with **Spring Boot**. This application supports comprehensive e-commerce workflows including multi-role user management (Admin, Vendor, Customer), secure authentication, product catalog, shopping cart, order processing, payments, reviews, and vendor-specific features.

## 🚀 Features

### 🔐 User & Security
*   **Role-Based Access Control (RBAC):**
    *   **Admin:** Platform oversight, vendor approval, order management.
    *   **Vendor:** Product management, order fulfillment, wallet tracking.
    *   **Customer:** Shopping, ordering, payments, reviews.
*   **Secure Authentication:** JWT (JSON Web Token) based stateless authentication.

### 🛒 Customer Features
*   **Shopping Cart:** Add, update, and remove items with real-time total calculation.
*   **Coupons:** Apply discount codes to carts.
*   **Order Management:** Place orders, view order history, and track status.
*   **Payments:** Integrated payment processing flow (Simulation).
*   **Reviews & Ratings:** Leave feedback for purchased products.

### 🏪 Vendor Features
*   **Onboarding:** Self-registration with Admin approval workflow.
*   **Product Management:** Full CRUD capabilities for managing inventory.
*   **Order Fulfillment:** View assigned orders, mark as Shipped or Delivered.
*   **Wallet:** Track earnings from sales.
*   **Profile Management:** Update store details.

### 🛡️ Admin Features
*   **Vendor Governance:** Review and approve/reject new vendor registrations.
*   **Platform Orders:** View and manage all orders across the platform; ability to cancel orders.
*   **Coupon Management:** Create and manage promotional codes.

## 🏗️ Engineering Highlights

This project demonstrates **System Design** principles and **Clean Architecture**:

*   **Concurrency Control**: Implements **Pessimistic Locking** (`PESSIMISTIC_WRITE`) on product inventory during order creation. This ensures data integrity and prevents overselling even under high contention (e.g., 200 users vying for 10 items).
*   **Transactional Integrity**: Uses `@Transactional` boundaries to ensure that Order creation, Stock deduction, and Cart clearing happen atomically. If any step fails, the entire transaction rolls back.
*   **Security Enforcement**:
    *   **RBAC**: Fine-grained `hasRole()` and `hasAuthority()` checks at the controller level.
    *   **Stateful to Stateless**: Migrated from session-based to **JWT** (JSON Web Token) authentication for scalability.
*   **Clean Architecture**:
    *   **Separation of Concerns**: Controller -> Service -> Repository layers.
    *   **DTO Pattern**: Exposes strictly typed `DTOs` via APIs, hiding internal JPA `Entities` to prevent over-fetching and accidental exposure of sensitive data.
*   **Performance Optimization**:
    *   **Database Indexing**: Strategic indexes on frequent lookup columns (e.g., `vendor_id`, `status`).
    *   **Efficient Querying**: Uses JPQL and optimized repository methods to fetch only necessary data.

---

## 🛠️ Tech Stack

*   **Backend Framework:** Spring Boot (Web, Data JPA, Security, Validation)
*   **Language:** Java 17
*   **Database:** PostgreSQL
*   **Authentication:** Spring Security with JWT (jjwt 0.12.6)
*   **Build Tool:** Maven

---

## 📦 Setup & Installation

### Prerequisites
*   Java 17 Development Kit (JDK)
*   Maven 3.8+
*   PostgreSQL Database

### Steps

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/your-username/ecommerce-backend.git
    cd ecommerce-backend
    ```

2.  **Configure Database**
    Create a PostgreSQL database named `ecommerce`.
    Update `src/main/resources/application.properties` with your credentials:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce
    spring.datasource.username=postgres
    spring.datasource.password=your_password
    ```

3.  **Build the Application**
    ```bash
    ./mvnw clean install
    ```

4.  **Run the Application**
    ```bash
    ./mvnw spring-boot:run
    ```
    The application will start on `http://localhost:8080`.

---

## 📡 API Documentation

### 🔓 Public / Authentication
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/register` | Register a new user (Customer) or Vendor (via specific DTO) |
| `POST` | `/login` | Authenticate and obtain JWT token |

### 👤 Customer Endpoints
_Requires `Bearer Token` with `CUSTOMER` role_

**Cart & Ordering**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/customer/cart` | View current cart |
| `POST` | `/customer/cart/add` | Add item into cart |
| `PUT` | `/customer/cart/update` | Update item quantity |
| `DELETE` | `/customer/cart/remove/{productId}` | Remove item from cart |
| `POST` | `/customer/cart/apply-coupon` | Apply a coupon code (`?code=...`) |
| `POST` | `/customer/order/create` | Place an order from current cart |
| `GET` | `/customer/orders` | View order history |
| `GET` | `/customer/order/{id}` | Get specific order details |
| `POST` | `/customer/payment/{orderId}` | Process payment for an order |

**Other**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/customer/review` | Submit a product review |

### 🏪 Vendor Endpoints
_Requires `Bearer Token` with `VENDOR` role_

**Account & Products**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/vendor/register` | Register as a new Vendor |
| `GET` | `/vendor/profile` | Get vendor profile details |
| `PUT` | `/vendor/update` | Update vendor profile |
| `GET` | `/vendor/wallet` | View earnings/wallet balance |
| `GET` | `/vendor/my-products` | List all owned products |
| `POST` | `/vendor/products` | Add a new product |
| `PUT` | `/vendor/products/{id}` | Update an existing product |
| `DELETE` | `/vendor/products/{id}` | Delete a product |

**Order Fulfillment**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/vendor/orders` | View orders containing your products |
| `PUT` | `/vendor/orders/{id}/ship` | Mark order as Shipped |
| `PUT` | `/vendor/orders/{id}/deliver` | Mark order as Delivered |

### 🛡️ Admin Endpoints
_Requires `Bearer Token` with `ADMIN` role_

**Management**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/admin/orders` | View all platform orders |
| `PUT` | `/admin/orders/{id}/cancel` | Cancel an order |
| `POST` | `/admin/coupons` | Create a new coupon |
| `GET` | `/admin/coupons` | List all coupons |
| `DELETE` | `/admin/coupons/{id}` | Delete a coupon |

**Vendor Approvals**
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/admin/vendors/pending` | List vendors awaiting approval |
| `PUT` | `/admin/vendors/{id}/approve` | Approve a vendor account |
| `PUT` | `/admin/vendors/{id}/reject` | Reject a vendor account |

---

## 💾 Database Schema

The application uses JPA entities to map to the following tables (auto-generated):
- `users`: Stores user credentials and roles.
- `products`: Product details linked to Vendors.
- `carts` / `cart_items`: Temporary storage for customer items.
- `orders` / `order_items`: Finalized transactions.
- `coupons`: Discount codes.
- `reviews`: Product feedback.
- `wallets`: Vendor earnings.

---

## 🤝 Contributing

1.  Fork the repository.
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
