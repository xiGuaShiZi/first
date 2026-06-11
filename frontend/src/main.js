/**
 * 文件说明：main.js
 * 作用：应用初始化入口，注册路由、UI 库并挂载根组件。
 * 关键逻辑：
 * 1. 提供项目所需的核心逻辑、数据处理和业务交互。
 * 2. 与路由、UI 组件或 API 服务进行协作，完成页面或模块功能。
 * 3. 所有业务行为、异常处理和状态更新都应在此模块中保持清晰可维护。
 */

import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import router from './router'

createApp(App).use(router).use(ElementPlus).mount('#app')
