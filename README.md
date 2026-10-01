# SMARTDINE — Smart Restaurant, Hotel & Canteen Platform
> **B.Tech Java Object-Oriented Programming (OOP) Academic Capstone Project**  
> Built with **Java 21**, **Spring Boot 3.2.4**, **Spring Data JPA**, **H2 / MySQL**, **HTML5 / Vanilla CSS / Modern JavaScript**, and **Leaflet / Google Maps**.

---

## 🌟 Key Architectural Innovations

### 1. Unified Establishment Hierarchy (Hotel & Canteen OOP Unification)
Instead of duplicating code across separate `Hotel` and `Canteen` classes, both are unified under a single `Establishment` entity with an `EstablishmentType` enum (`HOTEL` or `CANTEEN`).
- Eliminates code duplication across menus, tables, orders, reviews, and clearance discounts.
- Shares the same operational management dashboard while preserving business classification.

### 2. Dynamic Ambiance Theming & Food Recommendations
- Customers can select dining ambiances:
  - `Default` (White & Vibrant Orange)
  - `Candle Light Dinner` (Deep Red & Warm Amber)
  - `Romantic Dining` (Soft Rose Pink)
  - `Family Feast` (Warm Golden Amber)
  - `Friends Hangout` (Electric Sky Blue)
  - `Premium Luxury` (Brushed Gold & Obsidian Black)
  - `Casual Hangout` (Clean Orange)
  - `Outdoor Garden` (Botanical Forest Green)
  - `Birthday Celebration` (Festive Purple Glow)
- The UI theme dynamically re-styles the entire client experience without altering backend logic.
- The backend matches database food tags (`ROMANTIC`, `CANDLE_LIGHT`, `FAMILY`, `DESSERT`, `SPICY`, `DRINK`) to recommend mood-tailored dishes and matching establishments.

### 3. Fast Serve Express (At-Table Ordering)
- If a customer is already seated at a table, they don't need a reservation.
- Selecting **"I am already at the table"** prompts for the table number (e.g., Table `12`).
- Associating `Customer + Establishment + Table Number` routes the order directly to the kitchen pipeline as a priority Fast Serve ticket.

### 4. Smart Closing-Time Food Clearance Alert
- Prevents end-of-day food waste by allowing managers to trigger dynamic discount offers (e.g., **50% OFF**).
- Backend inventory rules enforce exact remaining quantities, prevent over-ordering, dynamically compute discounted prices, and automatically mark dishes as **SOLD OUT** when inventory reaches zero.

### 5. Verified Platform Financial Turnover
- Platform turnover is strictly calculated by summing **paid and completed orders** processed through SmartDine.
- Cancelled, failed, or unpaid orders are excluded from platform turnover analytics.

---

## 🎓 Complete OOP Implementation Matrix (For Project Viva)

| OOP Principle | Implementation Details | Relevant Source Files |
| :--- | :--- | :--- |
| **Encapsulation** | Private fields, validation invariants, guarded getters/setters, state encapsulation, protected constructors. | [`Person.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/Person.java)<br>[`FoodItem.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/FoodItem.java)<br>[`Order.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/Order.java)<br>[`Establishment.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/Establishment.java) |
| **Abstraction** | Abstract base classes (`Person`) declaring abstract contract methods (`getRoleDescription()`, `canPerformAdminActions()`). Clean service interfaces hiding repository details. | [`Person.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/Person.java)<br>[`Payment.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/payment/Payment.java)<br>[`OrderService.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/service/OrderService.java) |
| **Inheritance** | Joined-table entity inheritance hierarchy where `Customer`, `Admin`, and `EstablishmentOwner` extend `Person`. | [`Customer.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/Customer.java)<br>[`Admin.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/Admin.java)<br>[`EstablishmentOwner.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/EstablishmentOwner.java) |
| **Polymorphism** | Dynamic method dispatch via `Payment` interface implemented by `CashPayment` and `UPIPayment`. Overridden methods in subclasses. | [`Payment.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/payment/Payment.java)<br>[`CashPayment.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/payment/CashPayment.java)<br>[`UPIPayment.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/payment/UPIPayment.java)<br>[`PaymentServiceImpl.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/service/impl/PaymentServiceImpl.java) |
| **Composition** | `Establishment` has-a `List<FoodItem>` & `List<DiningTable>`; `Order` has-a `List<OrderItem>`; `Reservation` has-a `Establishment`, `DiningTable`, and `Customer`. | [`Establishment.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/Establishment.java)<br>[`Order.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/Order.java)<br>[`Reservation.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/model/Reservation.java) |
| **Custom Exceptions** | Explicit custom exception classes handled globally via `@RestControllerAdvice` returning structured JSON error bodies. | [`GlobalExceptionHandler.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/exception/GlobalExceptionHandler.java)<br>[`FoodNotAvailableException.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/exception/FoodNotAvailableException.java)<br>[`TableNotAvailableException.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/exception/TableNotAvailableException.java) |
| **Collections & Streams** | Extensively used `List<FoodItem>`, `Set<AmbianceType>`, `Set<String> tags`, Java 8+ Streams for distance calculations, turnover aggregations, and ranking. | [`EstablishmentServiceImpl.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/service/impl/EstablishmentServiceImpl.java)<br>[`AdminServiceImpl.java`](file:///c:/Users/LEYON/javaproject/src/main/java/com/smartdine/service/impl/AdminServiceImpl.java) |

---

## 👥 Three Roles & Workflows

### 1. USER / CUSTOMER (`customer.html`)
- Search bar: *"Search for hotels, restaurants or canteens..."* with live filtering.
- Interactive Map showing user position and nearby hotels & canteens.
- Ambiance switcher with real-time UI theme transformation and food recommendations.
- Fast Serve express ordering for seated diners (Table #).
- Table reservation system with guest count, time, and pre-ordered dishes.
- Full cart with food customizations ("Less spicy", "Extra cheese", "No onion"), 5% GST calculation, and UPI/Cash checkout.
- Real-time order progress tracker and 5-star customer review submission.

### 2. HOTEL / CANTEEN (`establishment.html`)
- **Shared Unified Dashboard**: Automatically adapts to the establishment type (`HOTEL` or `CANTEEN`).
- Real-time KPI metrics: SmartDine turnover, today's revenue, order counts, customer rating.
- Live kitchen order management pipeline (`PLACED` → `ACCEPTED` → `PREPARING` → `READY` → `SERVED` → `COMPLETED`).
- Fast Serve order badge indicators.
- Menu CRUD: Add dishes, edit prices, upload images, dietary veg/non-veg toggle, ambiance tags.
- Closing-time Smart Food Clearance Alert creator (dynamic 50% discount offers).
- Table reservations management & review monitoring.

### 3. ADMIN (`admin.html`)
- Platform-wide overview & verified turnover analytics.
- Date range filter: Today, Past 7 Days, Past 30 Days, All Time.
- User management: search users, block/unblock, suspend accounts.
- Establishment oversight: monitor hotels and canteens, activate/suspend establishments.
- Review moderation queue: review flagged customer feedback and resolve disputes.

---

## 🚀 Running the Project

### Option A: 1-Click Launch (Windows)
Double-click [`run.bat`](file:///c:/Users/LEYON/javaproject/run.bat) in the project root directory.

### Option B: Using Gradle
```powershell
.\gradlew.bat bootRun
```

### Option C: Using Maven
```bash
mvn spring-boot:run
```

Once started, open your browser at:
- **Application Portal**: [http://localhost:8080](http://localhost:8080)
- **H2 Web Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:smartdinedb`, User: `sa`, Password: empty)

---

## ⚡ 1-Click Evaluation Credentials

On the portal landing page ([http://localhost:8080](http://localhost:8080)), instant evaluation buttons are provided:
- **Customer**: `customer@smartdine.com` (*Aarav Sharma*)
- **Hotel Manager**: `hotel@smartdine.com` (*The Grand Taj Heritage Hotel - Chef Vikram*)
- **Canteen Manager**: `canteen@smartdine.com` (*Campus Central Canteen - Ramesh Gupta*)
- **Platform Admin**: `admin@smartdine.com` (*Dr. Priya Rao*)

---

## 🗄️ Database Architecture

The application runs out-of-the-box with embedded in-memory H2, pre-seeded with sample hotels, canteens, dishes, tables, reviews, and completed orders.

To run with **MySQL**:
1. Create a MySQL database named `smartdine_db` using [`schema-mysql.sql`](file:///c:/Users/LEYON/javaproject/src/main/resources/schema-mysql.sql).
2. Start the application with the MySQL profile:
```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=mysql'
```
