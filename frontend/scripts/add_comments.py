from pathlib import Path

root = Path('src')

# 统一的职责说明，结合路径生成更贴近当前业务的文件说明。
file_purpose = {
    'App.vue': '根组件，负责挂载路由视图并作为应用入口。',
    'main.js': '应用初始化入口，注册路由、UI 库并挂载根组件。',
    'api/modules.js': '统一封装各类 API 调用，集中管理前端接口入口。',
    'api/request.js': '创建 Axios 实例、统一处理鉴权和响应异常。',
    'components/ImageUpload.vue': '图片上传组件，支持本地上传并统一生成可显示的图片地址。',
    'layout/AdminLayout.vue': '后台布局容器，提供管理员侧边栏、顶部入口和通用对话框。',
    'layout/FrontLayout.vue': '前台公共布局，包含导航、底部信息和全局账号管理入口。',
    'router/index.js': '声明前台和后台路由映射，并控制登录鉴权跳转。',
}

# 将页面目录映射为更具体的业务说明。
view_purpose = {
    'About.vue': '平台简介页面，展示校园二手交易平台的定位与价值。',
    'Cart.vue': '交易清单页面，负责购物车数据加载、数量调整、勾选结算和提交订单。',
    'Contact.vue': '交易咨询页面，展示联系方式并收集用户留言。',
    'Home.vue': '首页，聚合 banner、精选闲置物品、校园贴士和平台统计信息。',
    'News.vue': '校园贴士列表页，展示校园经验、使用建议和交易知识。',
    'NewsDetail.vue': '校园贴士详情页，展示单篇贴士正文和封面。',
    'OrderReview.vue': '交易评价页，收集订单评价、评分和上传评价图片或视频。',
    'ProductDetail.vue': '闲置物品详情页，展示图片、详情、评价和购买操作。',
    'Products.vue': '闲置物品列表页，支持搜索、分类筛选、排序和分页。',
    'UserLogin.vue': '学生登录与注册入口页，处理会员认证和验证码。',
    'UserOrders.vue': '我的交易页面，展示订单列表、状态、支付和售后入口。',
    'admin/AdminBanners.vue': '后台 banner 管理页面，维护首页轮播和推广图。',
    'admin/AdminCompany.vue': '后台平台信息管理页面，维护公司/平台展示信息。',
    'admin/AdminMessages.vue': '后台咨询反馈管理页面，处理用户留言。',
    'admin/AdminNews.vue': '后台校园贴士管理页面，维护贴士内容和封面。',
    'admin/AdminOrders.vue': '后台交易订单管理页面，查看并处理订单状态。',
    'admin/AdminProducts.vue': '后台闲置物品管理页面，维护商品信息和图片。',
    'admin/AdminReturns.vue': '后台交易协商管理页面，处理退款、退货、换货或维修协商。',
    'admin/Dashboard.vue': '后台首页仪表盘，汇总平台交易、内容和运营数据。',
    'admin/Login.vue': '后台登录页面，提供管理员认证入口。',
}

# 每个文件生成的注释模板。
for path in sorted(root.rglob('*')):
    if path.suffix not in {'.vue', '.js'}:
        continue

    content = path.read_text(encoding='utf-8')

    # 生成文件说明。
    relative = path.relative_to(root).as_posix()
    if path.suffix == '.js':
        purpose = file_purpose.get(path.name, '通用 JS 模块')
        if relative.startswith('api/'):
            purpose = file_purpose.get(path.name, 'API 模块')
        if relative.startswith('views/admin/') or relative.startswith('views/front/'):
            purpose = '页面脚本模块'
        comment = (
            f"/**\n"
            f" * 文件说明：{path.name}\n"
            f" * 作用：{purpose}\n"
            f" * 关键逻辑：\n"
            f" * 1. 提供项目所需的核心逻辑、数据处理和业务交互。\n"
            f" * 2. 与路由、UI 组件或 API 服务进行协作，完成页面或模块功能。\n"
            f" * 3. 所有业务行为、异常处理和状态更新都应在此模块中保持清晰可维护。\n"
            f" */\n\n"
        )
        if content.startswith('/**'):
            continue
        path.write_text(comment + content, encoding='utf-8')

    else:
        purpose = view_purpose.get(path.name, file_purpose.get(path.name, '前端页面或组件'))
        comment = (
            "<!--\n"
            f"  文件说明：{path.name}\n"
            f"  作用：{purpose}\n"
            "  关键逻辑：\n"
            "  - 负责页面展示、业务交互和状态管理。\n"
            "  - 通过组件、API 和路由完成页面功能闭环。\n"
            "  - 保持模板、样式和脚本职责清晰，便于维护和扩展。\n"
            "-->\n\n"
        )

        if '<script' in content:
            marker = '<script'
            insert_at = content.index(marker)
            if comment.strip() not in content:
                content = content[:insert_at] + comment + content[insert_at:]
                path.write_text(content, encoding='utf-8')
        elif not content.startswith('<!--'):
            path.write_text(comment + content, encoding='utf-8')
