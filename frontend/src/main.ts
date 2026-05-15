import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './styles/theme.css'
import './styles/form.css'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 初始化主题
const dark = globalThis.matchMedia('(prefers-color-scheme: dark)').matches
document.documentElement.dataset.theme = dark ? 'dark' : 'light'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
