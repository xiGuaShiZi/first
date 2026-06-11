CREATE DATABASE IF NOT EXISTS enterprise_website
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE enterprise_website;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS return_request;
DROP TABLE IF EXISTS product_review;
DROP TABLE IF EXISTS product_order;
DROP TABLE IF EXISTS shopping_cart_item;
DROP TABLE IF EXISTS message;
DROP TABLE IF EXISTS banner;
DROP TABLE IF EXISTS news_media;
DROP TABLE IF EXISTS news;
DROP TABLE IF EXISTS product_image;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS product_category;
DROP TABLE IF EXISTS customer_address;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS company_info;
DROP TABLE IF EXISTS sys_user;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255) NOT NULL,
  real_name VARCHAR(80),
  status TINYINT NOT NULL DEFAULT 1,
  last_login_time DATETIME,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_sys_user_username UNIQUE (username),
  CONSTRAINT ck_sys_user_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RBAC用户';

CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(60) NOT NULL,
  role_name VARCHAR(80) NOT NULL,
  description VARCHAR(300),
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_sys_role_code UNIQUE (role_code),
  CONSTRAINT ck_sys_role_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RBAC角色';

CREATE TABLE sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  permission_code VARCHAR(100) NOT NULL,
  permission_name VARCHAR(100) NOT NULL,
  permission_type VARCHAR(20) NOT NULL,
  parent_id BIGINT,
  path VARCHAR(200),
  method VARCHAR(20),
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_sys_permission_code UNIQUE (permission_code),
  INDEX idx_permission_parent_sort (parent_id, sort),
  INDEX idx_permission_type_status (permission_type, status),
  CONSTRAINT fk_permission_parent FOREIGN KEY (parent_id) REFERENCES sys_permission(id) ON DELETE SET NULL,
  CONSTRAINT ck_permission_type CHECK (permission_type IN ('MENU', 'API', 'BUTTON')),
  CONSTRAINT ck_permission_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RBAC权限';

CREATE TABLE sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id),
  INDEX idx_sys_user_role_role (role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联';

CREATE TABLE sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_sys_role_permission UNIQUE (role_id, permission_id),
  INDEX idx_sys_role_permission_permission (permission_id),
  CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
  CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联';

CREATE TABLE operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT,
  operator_name VARCHAR(80),
  operator_type VARCHAR(20) NOT NULL DEFAULT 'ANONYMOUS',
  action VARCHAR(100) NOT NULL,
  request_method VARCHAR(20) NOT NULL,
  request_uri VARCHAR(300) NOT NULL,
  module VARCHAR(80),
  target_type VARCHAR(80),
  target_id VARCHAR(80),
  ip_address VARCHAR(80),
  user_agent VARCHAR(300),
  status_code INT,
  result_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
  error_message VARCHAR(500),
  duration_ms BIGINT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_operation_log_operator_time (operator_type, operator_id, create_time),
  INDEX idx_operation_log_module_time (module, create_time),
  INDEX idx_operation_log_result_time (result_status, create_time),
  INDEX idx_operation_log_uri_time (request_uri, create_time),
  CONSTRAINT ck_operation_log_result CHECK (result_status IN ('SUCCESS', 'FAIL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户操作日志';

CREATE TABLE customer (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(80) NOT NULL,
  password VARCHAR(255) NOT NULL,
  phone VARCHAR(50),
  email VARCHAR(120),
  default_address VARCHAR(300),
  bank_account VARCHAR(16),
  balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  points INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_customer_username UNIQUE (username),
  CONSTRAINT uk_customer_phone UNIQUE (phone),
  CONSTRAINT uk_customer_email UNIQUE (email),
  CONSTRAINT ck_customer_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='前台会员';

CREATE TABLE customer_address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  receiver_name VARCHAR(80) NOT NULL,
  phone VARCHAR(50) NOT NULL,
  province VARCHAR(80),
  city VARCHAR(80),
  district VARCHAR(80),
  detail_address VARCHAR(300) NOT NULL,
  is_default TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_customer_address_customer (customer_id, is_default, update_time),
  CONSTRAINT fk_customer_address_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
  CONSTRAINT ck_customer_address_default CHECK (is_default IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员收货地址';

CREATE TABLE company_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_name VARCHAR(200) NOT NULL,
  intro TEXT NOT NULL,
  culture TEXT,
  phone VARCHAR(50),
  email VARCHAR(120),
  address VARCHAR(300),
  logo VARCHAR(500),
  website VARCHAR(200),
  service_time VARCHAR(100),
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业信息';

CREATE TABLE banner (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  subtitle VARCHAR(300),
  image VARCHAR(500) NOT NULL,
  link VARCHAR(500),
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_banner_status_sort (status, sort, id),
  CONSTRAINT ck_banner_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页轮播图';

CREATE TABLE news (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  summary VARCHAR(500),
  content MEDIUMTEXT NOT NULL,
  author VARCHAR(50),
  cover_image VARCHAR(500),
  tags VARCHAR(300),
  source VARCHAR(100) NOT NULL DEFAULT '企业官网',
  view_count INT NOT NULL DEFAULT 0,
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_news_title (title),
  INDEX idx_news_status_time (status, create_time),
  INDEX idx_news_sort_time (sort, create_time),
  FULLTEXT INDEX ft_news_title_content (title, content),
  CONSTRAINT ck_news_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业新闻';

CREATE TABLE news_media (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  news_id BIGINT NOT NULL,
  media_type VARCHAR(20) NOT NULL DEFAULT 'IMAGE',
  url VARCHAR(500) NOT NULL,
  caption VARCHAR(300),
  sort INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_news_media_news_sort (news_id, sort),
  CONSTRAINT fk_news_media_news FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE,
  CONSTRAINT ck_news_media_type CHECK (media_type IN ('IMAGE', 'VIDEO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='新闻图文媒体';

CREATE TABLE product_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  parent_id BIGINT,
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_product_category_name UNIQUE (name),
  INDEX idx_product_category_parent_sort (parent_id, sort),
  CONSTRAINT fk_product_category_parent FOREIGN KEY (parent_id) REFERENCES product_category(id) ON DELETE SET NULL,
  CONSTRAINT ck_product_category_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类';

CREATE TABLE product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sku VARCHAR(80),
  name VARCHAR(200) NOT NULL,
  description TEXT NOT NULL,
  image VARCHAR(500) COMMENT '封面图片，保存后端上传接口返回的服务器路径',
  category VARCHAR(100),
  category_id BIGINT,
  tags VARCHAR(300),
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  original_price DECIMAL(10,2),
  stock INT NOT NULL DEFAULT 0,
  unit VARCHAR(20) NOT NULL DEFAULT '件',
  sales_count INT NOT NULL DEFAULT 0,
  weight_grams INT,
  detail MEDIUMTEXT,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_product_sku UNIQUE (sku),
  INDEX idx_product_name (name),
  INDEX idx_product_status_time (status, create_time),
  INDEX idx_product_category_status (category_id, status),
  FULLTEXT INDEX ft_product_name_detail (name, description, detail),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_category(id) ON DELETE SET NULL,
  CONSTRAINT ck_product_status CHECK (status IN (0, 1)),
  CONSTRAINT ck_product_stock CHECK (stock >= 0),
  CONSTRAINT ck_product_price CHECK (price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品';

CREATE TABLE product_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  image_url VARCHAR(500) NOT NULL COMMENT '详情图片，保存后端上传接口返回的服务器路径',
  caption VARCHAR(300),
  sort INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_product_image_product_sort (product_id, sort),
  CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片';

CREATE TABLE shopping_cart_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  customer_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL DEFAULT 0,
  quantity INT NOT NULL DEFAULT 1,
  checked TINYINT NOT NULL DEFAULT 1,
  selected TINYINT NOT NULL DEFAULT 1,
  snapshot_product_name VARCHAR(200),
  snapshot_price DECIMAL(10,2),
  snapshot_image VARCHAR(500),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT uk_cart_customer_product UNIQUE (customer_id, product_id, sku_id),
  INDEX idx_cart_customer_time (customer_id, update_time),
  INDEX idx_cart_product (product_id),
  CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
  CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
  CONSTRAINT ck_cart_quantity CHECK (quantity > 0),
  CONSTRAINT ck_cart_checked CHECK (checked IN (0, 1)),
  CONSTRAINT ck_cart_selected CHECK (selected IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车明细';

CREATE TABLE product_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(40) NOT NULL,
  main_order_no VARCHAR(40),
  sub_order_no VARCHAR(50),
  shop_id BIGINT,
  shop_name VARCHAR(120),
  customer_id BIGINT NOT NULL,
  customer_name VARCHAR(80),
  product_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL DEFAULT 0,
  product_name VARCHAR(200),
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  quantity INT NOT NULL DEFAULT 1,
  product_discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  order_discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  contact_phone VARCHAR(50),
  address VARCHAR(300),
  payment_method VARCHAR(40) NOT NULL DEFAULT 'SIMULATED',
  paid_time DATETIME,
  logistics_no VARCHAR(80),
  remark VARCHAR(500),
  status VARCHAR(40) NOT NULL DEFAULT 'WAIT_BUYER_PAY',
  is_reviewed TINYINT NOT NULL DEFAULT 0,
  finish_time DATETIME,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_product_order_no UNIQUE (order_no),
  INDEX idx_order_customer_time (customer_id, create_time),
  INDEX idx_order_product (product_id),
  INDEX idx_order_status_time (status, create_time),
  CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
  CONSTRAINT fk_order_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT ck_order_quantity CHECK (quantity > 0),
  CONSTRAINT ck_order_amount CHECK (total_amount >= 0),
  CONSTRAINT ck_order_reviewed CHECK (is_reviewed IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品订单';

CREATE TABLE product_review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_no VARCHAR(40),
  customer_id BIGINT NOT NULL,
  customer_name VARCHAR(80),
  product_id BIGINT NOT NULL,
  product_name VARCHAR(200),
  review_type VARCHAR(20) NOT NULL DEFAULT 'MAIN',
  quality_rating TINYINT NOT NULL,
  service_rating TINYINT NOT NULL,
  logistics_rating TINYINT NOT NULL,
  content TEXT,
  media_urls VARCHAR(2000),
  anonymous TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_product_review_order_type UNIQUE (order_id, review_type),
  INDEX idx_review_customer_time (customer_id, create_time),
  INDEX idx_review_product_time (product_id, create_time),
  CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES product_order(id),
  CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
  CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT ck_review_type CHECK (review_type IN ('MAIN', 'ADDITIONAL')),
  CONSTRAINT ck_review_quality CHECK (quality_rating BETWEEN 1 AND 5),
  CONSTRAINT ck_review_service CHECK (service_rating BETWEEN 1 AND 5),
  CONSTRAINT ck_review_logistics CHECK (logistics_rating BETWEEN 1 AND 5),
  CONSTRAINT ck_review_anonymous CHECK (anonymous IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价';

CREATE TABLE return_request (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_no VARCHAR(40),
  customer_id BIGINT NOT NULL,
  customer_name VARCHAR(80),
  product_name VARCHAR(200),
  request_type VARCHAR(40) NOT NULL DEFAULT 'ONLY_REFUND',
  reason TEXT NOT NULL,
  evidence_image VARCHAR(500),
  evidence_media VARCHAR(2000),
  status VARCHAR(40) NOT NULL DEFAULT 'APPLY_PENDING',
  reply TEXT,
  handle_time DATETIME,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_return_customer_time (customer_id, create_time),
  INDEX idx_return_status_time (status, create_time),
  INDEX idx_return_order (order_id),
  CONSTRAINT fk_return_order FOREIGN KEY (order_id) REFERENCES product_order(id),
  CONSTRAINT fk_return_customer FOREIGN KEY (customer_id) REFERENCES customer(id),
  CONSTRAINT ck_return_type CHECK (request_type IN ('ONLY_REFUND', 'RETURN_REFUND', 'EXCHANGE', 'REPAIR')),
  CONSTRAINT ck_return_status CHECK (status IN ('APPLY_PENDING', 'APPROVED', 'REJECTED', 'BUYER_RETURNING', 'SELLER_RECEIVED', 'REFUNDED', 'RESHIPPED', 'COMPLETED', 'CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='售后协商';

CREATE TABLE message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(80) NOT NULL,
  phone VARCHAR(50),
  email VARCHAR(120),
  content TEXT NOT NULL,
  source VARCHAR(40) NOT NULL DEFAULT 'WEB',
  status TINYINT NOT NULL DEFAULT 0,
  reply TEXT,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_message_status_time (status, create_time),
  INDEX idx_message_phone (phone),
  CONSTRAINT ck_message_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='咨询留言';
