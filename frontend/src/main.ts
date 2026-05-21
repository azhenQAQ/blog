import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useMusicStore } from '@/stores/music'
import '@fontsource/oswald/500.css'
import '@fontsource/oswald/700.css'
import '@fontsource/inter/400.css'
import '@fontsource/inter/600.css'
import '@fontsource/inter/800.css'
import './styles/theme.css'
import './styles/form.css'
import './styles/markdown.css'
import 'highlight.js/styles/github-dark.css'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import VueMarkdownEditor from '@kangc/v-md-editor'
import '@kangc/v-md-editor/lib/style/base-editor.css'
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js'
import '@kangc/v-md-editor/lib/theme/style/github.css'
import hljs from 'highlight.js'

// 初始化主题
const dark = globalThis.matchMedia('(prefers-color-scheme: dark)').matches
document.documentElement.dataset.theme = dark ? 'dark' : 'light'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// 初始化音乐播放器歌单
useMusicStore().fetchPlaylist()
VueMarkdownEditor.use(githubTheme, {
  Hljs: hljs,
})

app.use(ElementPlus, { locale: zhCn })
app.use(VueMarkdownEditor)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
