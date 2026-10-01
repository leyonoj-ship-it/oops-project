-- ===================================================
-- SMARTDINE DATABASE SCHEMA (MySQL)
-- Fully normalized relational architecture
-- ===================================================

CREATE DATABASE IF NOT EXISTS smartdine_db;
USE smartdine_db;

-- 1. Base Users Table (Inheritance root)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(30) NOT NULL, -- CUSTOMER, ESTABLISHMENT_OWNER, ADMIN
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, BLOCKED, SUSPENDED
    google_id VARCHAR(100),
    profile_image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Customer Extension Table
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT PRIMARY KEY,
    delivery_address VARCHAR(255),
    loyalty_points INT DEFAULT 0,
    preferred_ambiance VARCHAR(50),
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Admin Extension Table
CREATE TABLE IF NOT EXISTS admins (
    id BIGINT PRIMARY KEY,
    admin_department VARCHAR(100) DEFAULT 'Platform Moderation',
    clearance_level INT DEFAULT 1,
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Establishments Table (CRITICAL: Unifies HOTEL and CANTEEN into ONE table)
CREATE TABLE IF NOT EXISTS establishments (
    establishment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(30) NOT NULL, -- HOTEL or CANTEEN (EstablishmentType)
    description TEXT,
    location_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL DEFAULT 'Bengaluru',
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    phone VARCHAR(20),
    rating DOUBLE DEFAULT 4.0,
    review_count INT DEFAULT 0,
    opening_time VARCHAR(10) DEFAULT '08:00',
    closing_time VARCHAR(10) DEFAULT '22:30',
    cover_image_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, BLOCKED
    owner_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 5. Establishment Supported Ambiances (Many-to-Many / Element Collection)
CREATE TABLE IF NOT EXISTS establishment_ambiances (
    establishment_id BIGINT NOT NULL,
    ambiance_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (establishment_id, ambiance_type),
    FOREIGN KEY (establishment_id) REFERENCES establishments(establishment_id) ON DELETE CASCADE
);

-- 6. Food Categories Table
CREATE TABLE IF NOT EXISTS food_categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    display_order INT DEFAULT 0
);

-- 7. Food Items Table
CREATE TABLE IF NOT EXISTS food_items (
    food_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    establishment_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    category_id BIGINT,
    price DOUBLE NOT NULL,
    offer_price DOUBLE,
    clearance_price DOUBLE,
    is_available BOOLEAN DEFAULT TRUE,
    is_veg BOOLEAN DEFAULT TRUE,
    stock_quantity INT DEFAULT 50,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (establishment_id) REFERENCES establishments(establishment_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES food_categories(category_id) ON DELETE SET NULL
);

-- 8. Food Item Ambiance & Flavor Tags
CREATE TABLE IF NOT EXISTS food_tags (
    food_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (food_id, tag),
    FOREIGN KEY (food_id) REFERENCES food_items(food_id) ON DELETE CASCADE
);

-- 9. Dining Tables Table
CREATE TABLE IF NOT EXISTS dining_tables (
    table_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    establishment_id BIGINT NOT NULL,
    table_number INT NOT NULL,
    capacity INT NOT NULL DEFAULT 4,
    is_available BOOLEAN DEFAULT TRUE,
    ambiance VARCHAR(50) DEFAULT 'DEFAULT',
    FOREIGN KEY (establishment_id) REFERENCES establishments(establishment_id) ON DELETE CASCADE
);

-- 10. Reservations Table
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    establishment_id BIGINT NOT NULL,
    table_id BIGINT,
    reservation_date DATE NOT NULL,
    reservation_time VARCHAR(10) NOT NULL,
    number_of_guests INT NOT NULL,
    selected_ambiance VARCHAR(50) NOT NULL,
    special_notes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED', -- PENDING, CONFIRMED, COMPLETED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (establishment_id) REFERENCES establishments(establishment_id) ON DELETE CASCADE,
    FOREIGN KEY (table_id) REFERENCES dining_tables(table_id) ON DELETE SET NULL
);

-- 11. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    establishment_id BIGINT NOT NULL,
    table_id BIGINT,
    table_number INT,
    is_fast_serve BOOLEAN DEFAULT FALSE,
    subtotal DOUBLE NOT NULL,
    discount_amount DOUBLE DEFAULT 0.0,
    tax_amount DOUBLE DEFAULT 0.0,
    final_amount DOUBLE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PLACED', -- PLACED, ACCEPTED, PREPARING, READY, SERVED, COMPLETED, CANCELLED
    payment_method VARCHAR(20) NOT NULL, -- CASH, UPI
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, COMPLETED, FAILED
    payment_reference VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (establishment_id) REFERENCES establishments(establishment_id) ON DELETE CASCADE,
    FOREIGN KEY (table_id) REFERENCES dining_tables(table_id) ON DELETE SET NULL
);

-- 12. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    food_name VARCHAR(150) NOT NULL,
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    customization VARCHAR(255),
    total_price DOUBLE NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES food_items(food_id) ON DELETE CASCADE
);

-- 13. Customer Reviews Table
CREATE TABLE IF NOT EXISTS reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    establishment_id BIGINT NOT NULL,
    order_id BIGINT,
    rating DOUBLE NOT NULL,
    comment TEXT,
    is_flagged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (establishment_id) REFERENCES establishments(establishment_id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE SET NULL
);

-- 14. Smart Food Clearance Offers Table (Closing time dynamic discounts)
CREATE TABLE IF NOT EXISTS clearance_offers (
    offer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    establishment_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    discount_percentage INT NOT NULL,
    original_price DOUBLE NOT NULL,
    clearance_price DOUBLE NOT NULL,
    initial_quantity INT NOT NULL,
    remaining_quantity INT NOT NULL,
    start_time VARCHAR(10) NOT NULL,
    end_time VARCHAR(10) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (establishment_id) REFERENCES establishments(establishment_id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES food_items(food_id) ON DELETE CASCADE
);

-- 15. General Offers Table
CREATE TABLE IF NOT EXISTS offers (
    offer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    establishment_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    discount_percentage INT NOT NULL,
    valid_until DATE,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (establishment_id) REFERENCES establishments(establishment_id) ON DELETE CASCADE
);
