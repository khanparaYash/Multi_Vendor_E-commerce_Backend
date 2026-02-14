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

## 🔄 Application Workflows

### 1. Vendor Onboarding Flow

#### Step 1: Registration
*   **Action**: User signs up as a Vendor.
*   **API**: `POST /register` (with role `VENDOR`)
*   **Backend Logic**:
    *   Creates a `User` record with role `VENDOR`.
    *   Creates a `Vendor` profile linked to the user.
    *   Sets Vendor Status to `PENDING`.
*   **Outcome**: User can login but cannot manage products yet.

#### Step 2: Admin Approval
*   **Action**: Admin reviews pending vendor applications.
*   **API**: `PUT /admin/vendors/{id}/approve`
*   **Pre-conditions**: Authenticated as `ADMIN`.
*   **Backend Logic**:
    *   Updates Vendor Status to `APPROVED`.
    *   Enables the user account if it was locked.
*   **Outcome**: Vendor gains access to product management dashboards.

#### Step 3: Product Listing
*   **Action**: Vendor adds products to the marketplace.
*   **API**: `POST /vendor/products`
*   **Pre-conditions**: Vendor Status must be `APPROVED`.
*   **Backend Logic**:
    *   Validates product details (price, stock).
    *   Links product to the specific Vendor.
*   **Outcome**: Product becomes visible in the marketplace.

### 2. Order Fulfillment Lifecycle

#### Step 1: Shopping & Checkout
*   **Action**: Customer adds items to cart and places order.
*   **API**: `POST /customer/order/create`
*   **Backend Logic**:
    *   Validates stock availability in **Redis**.
    *   **Locking**: Acquires a lock on product/stock to prevent race conditions.
    *   **Reservation**: Decrements stock in Redis and DB.
    *   Creates `Order` with status `CREATED`.
    *   Clears the user's Cart.
*   **Outcome**: Order created, stock reserved.

#### Step 2: Payment (See Detailed Payment Workflow below)
*   **Action**: Customer pays for the order.
*   **Outcome**: Order Status becomes `PAID`.

#### Step 3: Shipping
*   **Action**: Vendor views new paid orders and ships them.
*   **API**: `PUT /vendor/orders/{id}/ship`
*   **Pre-conditions**:
    *   Order Status must be `PAID`.
    *   Vendor must own the products in the order.
*   **Backend Logic**:
    *   Updates Order Status to `SHIPPED`.
    *   (Optional) Triggers email notification to Customer.

#### Step 4: Delivery
*   **Action**: Vendor or Logistics Partner marks order as delivered.
*   **API**: `PUT /vendor/orders/{id}/deliver`
*   **Backend Logic**:
    *   Updates Order Status to `DELIVERED`.
    *   **Wallet Update**: Credits the Vendor's wallet with the sale amount (platform fees logic can be added here).
*   **Outcome**: Transaction complete. User can now leave a review.

### 3. Payment Workflow (Stripe Integration)
The system uses **Stripe PaymentIntents** and **Webhooks** for secure payment processing.

#### Step 1: User Initiates Payment
*   **Action**: Customer clicks "Pay Now" for an Order.
*   **API**: `POST /customer/payment/{orderId}`
*   **Pre-conditions**:
    *   User must be authenticated (`CUSTOMER` role).
    *   Order must exist and belong to the user.
    *   Order Status must be `CREATED` or `PAYMENT_PENDING`.
*   **Backend Logic**:
    1.  Calculates total order amount.
    2.  Creates a **Stripe PaymentIntent** via API.
    3.  Attaches **Metadata**:
        *   `order_id`: To link the payment to the order.
        *   `app_name`: To identify the application (for multi-project support).
    4.  Creates a local `Payment` record with status `PENDING`.
    5.  Updates Order Status to `PAYMENT_PENDING`.
*   **Response**: Returns `clientSecret` and `paymentIntentId`.

#### Step 2: Frontend Processing
*   **Action**: Frontend uses the `clientSecret` with **Stripe.js**.
*   **Logic**: Calls `stripe.confirmCardPayment(clientSecret)` to securely handle card details.
*   **Outcome**: Stripe processes the payment.

#### Step 3: Webhook Verification
*   **Event**: Stripe sends a webhook event to the backend.
*   **API**: `POST /stripe/webhook` (Public Endpoint)
*   **Security**:
    1.  **Signature Verification**: Backend verifies `Stripe-Signature` header using the configured `stripe.webhook.secret`.
    2.  **App Verification**: Backend checks `app.name` metadata. If it doesn't match the configured `app.name`, the event is ignored (allowing multiple apps to use the same Stripe account).

#### Step 4: Completion & Status Updates
*   **Scenario A: Payment Succeeded (`payment_intent.succeeded`)**
    *   Finds `Payment` record by `paymentIntentId`.
    *   **Check**: If payment is already `SUCCESS`, logs and skips (idempotency).
    *   Updates `Payment` Status to `SUCCESS`.
    *   Updates `Order` Status to **`PAID`**.
    *   Logs success.
*   **Scenario B: Payment Failed (`payment_intent.payment_failed`)**
    *   Updates `Payment` Status to `FAILED`.
    *   Order Status remains `PAYMENT_PENDING` (user can retry with a different card).
    *   Logs failure.




## 🏗️ Engineering Highlights

This project demonstrates **System Design** principles and **Clean Architecture**:

*   **Concurrency Control**: Uses **Redis** for high-performance stock reservation combined with **Pessimistic Locking** (`PESSIMISTIC_WRITE`) on the database. This dual-layer approach ensures data integrity and prevents overselling under extreme load.
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
*   **Caching:** Redis & Spring Cache
*   **Build Tool:** Maven


---

## 📦 Setup & Installation

### Prerequisites
*   Java 17 Development Kit (JDK)
*   Maven 3.8+
*   PostgreSQL Database
*   **Stripe Account** (Secret Key & Webhook Secret)

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
    
    # Stripe Configuration
    stripe.secret.key=sk_test_...
    stripe.webhook.secret=whsec_...
    app.name=ecommerce
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

5.  **Default Data (Data Seeder)**
    The application automatically seeds the database with sample users and products on startup if they don't exist.

    **Default Credentials:**
    *   **Admin:** `admin@example.com` / `admin123`
    *   **Vendor:** `vendor@example.com` / `vendor123`
    *   **Customer:** `customer@example.com` / `customer123`

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
