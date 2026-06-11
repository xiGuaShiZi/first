USE enterprise_website;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE operation_log;
TRUNCATE TABLE sys_role_permission;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_permission;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE return_request;
TRUNCATE TABLE product_review;
TRUNCATE TABLE product_order;
TRUNCATE TABLE shopping_cart_item;
TRUNCATE TABLE message;
TRUNCATE TABLE banner;
TRUNCATE TABLE news_media;
TRUNCATE TABLE news;
TRUNCATE TABLE product_image;
TRUNCATE TABLE product;
TRUNCATE TABLE product_category;
TRUNCATE TABLE customer_address;
TRUNCATE TABLE customer;
TRUNCATE TABLE company_info;
TRUNCATE TABLE sys_user;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO sys_user (id, username, password, real_name, status, create_time, update_time)
VALUES
(1, 'admin', '$2a$10$xjdLSFgtaUjL9ouJSkVzb.HMdXInsQubR9ly3SiSThK/ZL42q7JBu', '系统管理员', 1, NOW(), NOW()),
(2, 'content', '$2a$10$xjdLSFgtaUjL9ouJSkVzb.HMdXInsQubR9ly3SiSThK/ZL42q7JBu', '内容运营', 1, NOW(), NOW()),
(3, 'service', '$2a$10$xjdLSFgtaUjL9ouJSkVzb.HMdXInsQubR9ly3SiSThK/ZL42q7JBu', '客服专员', 1, NOW(), NOW());

INSERT INTO sys_role (id, role_code, role_name, description, status, create_time, update_time)
VALUES
(1, 'SUPER_ADMIN', '超级管理员', '拥有后台所有菜单和操作权限', 1, NOW(), NOW()),
(2, 'CONTENT_MANAGER', '内容管理员', '维护企业信息、轮播图、企业新闻和商品展示内容', 1, NOW(), NOW()),
(3, 'SERVICE_MANAGER', '客服管理员', '处理会员订单、售后协商和咨询留言', 1, NOW(), NOW()),
(4, 'ORDER_MANAGER', '订单管理员', '查看订单并维护订单服务状态', 1, NOW(), NOW());

INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, method, sort, status, create_time, update_time)
VALUES
(1, 'dashboard:view', '查看仪表盘', 'MENU', NULL, '/api/admin/dashboard', 'GET', 10, 1, NOW(), NOW()),
(2, 'company:view', '查看企业信息', 'API', NULL, '/api/admin/company', 'GET', 20, 1, NOW(), NOW()),
(3, 'company:update', '更新企业信息', 'API', NULL, '/api/admin/company', 'PUT', 21, 1, NOW(), NOW()),
(4, 'banner:view', '查看轮播图', 'API', NULL, '/api/admin/banners', 'GET', 30, 1, NOW(), NOW()),
(5, 'banner:create', '新增轮播图', 'BUTTON', 4, '/api/admin/banners', 'POST', 31, 1, NOW(), NOW()),
(6, 'banner:update', '更新轮播图', 'BUTTON', 4, '/api/admin/banners/{id}', 'PUT', 32, 1, NOW(), NOW()),
(7, 'banner:delete', '删除轮播图', 'BUTTON', 4, '/api/admin/banners/{id}', 'DELETE', 33, 1, NOW(), NOW()),
(8, 'news:view', '查看企业新闻', 'API', NULL, '/api/admin/news', 'GET', 40, 1, NOW(), NOW()),
(9, 'news:create', '新增企业新闻', 'BUTTON', 8, '/api/admin/news', 'POST', 41, 1, NOW(), NOW()),
(10, 'news:update', '更新企业新闻', 'BUTTON', 8, '/api/admin/news/{id}', 'PUT', 42, 1, NOW(), NOW()),
(11, 'news:delete', '删除企业新闻', 'BUTTON', 8, '/api/admin/news/{id}', 'DELETE', 43, 1, NOW(), NOW()),
(12, 'product:view', '查看商品', 'API', NULL, '/api/admin/products', 'GET', 50, 1, NOW(), NOW()),
(13, 'product:create', '新增商品', 'BUTTON', 12, '/api/admin/products', 'POST', 51, 1, NOW(), NOW()),
(14, 'product:update', '更新商品', 'BUTTON', 12, '/api/admin/products/{id}', 'PUT', 52, 1, NOW(), NOW()),
(15, 'product:delete', '删除商品', 'BUTTON', 12, '/api/admin/products/{id}', 'DELETE', 53, 1, NOW(), NOW()),
(16, 'order:view', '查看订单', 'API', NULL, '/api/admin/orders', 'GET', 60, 1, NOW(), NOW()),
(17, 'return:view', '查看售后协商', 'API', NULL, '/api/admin/returns', 'GET', 70, 1, NOW(), NOW()),
(18, 'return:handle', '处理售后协商', 'BUTTON', 17, '/api/admin/returns/{id}/handle', 'PUT', 71, 1, NOW(), NOW()),
(19, 'message:view', '查看咨询留言', 'API', NULL, '/api/admin/messages', 'GET', 80, 1, NOW(), NOW()),
(20, 'message:handle', '回复咨询留言', 'BUTTON', 19, '/api/admin/messages/{id}/reply', 'PUT', 81, 1, NOW(), NOW()),
(21, 'message:delete', '删除咨询留言', 'BUTTON', 19, '/api/admin/messages/{id}', 'DELETE', 82, 1, NOW(), NOW()),
(22, 'upload:image', '上传图片', 'API', NULL, '/api/upload', 'POST', 90, 1, NOW(), NOW()),
(23, 'rbac:view', '查看权限配置', 'API', NULL, '/api/admin/rbac', 'GET', 100, 1, NOW(), NOW()),
(24, 'rbac:update', '维护权限配置', 'BUTTON', 23, '/api/admin/rbac', 'PUT', 101, 1, NOW(), NOW());

INSERT INTO sys_user_role (user_id, role_id, create_time)
VALUES
(1, 1, NOW()),
(2, 2, NOW()),
(3, 3, NOW());

INSERT INTO sys_role_permission (role_id, permission_id, create_time)
SELECT 1, id, NOW() FROM sys_permission;

INSERT INTO sys_role_permission (role_id, permission_id, create_time)
SELECT 2, id, NOW() FROM sys_permission
WHERE permission_code IN (
  'dashboard:view', 'company:view', 'company:update',
  'banner:view', 'banner:create', 'banner:update', 'banner:delete',
  'news:view', 'news:create', 'news:update', 'news:delete',
  'product:view', 'product:create', 'product:update', 'product:delete',
  'upload:image'
);

INSERT INTO sys_role_permission (role_id, permission_id, create_time)
SELECT 3, id, NOW() FROM sys_permission
WHERE permission_code IN (
  'dashboard:view', 'order:view', 'return:view', 'return:handle',
  'message:view', 'message:handle', 'message:delete'
);

INSERT INTO sys_role_permission (role_id, permission_id, create_time)
SELECT 4, id, NOW() FROM sys_permission
WHERE permission_code IN ('dashboard:view', 'order:view', 'return:view');

INSERT INTO company_info (id, company_name, intro, culture, phone, email, address, logo, website, service_time, update_time)
VALUES (
  1,
  '优品生活',
  '优品生活是一家面向家庭、厨房、办公与健康场景的商品公司，主营小家电、日用百货、清洁收纳和健康生活类商品。',
  '坚持严选供应链、透明价格、快速履约和可追踪售后，让用户买得清楚、用得安心、协商有记录。',
  '400-800-2026',
  'service@example.com',
  '上海市浦东新区优品产业园',
  '',
  'https://www.example.com',
  '周一至周日 09:00-21:00',
  NOW()
);

INSERT INTO banner (id, title, subtitle, image, link, sort, status, create_time, update_time)
VALUES
(1, '优品生活', '精选生活商品，支持在线购买、订单查询与售后协商', 'https://images.unsplash.com/photo-1556742502-ec7c0e9f34b1?auto=format&fit=crop&w=1800&q=80', '/product', 1, 1, NOW(), NOW()),
(2, '厨房轻食新品上架', '便携料理机、恒温饭盒、保鲜套装进入春季新品专区', 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=1800&q=80', '/news', 2, 1, NOW(), NOW()),
(3, '售后协商在线化', '登录会员账号即可查看订单并提交退换货或配件补发诉求', 'https://images.unsplash.com/photo-1556745757-8d76bdb6984b?auto=format&fit=crop&w=1800&q=80', '/orders', 3, 1, NOW(), NOW());

INSERT INTO product_category (id, name, parent_id, sort, status, create_time, update_time)
VALUES
(1, '厨房电器', NULL, 10, 1, NOW(), NOW()),
(2, '健康家居', NULL, 20, 1, NOW(), NOW()),
(3, '清洁护理', NULL, 30, 1, NOW(), NOW()),
(4, '收纳整理', NULL, 40, 1, NOW(), NOW()),
(5, '厨房用品', NULL, 50, 1, NOW(), NOW());

INSERT INTO news (id, title, summary, content, author, cover_image, tags, source, view_count, sort, status, create_time, update_time)
VALUES
(1, '售后协商服务增加换货和配件补发通道', '用户可在订单记录中选择退货、换货、补发配件或其他协商类型，处理结果可追踪。',
'<p>为提升商品售后处理效率，优品生活将售后协商入口细分为退货、换货、补发配件和其他协商。用户登录后可在订单记录中选择具体诉求，并补充问题描述，后台客服根据订单状态统一处理。</p><figure><img src="https://images.unsplash.com/photo-1556745757-8d76bdb6984b?auto=format&fit=crop&w=1200&q=80" alt="客服处理售后信息"><figcaption>客服工作台集中查看订单、协商类型和用户说明。</figcaption></figure><h2>处理结果可追踪</h2><p>管理员处理后，用户可以在售后协商记录中查看状态和回复。对于配件缺失、运输破损等问题，平台会优先核对出库清单和商品批次，减少重复沟通。</p>', '客服中心', 'https://images.unsplash.com/photo-1556745757-8d76bdb6984b?auto=format&fit=crop&w=1200&q=80', '售后,换货,配件补发', '优品生活', 318, 100, 1, NOW() - INTERVAL 1 HOUR, NOW()),
(2, '厨房轻食新品体验日在旗舰展厅举行', '旗舰展厅围绕早餐、办公室午餐和露营轻食三个场景展示新品。',
'<p>优品生活在旗舰展厅举办厨房轻食新品体验日，集中展示便携料理机、恒温电热饭盒和硅胶保鲜套装。现场围绕早餐准备、办公室午餐和露营轻食三个场景，邀请用户体验商品容量、清洗方式和携带便利性。</p><figure><img src="https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=1200&q=80" alt="厨房台面上的轻食商品"><figcaption>新品体验区按家庭厨房和办公室午餐场景布置。</figcaption></figure><h2>反馈进入商品迭代</h2><p>体验日收集到的杯体重量、饭盒分区和刀头清洗建议，将进入下一批商品采购和质检清单。</p>', '品牌市场部', 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=1200&q=80', '新品,体验日,厨房', '优品生活', 246, 95, 1, NOW() - INTERVAL 2 HOUR, NOW()),
(3, '上海仓配中心完成智能分拣升级', '上海仓配中心新增按订单类型、商品场景和售后优先级分流的作业线。',
'<p>优品生活上海仓配中心完成智能分拣升级，新增按订单类型、商品温控需求和售后换货优先级进行分流的作业线。升级后，小家电、清洁护理和厨房轻食类商品可以在出库前完成批次复核、外观抽检和包装确认。</p><figure><img src="https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=1200&q=80" alt="仓配中心货架与分拣区域"><figcaption>仓配中心通过分区货架和条码复核提升出库准确率。</figcaption></figure><h2>订单履约更透明</h2><p>系统会在订单生成后记录商品批次、出库状态和售后协商入口，便于用户和客服共同追踪。</p>', '仓配中心', 'https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=1200&q=80', '仓配,履约,订单', '优品生活', 291, 90, 1, NOW() - INTERVAL 3 HOUR, NOW()),
(4, '春季生活小家电系列正式发布', '春季生活小家电系列覆盖早餐料理、随身饮水、空气净化和桌面收纳等高频场景。',
'<p>优品生活发布春季生活小家电系列，覆盖早餐料理、随身饮水、空气净化和桌面收纳等高频场景。新品统一采用可追踪批次管理，商品详情页展示价格、库存、标签和售后入口。</p><figure><img src="https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=1200&q=80" alt="新品陈列"><figcaption>春季新品在商品详情页同步展示价格、库存与售后入口。</figcaption></figure>', '商品中心', 'https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=1200&q=80', '新品,小家电,春季上新', '优品生活', 208, 80, 1, NOW() - INTERVAL 1 DAY, NOW());

INSERT INTO news_media (news_id, media_type, url, caption, sort, create_time)
VALUES
(1, 'IMAGE', 'https://images.unsplash.com/photo-1556745757-8d76bdb6984b?auto=format&fit=crop&w=1200&q=80', '客服处理售后协商', 1, NOW()),
(1, 'IMAGE', 'https://images.unsplash.com/photo-1556740758-90de374c12ad?auto=format&fit=crop&w=1200&q=80', '售后记录留痕', 2, NOW()),
(2, 'IMAGE', 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=1200&q=80', '厨房轻食体验', 1, NOW()),
(3, 'IMAGE', 'https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?auto=format&fit=crop&w=1200&q=80', '仓配中心分拣', 1, NOW());

INSERT INTO product (id, sku, name, description, image, category, category_id, tags, price, original_price, stock, unit, sales_count, weight_grams, detail, status, create_time, update_time)
VALUES
(1, 'YP-CUP-450', '智能恒温杯', '316不锈钢内胆，触控温显，适合通勤、会议和长时间办公饮水。', '/uploads/485c6e70c4574811bc8fc429be392415.jpg', '健康家居', 2, '新品,保温,办公', 129.00, 159.00, 296, '个', 124, 420, '容量450ml，Type-C充电，杯盖显示实时水温。杯体采用双层真空结构，减少外壁烫手问题。', 1, NOW() - INTERVAL 8 DAY, NOW()),
(2, 'YP-BLEND-300', '便携料理机', '轻量杯体与安全锁结构，适合果汁、奶昔、辅食和健身餐制作。', '/uploads/6845ac9b895f4e71a820be6612b1c3b6.jpg', '厨房电器', 1, '热销,厨房,便携', 199.00, 239.00, 176, '台', 208, 780, '300ml轻量杯体，6叶刀头，双击启动，杯身可拆洗。未正确安装时无法启动。', 1, NOW() - INTERVAL 7 DAY, NOW()),
(3, 'YP-AIR-DESK', '桌面空气净化器', '小体积低噪音设计，适合卧室、书桌、办公位和儿童学习桌。', '/uploads/8152e4015e2442dc9dad45630bb4731b.jpg', '健康家居', 2, '空气,桌面,健康', 269.00, 299.00, 118, '台', 77, 980, 'HEPA滤芯，三档风量，USB供电，夜间低亮度指示灯。', 1, NOW() - INTERVAL 6 DAY, NOW()),
(4, 'YP-LUNCH-2L', '恒温电热饭盒', '双层分区内胆，支持预约加热，适合上班族午餐和学生宿舍使用。', '/uploads/be114c75927f49358e3fe0b640f6bbfe.jpg', '厨房电器', 1, '饭盒,加热,通勤', 239.00, 279.00, 94, '台', 91, 1100, '304不锈钢内胆，饭菜分区，支持蒸热、保温和防干烧保护。', 1, NOW() - INTERVAL 5 DAY, NOW()),
(5, 'YP-MITE-MINI', '迷你除螨仪', '高频拍打与热风除湿组合，适合床品、沙发、抱枕和布艺座椅清洁。', '/uploads/485c6e70c4574811bc8fc429be392415.jpg', '清洁护理', 3, '除螨,清洁,家居', 329.00, 369.00, 79, '台', 65, 1450, '轻量机身，透明尘杯，支持滤网拆洗。建议每周使用1到2次。', 1, NOW() - INTERVAL 4 DAY, NOW()),
(6, 'YP-CABLE-BOX', '桌面理线收纳盒', '隐藏插排和杂乱线缆，让办公桌、电视柜和床头柜更整洁。', '/uploads/6845ac9b895f4e71a820be6612b1c3b6.jpg', '收纳整理', 4, '收纳,办公,桌面', 59.00, 79.00, 498, '个', 320, 620, '阻燃材质，顶部开槽便于手机充电线穿出，底部留有散热孔。', 1, NOW() - INTERVAL 3 DAY, NOW());

INSERT INTO product_image (product_id, image_url, caption, sort, create_time)
VALUES
(1, '/uploads/485c6e70c4574811bc8fc429be392415.jpg', '智能恒温杯主图', 1, NOW()),
(1, '/uploads/6845ac9b895f4e71a820be6612b1c3b6.jpg', '智能恒温杯细节图', 2, NOW()),
(2, '/uploads/6845ac9b895f4e71a820be6612b1c3b6.jpg', '便携料理机场景图', 1, NOW()),
(2, '/uploads/8152e4015e2442dc9dad45630bb4731b.jpg', '便携料理机细节图', 2, NOW()),
(3, '/uploads/8152e4015e2442dc9dad45630bb4731b.jpg', '桌面空气净化器', 1, NOW()),
(4, '/uploads/be114c75927f49358e3fe0b640f6bbfe.jpg', '恒温饭盒细节图', 1, NOW()),
(5, '/uploads/485c6e70c4574811bc8fc429be392415.jpg', '清洁护理场景', 1, NOW()),
(6, '/uploads/6845ac9b895f4e71a820be6612b1c3b6.jpg', '桌面收纳场景', 1, NOW());

INSERT INTO customer (id, username, password, phone, email, default_address, status, create_time, update_time)
VALUES
(1, 'alice', '$2a$10$xjdLSFgtaUjL9ouJSkVzb.HMdXInsQubR9ly3SiSThK/ZL42q7JBu', '13800010001', 'alice@example.com', '上海市浦东新区世纪大道100号', 1, NOW() - INTERVAL 10 DAY, NOW()),
(2, 'bob', '$2a$10$xjdLSFgtaUjL9ouJSkVzb.HMdXInsQubR9ly3SiSThK/ZL42q7JBu', '13800010002', 'bob@example.com', '杭州市西湖区文三路88号', 1, NOW() - INTERVAL 8 DAY, NOW()),
(3, 'chen', '$2a$10$xjdLSFgtaUjL9ouJSkVzb.HMdXInsQubR9ly3SiSThK/ZL42q7JBu', '13800010003', 'chen@example.com', '南京市建邺区云龙山路66号', 1, NOW() - INTERVAL 6 DAY, NOW());

INSERT INTO customer_address (id, customer_id, receiver_name, phone, province, city, district, detail_address, is_default, create_time, update_time)
VALUES
(1, 1, 'Alice', '13800010001', '上海市', '上海市', '浦东新区', '世纪大道100号', 1, NOW() - INTERVAL 10 DAY, NOW()),
(2, 2, 'Bob', '13800010002', '浙江省', '杭州市', '西湖区', '文三路88号', 1, NOW() - INTERVAL 8 DAY, NOW()),
(3, 3, '陈同学', '13800010003', '江苏省', '南京市', '建邺区', '云龙山路66号', 1, NOW() - INTERVAL 6 DAY, NOW());

INSERT INTO product_order (id, order_no, main_order_no, sub_order_no, shop_id, shop_name, customer_id, customer_name, product_id, sku_id, product_name, price, quantity, product_discount_amount, order_discount_amount, total_amount, contact_phone, address, payment_method, paid_time, logistics_no, remark, status, is_reviewed, finish_time, create_time, update_time)
VALUES
(1, 'PO202604290001', 'MO202604290001', 'SO202604290001-01', 1, '默认店铺', 1, 'alice', 1, 0, '智能恒温杯', 129.00, 2, 0.00, 0.00, 258.00, '13800010001', '上海市浦东新区世纪大道100号', 'SIMULATED', NOW() - INTERVAL 5 DAY, 'YT202604290001', '工作日配送', 'WAIT_BUYER_CONFIRM_GOODS', 0, NULL, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(2, 'PO202604290002', 'MO202604290002', 'SO202604290002-01', 1, '默认店铺', 1, 'alice', 2, 0, '便携料理机', 199.00, 1, 0.00, 0.00, 199.00, '13800010001', '上海市浦东新区世纪大道100号', 'SIMULATED', NOW() - INTERVAL 4 DAY, 'YT202604290002', '需要发票', 'RETURNING', 0, NULL, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 3 DAY),
(3, 'PO202604290003', 'MO202604290003', 'SO202604290003-01', 1, '默认店铺', 2, 'bob', 6, 0, '桌面理线收纳盒', 59.00, 2, 0.00, 0.00, 118.00, '13800010002', '杭州市西湖区文三路88号', 'SIMULATED', NOW() - INTERVAL 3 DAY, 'YT202604290003', '', 'AFTER_SALES_FINISHED', 0, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY);

INSERT INTO return_request (id, order_id, order_no, customer_id, customer_name, product_name, request_type, reason, evidence_image, evidence_media, status, reply, handle_time, create_time, update_time)
VALUES
(1, 2, 'PO202604290002', 1, 'alice', '便携料理机', 'EXCHANGE', '杯体运输途中有轻微裂痕，希望更换同款杯体。', 'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?auto=format&fit=crop&w=1200&q=80', NULL, 'APPLY_PENDING', '已收到申请，客服正在核对出库批次和物流记录。', NULL, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY),
(2, 3, 'PO202604290003', 2, 'bob', '桌面理线收纳盒', 'RETURN_REFUND', '尺寸与电视柜不匹配，申请退货。', NULL, NULL, 'COMPLETED', '已同意退货，请保持商品和配件完整，等待客服发送退货地址。', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY);

INSERT INTO message (id, username, phone, email, content, source, status, reply, create_time, update_time)
VALUES
(1, '林女士', '13900020001', 'lin@example.com', '想咨询企业批量采购恒温电热饭盒是否有阶梯价格。', 'WEB', 0, NULL, NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 6 HOUR),
(2, '王先生', '13900020002', 'wang@example.com', '请问智能恒温杯是否支持公司礼品定制包装？', 'WEB', 1, '已电话沟通，批量定制需提供数量和交付日期。', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 12 HOUR),
(3, '周同学', '13900020003', 'zhou@example.com', '桌面空气净化器滤芯多久更换一次？', 'WEB', 0, NULL, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY);

INSERT INTO operation_log (id, operator_id, operator_name, operator_type, action, request_method, request_uri, module, target_type, target_id, ip_address, user_agent, status_code, result_status, error_message, duration_ms, create_time)
VALUES
(1, 1, 'admin', 'ADMIN', 'CREATE', 'POST', '/api/admin/news', 'news', 'news', '1', '127.0.0.1', 'seed-data', 200, 'SUCCESS', NULL, 36, NOW() - INTERVAL 2 DAY),
(2, 2, 'content', 'ADMIN', 'UPDATE', 'PUT', '/api/admin/products/2', 'products', 'products', '2', '127.0.0.1', 'seed-data', 200, 'SUCCESS', NULL, 42, NOW() - INTERVAL 1 DAY),
(3, 3, 'service', 'ADMIN', 'UPDATE', 'PUT', '/api/admin/returns/2', 'returns', 'returns', '2', '127.0.0.1', 'seed-data', 200, 'SUCCESS', NULL, 29, NOW() - INTERVAL 12 HOUR);
