import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './styles/theme.css'
import './styles/form.css'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import VueMarkdownEditor from '@kangc/v-md-editor'
import '@kangc/v-md-editor/lib/style/base-editor.css'
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js'
import '@kangc/v-md-editor/lib/theme/style/github.css'
import hljs from 'highlight.js'

// 初始化主题
const dark = globalThis.matchMedia('(prefers-color-scheme: dark)').matches
document.documentElement.dataset.theme = dark ? 'dark' : 'light'

const app = createApp(App)

app.use(createPinia())
app.use(router)
VueMarkdownEditor.use(githubTheme, {
  Hljs: hljs,
})

app.use(ElementPlus, { locale: zhCn })
app.use(VueMarkdownEditor)

app.mount('#app')
