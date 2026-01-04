USE JacketShop;
GO

-- ============================================
-- DROP ALL TABLES (if exists) - For clean install
-- ============================================
IF OBJECT_ID('dbo.reviews', 'U') IS NOT NULL DROP TABLE dbo.reviews;
IF OBJECT_ID('dbo.order_histories', 'U') IS NOT NULL DROP TABLE dbo.order_histories;
IF OBJECT_ID('dbo.order_details', 'U') IS NOT NULL DROP TABLE dbo.order_details;
IF OBJECT_ID('dbo.orders', 'U') IS NOT NULL DROP TABLE dbo.orders;
IF OBJECT_ID('dbo.cart_items', 'U') IS NOT NULL DROP TABLE dbo.cart_items;
IF OBJECT_ID('dbo.carts', 'U') IS NOT NULL DROP TABLE dbo.carts;
IF OBJECT_ID('dbo.sale_variants', 'U') IS NOT NULL DROP TABLE dbo.sale_variants;
IF OBJECT_ID('dbo.product_images', 'U') IS NOT NULL DROP TABLE dbo.product_images;
IF OBJECT_ID('dbo.product_variants', 'U') IS NOT NULL DROP TABLE dbo.product_variants;
IF OBJECT_ID('dbo.products', 'U') IS NOT NULL DROP TABLE dbo.products;
IF OBJECT_ID('dbo.addresses', 'U') IS NOT NULL DROP TABLE dbo.addresses;
IF OBJECT_ID('dbo.password_reset_tokens', 'U') IS NOT NULL DROP TABLE dbo.password_reset_tokens;
IF OBJECT_ID('dbo.refresh_tokens', 'U') IS NOT NULL DROP TABLE dbo.refresh_tokens;
IF OBJECT_ID('dbo.user_coupons', 'U') IS NOT NULL DROP TABLE dbo.user_coupons;
IF OBJECT_ID('dbo.user_roles', 'U') IS NOT NULL DROP TABLE dbo.user_roles;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
IF OBJECT_ID('dbo.wards', 'U') IS NOT NULL DROP TABLE dbo.wards;
IF OBJECT_ID('dbo.districts', 'U') IS NOT NULL DROP TABLE dbo.districts;
IF OBJECT_ID('dbo.provinces', 'U') IS NOT NULL DROP TABLE dbo.provinces;
IF OBJECT_ID('dbo.sales', 'U') IS NOT NULL DROP TABLE dbo.sales;
IF OBJECT_ID('dbo.coupons', 'U') IS NOT NULL DROP TABLE dbo.coupons;
IF OBJECT_ID('dbo.payment_methods', 'U') IS NOT NULL DROP TABLE dbo.payment_methods;
IF OBJECT_ID('dbo.roles', 'U') IS NOT NULL DROP TABLE dbo.roles;
IF OBJECT_ID('dbo.styles', 'U') IS NOT NULL DROP TABLE dbo.styles;
IF OBJECT_ID('dbo.sizes', 'U') IS NOT NULL DROP TABLE dbo.sizes;
IF OBJECT_ID('dbo.materials', 'U') IS NOT NULL DROP TABLE dbo.materials;
IF OBJECT_ID('dbo.colors', 'U') IS NOT NULL DROP TABLE dbo.colors;
IF OBJECT_ID('dbo.brands', 'U') IS NOT NULL DROP TABLE dbo.brands;
GO

-- ============================================
-- REFERENCE DATA TABLES
-- ============================================

CREATE TABLE dbo.brands (
                            id BIGINT IDENTITY(1,1) NOT NULL,
                            created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                            updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                            created_by BIGINT NULL,
                            updated_by BIGINT NULL,
                            name NVARCHAR(120) NOT NULL,
                            description NVARCHAR(500) NULL,
                            logo VARCHAR(500) NULL,
                            status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                            CONSTRAINT PK_brands PRIMARY KEY (id),
                            CONSTRAINT UK_brands_name UNIQUE (name),
                            CONSTRAINT CHK_brands_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

CREATE INDEX IX_brands_status ON dbo.brands(status);
GO

CREATE TABLE dbo.colors (
                            id BIGINT IDENTITY(1,1) NOT NULL,
                            created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                            updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                            created_by BIGINT NULL,
                            updated_by BIGINT NULL,
                            name NVARCHAR(120) NOT NULL,
                            hex_code VARCHAR(10) NOT NULL,
                            description NVARCHAR(500) NULL,
                            status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                            CONSTRAINT PK_colors PRIMARY KEY (id),
                            CONSTRAINT UK_colors_name UNIQUE (name),
                            CONSTRAINT UK_colors_hex_code UNIQUE (hex_code),
                            CONSTRAINT CHK_colors_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

CREATE INDEX IX_colors_status ON dbo.colors(status);
GO

CREATE TABLE dbo.materials (
                               id BIGINT IDENTITY(1,1) NOT NULL,
                               created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                               updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                               created_by BIGINT NULL,
                               updated_by BIGINT NULL,
                               name NVARCHAR(120) NOT NULL,
                               description NVARCHAR(500) NULL,
                               status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                               CONSTRAINT PK_materials PRIMARY KEY (id),
                               CONSTRAINT UK_materials_name UNIQUE (name),
                               CONSTRAINT CHK_materials_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

CREATE INDEX IX_materials_status ON dbo.materials(status);
GO

CREATE TABLE dbo.sizes (
                           id BIGINT IDENTITY(1,1) NOT NULL,
                           created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                           updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                           created_by BIGINT NULL,
                           updated_by BIGINT NULL,
                           name NVARCHAR(120) NOT NULL,
                           description NVARCHAR(500) NULL,
                           status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                           CONSTRAINT PK_sizes PRIMARY KEY (id),
                           CONSTRAINT UK_sizes_name UNIQUE (name),
                           CONSTRAINT CHK_sizes_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

CREATE INDEX IX_sizes_status ON dbo.sizes(status);
GO

CREATE TABLE dbo.styles (
                            id BIGINT IDENTITY(1,1) NOT NULL,
                            created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                            updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                            created_by BIGINT NULL,
                            updated_by BIGINT NULL,
                            name NVARCHAR(120) NOT NULL,
                            description NVARCHAR(500) NULL,
                            status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                            CONSTRAINT PK_styles PRIMARY KEY (id),
                            CONSTRAINT UK_styles_name UNIQUE (name),
                            CONSTRAINT CHK_styles_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

CREATE INDEX IX_styles_status ON dbo.styles(status);
GO

CREATE TABLE dbo.roles (
                           id BIGINT IDENTITY(1,1) NOT NULL,
                           created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                           updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                           name VARCHAR(50) NOT NULL,
                           description NVARCHAR(255) NULL,
                           CONSTRAINT PK_roles PRIMARY KEY (id),
                           CONSTRAINT UK_roles_name UNIQUE (name)
);
GO

CREATE TABLE dbo.payment_methods (
                                     id BIGINT IDENTITY(1,1) NOT NULL,
                                     created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                     updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                     created_by BIGINT NULL,
                                     updated_by BIGINT NULL,
                                     code VARCHAR(50) NOT NULL,
                                     name NVARCHAR(100) NOT NULL,
                                     description NVARCHAR(500) NULL,
                                     type VARCHAR(20) NOT NULL,
                                     status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                                     CONSTRAINT PK_payment_methods PRIMARY KEY (id),
                                     CONSTRAINT UK_payment_methods_code UNIQUE (code),
                                     CONSTRAINT CHK_payment_methods_type CHECK (type IN ('ONLINE', 'POS')),
                                     CONSTRAINT CHK_payment_methods_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

CREATE INDEX IX_payment_methods_status_type ON dbo.payment_methods(status, type);
GO

CREATE TABLE dbo.coupons (
                             id BIGINT IDENTITY(1,1) NOT NULL,
                             created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                             updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                             created_by BIGINT NULL,
                             updated_by BIGINT NULL,
                             code VARCHAR(50) NOT NULL,
                             description NVARCHAR(500) NULL,
                             type VARCHAR(20) NOT NULL,
                             value DECIMAL(12, 2) NOT NULL,
                             min_order_value DECIMAL(12, 2) NULL,
                             max_discount DECIMAL(12, 2) NULL,
                             usage_limit INT NULL,
                             usage_limit_per_user INT NULL,
                             used_count INT NOT NULL DEFAULT 0,
                             valid_from DATETIMEOFFSET(6) NOT NULL,
                             valid_to DATETIMEOFFSET(6) NOT NULL,
                             status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                             CONSTRAINT PK_coupons PRIMARY KEY (id),
                             CONSTRAINT UK_coupons_code UNIQUE (code),
                             CONSTRAINT CHK_coupons_type CHECK (type IN ('PERCENT', 'AMOUNT')),
                             CONSTRAINT CHK_coupons_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
                             CONSTRAINT CHK_coupons_value CHECK (value > 0),
                             CONSTRAINT CHK_coupons_percent CHECK (type != 'PERCENT' OR value <= 100),
                             CONSTRAINT CHK_coupons_dates CHECK (valid_to >= valid_from)
);
GO

CREATE INDEX IX_coupons_code_status ON dbo.coupons(code, status);
CREATE INDEX IX_coupons_dates ON dbo.coupons(valid_from, valid_to, status);
GO

CREATE TABLE dbo.sales (
                           id BIGINT IDENTITY(1,1) NOT NULL,
                           created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                           updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                           created_by BIGINT NULL,
                           updated_by BIGINT NULL,
                           name NVARCHAR(255) NOT NULL,
                           description NVARCHAR(500) NULL,
                           discount_percentage DECIMAL(5, 2) NOT NULL,
                           start_date DATETIMEOFFSET(6) NOT NULL,
                           end_date DATETIMEOFFSET(6) NOT NULL,
                           status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                           CONSTRAINT PK_sales PRIMARY KEY (id),
                           CONSTRAINT CHK_sales_discount CHECK (discount_percentage BETWEEN 0 AND 100),
                           CONSTRAINT CHK_sales_dates CHECK (end_date >= start_date),
                           CONSTRAINT CHK_sales_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

CREATE INDEX IX_sales_dates_status ON dbo.sales(start_date, end_date, status);
GO

-- ============================================
-- LOCATION TABLES
-- ============================================

CREATE TABLE dbo.provinces (
                               id BIGINT IDENTITY(1,1) NOT NULL,
                               created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                               updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                               name NVARCHAR(255) NOT NULL,
                               goship_id VARCHAR(50) NULL,
                               code VARCHAR(20) NULL,
                               CONSTRAINT PK_provinces PRIMARY KEY (id),
                               CONSTRAINT UK_provinces_name UNIQUE (name)
);
GO

CREATE INDEX IX_provinces_code ON dbo.provinces(code);
GO

CREATE TABLE dbo.districts (
                               id BIGINT IDENTITY(1,1) NOT NULL,
                               created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                               updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                               name NVARCHAR(255) NOT NULL,
                               province_id BIGINT NOT NULL,
                               goship_id VARCHAR(50) NULL,
                               code VARCHAR(20) NULL,
                               CONSTRAINT PK_districts PRIMARY KEY (id),
                               CONSTRAINT FK_districts_province FOREIGN KEY (province_id) REFERENCES dbo.provinces(id)
);
GO

CREATE INDEX IX_districts_province ON dbo.districts(province_id);
CREATE INDEX IX_districts_code ON dbo.districts(code);
GO

CREATE TABLE dbo.wards (
                           id BIGINT IDENTITY(1,1) NOT NULL,
                           created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                           updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                           name NVARCHAR(255) NOT NULL,
                           district_id BIGINT NOT NULL,
                           goship_id VARCHAR(50) NULL,
                           code VARCHAR(20) NULL,
                           CONSTRAINT PK_wards PRIMARY KEY (id),
                           CONSTRAINT FK_wards_district FOREIGN KEY (district_id) REFERENCES dbo.districts(id)
);
GO

CREATE INDEX IX_wards_district ON dbo.wards(district_id);
CREATE INDEX IX_wards_code ON dbo.wards(code);
GO

-- ============================================
-- USER TABLES
-- ============================================

CREATE TABLE dbo.users (
                           id BIGINT IDENTITY(1,1) NOT NULL,
                           created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                           updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                           username VARCHAR(50) NOT NULL,
                           email VARCHAR(255) NULL,
                           password VARCHAR(255) NOT NULL,
                           full_name NVARCHAR(150) NOT NULL,
                           phone VARCHAR(15) NOT NULL,
                           avatar VARCHAR(500) NULL,
                           status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                           email_verified BIT NOT NULL DEFAULT 0,
                           phone_verified BIT NOT NULL DEFAULT 0,
                           last_login_at DATETIMEOFFSET(6) NULL,
                           CONSTRAINT PK_users PRIMARY KEY (id),
                           CONSTRAINT UK_users_username UNIQUE (username),
                           CONSTRAINT CHK_users_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

CREATE UNIQUE INDEX IX_users_email ON dbo.users(email) WHERE email IS NOT NULL;
CREATE INDEX IX_users_status ON dbo.users(status);
CREATE INDEX IX_users_phone ON dbo.users(phone);
GO

CREATE TABLE dbo.user_roles (
                                user_id BIGINT NOT NULL,
                                role_id BIGINT NOT NULL,
                                created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                CONSTRAINT PK_user_roles PRIMARY KEY (user_id, role_id),
                                CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE,
                                CONSTRAINT FK_user_roles_role FOREIGN KEY (role_id) REFERENCES dbo.roles(id) ON DELETE CASCADE
);
GO

CREATE INDEX IX_user_roles_role ON dbo.user_roles(role_id);
GO

CREATE TABLE dbo.user_coupons (
                                  id BIGINT IDENTITY(1,1) NOT NULL,
                                  created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                  updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                  user_id BIGINT NOT NULL,
                                  coupon_id BIGINT NOT NULL,
                                  used_count INT NOT NULL DEFAULT 0,
                                  first_used_at DATETIMEOFFSET(6) NULL,
                                  last_used_at DATETIMEOFFSET(6) NULL,
                                  CONSTRAINT PK_user_coupons PRIMARY KEY (id),
                                  CONSTRAINT UK_user_coupons UNIQUE (user_id, coupon_id),
                                  CONSTRAINT FK_user_coupons_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE,
                                  CONSTRAINT FK_user_coupons_coupon FOREIGN KEY (coupon_id) REFERENCES dbo.coupons(id) ON DELETE CASCADE,
                                  CONSTRAINT CHK_user_coupons_used_count CHECK (used_count >= 0)
);
GO

CREATE INDEX IX_user_coupons_user ON dbo.user_coupons(user_id);
CREATE INDEX IX_user_coupons_coupon ON dbo.user_coupons(coupon_id);
GO

CREATE TABLE dbo.refresh_tokens (
                                    id BIGINT IDENTITY(1,1) NOT NULL,
                                    created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                    updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                    user_id BIGINT NOT NULL,
                                    jti VARCHAR(64) NOT NULL,
                                    expires_at DATETIMEOFFSET(6) NOT NULL,
                                    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                                    CONSTRAINT PK_refresh_tokens PRIMARY KEY (id),
                                    CONSTRAINT UK_refresh_tokens_jti UNIQUE (jti),
                                    CONSTRAINT FK_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE,
                                    CONSTRAINT CHK_refresh_tokens_status CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED'))
);
GO

CREATE INDEX IX_refresh_tokens_user ON dbo.refresh_tokens(user_id);
CREATE INDEX IX_refresh_tokens_expires ON dbo.refresh_tokens(expires_at, status);
GO

CREATE TABLE dbo.password_reset_tokens (
                                           id BIGINT IDENTITY(1,1) NOT NULL,
                                           created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                           updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                           user_id BIGINT NOT NULL,
                                           token VARCHAR(255) NOT NULL,
                                           expiry_date DATETIMEOFFSET(6) NOT NULL,
                                           is_used BIT NOT NULL DEFAULT 0,
                                           CONSTRAINT PK_password_reset_tokens PRIMARY KEY (id),
                                           CONSTRAINT UK_password_reset_tokens_token UNIQUE (token),
                                           CONSTRAINT FK_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE
);
GO

CREATE INDEX IX_password_reset_tokens_user ON dbo.password_reset_tokens(user_id);
CREATE INDEX IX_password_reset_tokens_expiry ON dbo.password_reset_tokens(expiry_date, is_used);
GO

CREATE TABLE dbo.addresses (
                               id BIGINT IDENTITY(1,1) NOT NULL,
                               created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                               updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                               user_id BIGINT NOT NULL,
                               recipient_name NVARCHAR(120) NOT NULL,
                               recipient_phone VARCHAR(15) NOT NULL,
                               address_line NVARCHAR(255) NOT NULL,
                               ward_id BIGINT NOT NULL,
                               district_id BIGINT NOT NULL,
                               province_id BIGINT NOT NULL,
                               is_default BIT NOT NULL DEFAULT 0,
                               CONSTRAINT PK_addresses PRIMARY KEY (id),
                               CONSTRAINT FK_addresses_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE,
                               CONSTRAINT FK_addresses_ward FOREIGN KEY (ward_id) REFERENCES dbo.wards(id),
                               CONSTRAINT FK_addresses_district FOREIGN KEY (district_id) REFERENCES dbo.districts(id),
                               CONSTRAINT FK_addresses_province FOREIGN KEY (province_id) REFERENCES dbo.provinces(id)
);
GO

CREATE INDEX IX_addresses_user ON dbo.addresses(user_id);
CREATE INDEX IX_addresses_user_default ON dbo.addresses(user_id, is_default);
GO

-- ============================================
-- PRODUCT TABLES
-- ============================================

CREATE TABLE dbo.products (
                              id BIGINT IDENTITY(1,1) NOT NULL,
                              created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                              updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                              created_by BIGINT NULL,
                              updated_by BIGINT NULL,
                              name NVARCHAR(200) NOT NULL,
                              description NVARCHAR(4000) NULL,
                              brand_id BIGINT NOT NULL,
                              style_id BIGINT NOT NULL,
                              min_price DECIMAL(12, 2) NULL,
                              max_price DECIMAL(12, 2) NULL,
                              is_featured BIT NOT NULL DEFAULT 0,
                              sold_count BIGINT NOT NULL DEFAULT 0,
                              rating_count INT NOT NULL DEFAULT 0,
                              rating_average DECIMAL(3, 1) NULL DEFAULT 0,
                              status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                              version INT NOT NULL DEFAULT 1,
                              CONSTRAINT PK_products PRIMARY KEY (id),
                              CONSTRAINT FK_products_brand FOREIGN KEY (brand_id) REFERENCES dbo.brands(id),
                              CONSTRAINT FK_products_style FOREIGN KEY (style_id) REFERENCES dbo.styles(id),
                              CONSTRAINT CHK_products_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
                              CONSTRAINT CHK_products_rating CHECK (rating_average IS NULL OR rating_average BETWEEN 0 AND 5)
);
GO

CREATE INDEX IX_products_brand ON dbo.products(brand_id, status);
CREATE INDEX IX_products_style ON dbo.products(style_id, status);
CREATE INDEX IX_products_featured ON dbo.products(is_featured, status);
CREATE INDEX IX_products_rating ON dbo.products(rating_average DESC, rating_count DESC) WHERE status = 'ACTIVE';
CREATE INDEX IX_products_sold ON dbo.products(sold_count DESC) WHERE status = 'ACTIVE';
GO

CREATE TABLE dbo.product_variants (
                                      id BIGINT IDENTITY(1,1) NOT NULL,
                                      created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                      updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                      created_by BIGINT NULL,
                                      updated_by BIGINT NULL,
                                      product_id BIGINT NOT NULL,
                                      size_id BIGINT NOT NULL,
                                      color_id BIGINT NOT NULL,
                                      material_id BIGINT NOT NULL,
                                      sku VARCHAR(64) NOT NULL,
                                      cost_price DECIMAL(12, 2) NOT NULL,
                                      price DECIMAL(12, 2) NOT NULL,
                                      quantity INT NOT NULL DEFAULT 0,
                                      available_quantity INT NOT NULL DEFAULT 0,
                                      reserved_quantity INT NOT NULL DEFAULT 0,
                                      sold_count INT NOT NULL DEFAULT 0,
                                      return_count INT NOT NULL DEFAULT 0,
                                      weight DECIMAL(8, 2) NULL,
                                      length DECIMAL(8, 2) NULL,
                                      width DECIMAL(8, 2) NULL,
                                      height DECIMAL(8, 2) NULL,
                                      status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                                      version INT NOT NULL DEFAULT 1,
                                      CONSTRAINT PK_product_variants PRIMARY KEY (id),
                                      CONSTRAINT UK_product_variants_sku UNIQUE (sku),
                                      CONSTRAINT UK_product_variants_combination UNIQUE (product_id, size_id, color_id, material_id),
                                      CONSTRAINT FK_product_variants_product FOREIGN KEY (product_id) REFERENCES dbo.products(id) ON DELETE CASCADE,
                                      CONSTRAINT FK_product_variants_size FOREIGN KEY (size_id) REFERENCES dbo.sizes(id),
                                      CONSTRAINT FK_product_variants_color FOREIGN KEY (color_id) REFERENCES dbo.colors(id),
                                      CONSTRAINT FK_product_variants_material FOREIGN KEY (material_id) REFERENCES dbo.materials(id),
                                      CONSTRAINT CHK_product_variants_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
                                      CONSTRAINT CHK_product_variants_prices CHECK (cost_price >= 0 AND price > 0 AND price >= cost_price),
                                      CONSTRAINT CHK_product_variants_quantities CHECK (
                                          quantity >= 0 AND
                                          available_quantity >= 0 AND
                                          reserved_quantity >= 0 AND
                                          sold_count >= 0 AND
                                          return_count >= 0
                                          ),
                                      CONSTRAINT CHK_product_variants_inventory CHECK (quantity = available_quantity + reserved_quantity)
);
GO

CREATE INDEX IX_product_variants_product ON dbo.product_variants(product_id, status);
CREATE INDEX IX_product_variants_sku ON dbo.product_variants(sku);
CREATE INDEX IX_product_variants_size ON dbo.product_variants(size_id);
CREATE INDEX IX_product_variants_color ON dbo.product_variants(color_id);
CREATE INDEX IX_product_variants_material ON dbo.product_variants(material_id);
GO

CREATE TABLE dbo.product_images (
                                    id BIGINT IDENTITY(1,1) NOT NULL,
                                    created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                    product_id BIGINT NOT NULL,
                                    color_id BIGINT NULL,
                                    image_url VARCHAR(500) NOT NULL,
                                    is_thumbnail BIT NOT NULL DEFAULT 0,
                                    CONSTRAINT PK_product_images PRIMARY KEY (id),
                                    CONSTRAINT FK_product_images_product FOREIGN KEY (product_id) REFERENCES dbo.products(id) ON DELETE CASCADE,
                                    CONSTRAINT FK_product_images_color FOREIGN KEY (color_id) REFERENCES dbo.colors(id)
);
GO

CREATE INDEX IX_product_images_product ON dbo.product_images(product_id);
GO

CREATE TABLE dbo.sale_variants (
                                   sale_id BIGINT NOT NULL,
                                   product_variant_id BIGINT NOT NULL,
                                   created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                   CONSTRAINT PK_sale_variants PRIMARY KEY (sale_id, product_variant_id),
                                   CONSTRAINT FK_sale_variants_sale FOREIGN KEY (sale_id) REFERENCES dbo.sales(id) ON DELETE CASCADE,
                                   CONSTRAINT FK_sale_variants_variant FOREIGN KEY (product_variant_id) REFERENCES dbo.product_variants(id) ON DELETE CASCADE
);
GO

CREATE INDEX IX_sale_variants_variant ON dbo.sale_variants(product_variant_id);
GO

-- ============================================
-- CART TABLES
-- ============================================

CREATE TABLE dbo.carts (
                           id BIGINT IDENTITY(1,1) NOT NULL,
                           created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                           updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                           user_id BIGINT NOT NULL,
                           CONSTRAINT PK_carts PRIMARY KEY (id),
                           CONSTRAINT UK_carts_user UNIQUE (user_id),
                           CONSTRAINT FK_carts_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.cart_items (
                                id BIGINT IDENTITY(1,1) NOT NULL,
                                created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                cart_id BIGINT NOT NULL,
                                product_variant_id BIGINT NOT NULL,
                                quantity INT NOT NULL,
                                unit_price DECIMAL(12, 2) NOT NULL,
                                selected BIT NOT NULL DEFAULT 1,
                                CONSTRAINT PK_cart_items PRIMARY KEY (id),
                                CONSTRAINT UK_cart_items_variant UNIQUE (cart_id, product_variant_id),
                                CONSTRAINT FK_cart_items_cart FOREIGN KEY (cart_id) REFERENCES dbo.carts(id) ON DELETE CASCADE,
                                CONSTRAINT FK_cart_items_variant FOREIGN KEY (product_variant_id) REFERENCES dbo.product_variants(id) ON DELETE CASCADE,
                                CONSTRAINT CHK_cart_items_quantity CHECK (quantity > 0),
                                CONSTRAINT CHK_cart_items_price CHECK (unit_price > 0)
);
GO

CREATE INDEX IX_cart_items_cart ON dbo.cart_items(cart_id);
CREATE INDEX IX_cart_items_variant ON dbo.cart_items(product_variant_id);
GO

-- ============================================
-- ORDER TABLES
-- ============================================

CREATE TABLE dbo.orders (
                            id BIGINT IDENTITY(1,1) NOT NULL,
                            created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                            updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                            order_code VARCHAR(32) NOT NULL,
                            order_type VARCHAR(20) NOT NULL,
                            user_id BIGINT NULL,
                            staff_id BIGINT NULL,
                            customer_name NVARCHAR(120) NOT NULL,
                            customer_phone VARCHAR(15) NOT NULL,
                            customer_email VARCHAR(255) NULL,
                            shipping_recipient_name NVARCHAR(120) NULL,
                            shipping_recipient_phone VARCHAR(15) NULL,
                            shipping_address_line NVARCHAR(255) NULL,
                            shipping_ward_code VARCHAR(20) NULL,
                            shipping_ward_name NVARCHAR(100) NULL,
                            shipping_district_code VARCHAR(20) NULL,
                            shipping_district_name NVARCHAR(100) NULL,
                            shipping_province_code VARCHAR(20) NULL,
                            shipping_province_name NVARCHAR(100) NULL,
                            shipping_fee DECIMAL(12, 2) NOT NULL DEFAULT 0,
                            carrier_name NVARCHAR(100) NULL,
                            carrier_service_name NVARCHAR(100) NULL,
                            carrier_rate_id VARCHAR(100) NULL,
                            delivery_time_estimate NVARCHAR(255) NULL,
                            tracking_number VARCHAR(100) NULL,
                            payment_method_id BIGINT NULL,
                            payment_method_name NVARCHAR(80) NULL,
                            payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
                            payment_date DATETIMEOFFSET(6) NULL,
                            transaction_id VARCHAR(255) NULL,
                            subtotal DECIMAL(12, 2) NOT NULL,
                            discount DECIMAL(12, 2) NOT NULL DEFAULT 0,
                            total DECIMAL(12, 2) NOT NULL,
                            coupon_id BIGINT NULL,
                            coupon_code VARCHAR(50) NULL,
                            status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                            note NVARCHAR(1000) NULL,
                            confirmed_at DATETIMEOFFSET(6) NULL,
                            processing_at DATETIMEOFFSET(6) NULL,
                            shipped_at DATETIMEOFFSET(6) NULL,
                            completed_at DATETIMEOFFSET(6) NULL,
                            cancelled_at DATETIMEOFFSET(6) NULL,
                            returned_at DATETIMEOFFSET(6) NULL,
                            CONSTRAINT PK_orders PRIMARY KEY (id),
                            CONSTRAINT UK_orders_code UNIQUE (order_code),
                            CONSTRAINT FK_orders_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
                            CONSTRAINT FK_orders_staff FOREIGN KEY (staff_id) REFERENCES dbo.users(id),
                            CONSTRAINT FK_orders_payment_method FOREIGN KEY (payment_method_id) REFERENCES dbo.payment_methods(id),
                            CONSTRAINT FK_orders_coupon FOREIGN KEY (coupon_id) REFERENCES dbo.coupons(id),
                            CONSTRAINT CHK_orders_type CHECK (order_type IN ('ONLINE', 'POS_INSTORE')),
                            CONSTRAINT CHK_orders_payment_status CHECK (payment_status IN ('UNPAID', 'PAID', 'REFUNDED')),
                            CONSTRAINT CHK_orders_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'COMPLETED', 'CANCELLED', 'RETURNED')),
                            CONSTRAINT CHK_orders_amounts CHECK (subtotal >= 0 AND discount >= 0 AND total >= 0)
);
GO

CREATE INDEX IX_orders_code ON dbo.orders(order_code);
CREATE INDEX IX_orders_user ON dbo.orders(user_id, created_at DESC);
CREATE INDEX IX_orders_staff ON dbo.orders(staff_id, created_at DESC);
CREATE INDEX IX_orders_status ON dbo.orders(status, created_at DESC);
CREATE INDEX IX_orders_payment_status ON dbo.orders(payment_status, status);
CREATE INDEX IX_orders_type ON dbo.orders(order_type, status);
CREATE INDEX IX_orders_created ON dbo.orders(created_at DESC);
CREATE INDEX IX_orders_tracking ON dbo.orders(tracking_number) WHERE tracking_number IS NOT NULL;
GO

CREATE TABLE dbo.order_details (
                                   id BIGINT IDENTITY(1,1) NOT NULL,
                                   created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                   updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                   order_id BIGINT NOT NULL,
                                   product_variant_id BIGINT NOT NULL,
                                   product_name NVARCHAR(200) NOT NULL,
                                   sku VARCHAR(64) NOT NULL,
                                   color NVARCHAR(50) NOT NULL,
                                   size NVARCHAR(50) NOT NULL,
                                   material NVARCHAR(50) NOT NULL,
                                   image NVARCHAR(500) NULL,
                                   original_price DECIMAL(12, 2) NOT NULL,
                                   price DECIMAL(12, 2) NOT NULL,
                                   discount_percentage DECIMAL(5, 2) NULL,
                                   quantity INT NOT NULL,
                                   subtotal DECIMAL(12, 2) NOT NULL,
                                   CONSTRAINT PK_order_details PRIMARY KEY (id),
                                   CONSTRAINT FK_order_details_order FOREIGN KEY (order_id) REFERENCES dbo.orders(id) ON DELETE CASCADE,
                                   CONSTRAINT FK_order_details_variant FOREIGN KEY (product_variant_id) REFERENCES dbo.product_variants(id),
                                   CONSTRAINT CHK_order_details_quantity CHECK (quantity > 0),
                                   CONSTRAINT CHK_order_details_prices CHECK (original_price > 0 AND price > 0 AND price <= original_price),
                                   CONSTRAINT CHK_order_details_discount CHECK (discount_percentage IS NULL OR discount_percentage BETWEEN 0 AND 100)
);
GO

CREATE INDEX IX_order_details_order ON dbo.order_details(order_id);
CREATE INDEX IX_order_details_variant ON dbo.order_details(product_variant_id);
GO

CREATE TABLE dbo.order_histories (
                                     id BIGINT IDENTITY(1,1) NOT NULL,
                                     created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                     updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                                     order_id BIGINT NOT NULL,
                                     changed_by_user_id BIGINT NULL,
                                     old_status VARCHAR(20) NULL,
                                     new_status VARCHAR(20) NULL,
                                     old_payment_status VARCHAR(20) NULL,
                                     new_payment_status VARCHAR(20) NULL,
                                     note NVARCHAR(1000) NULL,
                                     CONSTRAINT PK_order_histories PRIMARY KEY (id),
                                     CONSTRAINT FK_order_histories_order FOREIGN KEY (order_id) REFERENCES dbo.orders(id) ON DELETE CASCADE,
                                     CONSTRAINT FK_order_histories_user FOREIGN KEY (changed_by_user_id) REFERENCES dbo.users(id)
);
GO

CREATE INDEX IX_order_histories_order ON dbo.order_histories(order_id, created_at DESC);
CREATE INDEX IX_order_histories_user ON dbo.order_histories(changed_by_user_id);
GO

-- ============================================
-- REVIEW TABLES
-- ============================================

CREATE TABLE dbo.reviews (
                             id BIGINT IDENTITY(1,1) NOT NULL,
                             created_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                             updated_at DATETIMEOFFSET(6) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
                             user_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             order_id BIGINT NULL,
                             user_name NVARCHAR(120) NOT NULL,
                             product_name NVARCHAR(200) NOT NULL,
                             rating INT NOT NULL,
                             comment NVARCHAR(2000) NULL,
                             is_verified_purchase BIT NOT NULL DEFAULT 0,
                             CONSTRAINT PK_reviews PRIMARY KEY (id),
                             CONSTRAINT FK_reviews_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
                             CONSTRAINT FK_reviews_product FOREIGN KEY (product_id) REFERENCES dbo.products(id) ON DELETE CASCADE,
                             CONSTRAINT FK_reviews_order FOREIGN KEY (order_id) REFERENCES dbo.orders(id),
                             CONSTRAINT CHK_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);
GO

CREATE INDEX IX_reviews_product ON dbo.reviews(product_id, created_at DESC);
CREATE INDEX IX_reviews_user ON dbo.reviews(user_id, created_at DESC);
CREATE INDEX IX_reviews_order ON dbo.reviews(order_id);
CREATE INDEX IX_reviews_rating ON dbo.reviews(product_id, rating);
GO

PRINT 'Database schema created successfully!';
PRINT 'Total tables: 28';
PRINT 'All business logic should be implemented in application layer.';
GO