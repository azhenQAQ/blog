<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { addComment, listArticleComments, listGuestbook } from '@/api/modules/comment'
import type { CommentVO, CommentAddRequest } from '@/types/comment'
import { COMMENT_TYPE } from '@/types/comment'

const props = withDefaults(
  defineProps<{
    type: 'comment' | 'guestbook'
    articleId?: number | null
  }>(),
  {
    articleId: null,
  },
)

const comments = ref<CommentVO[]>([])
const loading = ref(true)

const form = ref({
  nickname: '',
  email: '',
  content: '',
})

const replyTo = ref<{ id: number; name: string } | null>(null)
const submitting = ref(false)
const errorMsg = ref('')

const count = computed(() => comments.value.length)
const title = computed(() => (props.type === 'guestbook' ? '留言板' : '评论'))
const maxLength = 500

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function getAvatarLetter(name: string): string {
  return name?.charAt(0)?.toUpperCase() || '?'
}

function validateEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

async function fetchComments() {
  loading.value = true
  try {
    if (props.type === 'guestbook') {
      comments.value = await listGuestbook()
    } else if (props.articleId) {
      comments.value = await listArticleComments(props.articleId)
    }
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  errorMsg.value = ''

  const nickname = form.value.nickname.trim()
  const email = form.value.email.trim()
  const content = form.value.content.trim()

  if (!nickname) {
    errorMsg.value = '请填写昵称'
    return
  }
  if (nickname.length > 50) {
    errorMsg.value = '昵称不能超过50字'
    return
  }
  if (!email) {
    errorMsg.value = '请填写邮箱'
    return
  }
  if (email.length > 100 || !validateEmail(email)) {
    errorMsg.value = '请填写有效的邮箱地址'
    return
  }
  if (!content) {
    errorMsg.value = '请填写内容'
    return
  }
  if (content.length > maxLength) {
    errorMsg.value = `内容不能超过${maxLength}字`
    return
  }

  submitting.value = true
  try {
    const payload: CommentAddRequest = {
      type: props.type,
      nickname,
      email,
      content,
    }
    if (props.type === 'comment' && props.articleId) {
      payload.articleId = props.articleId
    }
    if (replyTo.value) {
      payload.parentId = replyTo.value.id
      payload.replyToName = replyTo.value.name
    }
    await addComment(payload)
    form.value.content = ''
    replyTo.value = null
    await fetchComments()
  } catch (e: any) {
    errorMsg.value = e.message || '提交失败，请稍后再试'
  } finally {
    submitting.value = false
  }
}

function handleReply(comment: CommentVO) {
  replyTo.value = { id: comment.id, name: comment.nickname }
  form.value.content = `@${comment.nickname} `
  errorMsg.value = ''
}

function cancelReply() {
  replyTo.value = null
  form.value.content = ''
  errorMsg.value = ''
}

onMounted(fetchComments)
</script>

<template>
  <div class="comment-section">
    <!-- Form -->
    <div class="comment-form-card">
      <h3 class="form-title">
        {{ replyTo ? `回复 @${replyTo.name}` : `发表${title}` }}
        <button v-if="replyTo" class="cancel-reply" @click="cancelReply">取消回复</button>
      </h3>
      <div class="form-row">
        <input
          v-model="form.nickname"
          type="text"
          class="form-input"
          placeholder="昵称 *"
          maxlength="50"
          autocomplete="off"
        />
        <input
          v-model="form.email"
          type="email"
          class="form-input"
          placeholder="邮箱 *"
          maxlength="100"
          autocomplete="off"
        />
      </div>
      <textarea
        v-model="form.content"
        class="form-textarea"
        :placeholder="replyTo ? `回复 @${replyTo.name}...` : '说点什么...'"
        :maxlength="maxLength"
        rows="3"
      />
      <div class="form-footer">
        <span class="char-count">{{ form.content.length }} / {{ maxLength }}</span>
        <p v-if="errorMsg" class="form-error">{{ errorMsg }}</p>
        <button class="submit-btn" :disabled="submitting" @click="handleSubmit">
          {{ submitting ? '提交中...' : '提交' }}
        </button>
      </div>
    </div>

    <!-- Comment list -->
    <div class="comment-list">
      <h3 class="list-title">{{ title }} ({{ count }})</h3>

      <div v-if="loading" class="list-state">加载中...</div>

      <div v-else-if="count === 0" class="list-state empty">还没有{{ title }}，来抢沙发吧~</div>

      <div v-else class="comment-items">
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="comment-item"
          :class="{ 'is-reply': comment.parentId }"
        >
          <div class="comment-avatar">{{ getAvatarLetter(comment.nickname) }}</div>
          <div class="comment-body">
            <div class="comment-meta">
              <span class="comment-nickname">{{ comment.nickname }}</span>
              <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
            </div>
            <p v-if="comment.replyToName" class="comment-reply-to">
              回复 <span class="reply-target">@{{ comment.replyToName }}</span>
            </p>
            <p class="comment-content">{{ comment.content }}</p>
            <button class="reply-btn" @click="handleReply(comment)">回复</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-section {
  margin-top: 40px;
}

/* Form */
.comment-form-card {
  background: var(--card-bg);
  border: var(--card-border);
  box-shadow: var(--card-shadow);
  padding: 24px;
  margin-bottom: 24px;
}

.form-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: var(--font-heading);
  font-size: 1.05em;
  font-weight: 700;
  color: var(--text-strong);
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.cancel-reply {
  font-size: 0.75em;
  font-weight: 600;
  color: var(--text-muted);
  background: none;
  border: 2px solid var(--shadow-color);
  cursor: pointer;
  padding: 2px 10px;
  transition: all 0.1s ease;
  text-transform: uppercase;
}

.cancel-reply:hover {
  background: #1a1a1a;
  color: #fff;
}

[data-theme='dark'] .cancel-reply:hover {
  background: var(--accent);
}

.form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.form-input {
  flex: 1;
  padding: 10px 14px;
  border: 3px solid var(--shadow-color);
  background: var(--page-bg);
  color: var(--text-main);
  font-size: 0.95em;
  font-family: var(--font-main);
  outline: none;
  transition: border-color 0.1s ease;
}

.form-input:focus {
  border-color: var(--accent);
}

.form-input::placeholder {
  color: var(--text-muted);
}

.form-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 3px solid var(--shadow-color);
  background: var(--page-bg);
  color: var(--text-main);
  font-size: 0.95em;
  font-family: var(--font-main);
  resize: vertical;
  min-height: 80px;
  outline: none;
  transition: border-color 0.1s ease;
}

.form-textarea:focus {
  border-color: var(--accent);
}

.form-textarea::placeholder {
  color: var(--text-muted);
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.char-count {
  font-size: 0.8em;
  color: var(--text-muted);
  margin-right: auto;
}

.form-error {
  font-size: 0.85em;
  color: #e9546b;
  margin: 0;
  font-weight: 600;
}

.submit-btn {
  padding: 8px 24px;
  background: #1a1a1a;
  color: #fff;
  border: 3px solid #1a1a1a;
  font-family: var(--font-heading);
  font-size: 0.9em;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  cursor: pointer;
  box-shadow: 4px 4px 0 var(--accent-yellow);
  transition: all 0.1s ease;
}

.submit-btn:hover:not(:disabled) {
  box-shadow: 2px 2px 0 var(--accent-yellow);
  transform: translate(2px, 2px);
}

[data-theme='dark'] .submit-btn {
  background: var(--accent);
  border-color: var(--accent);
  box-shadow: 4px 4px 0 rgba(255, 87, 34, 0.3);
}

.submit-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

/* List */
.comment-list {
  background: var(--card-bg);
  border: var(--card-border);
  box-shadow: var(--card-shadow);
  padding: 24px;
}

.list-title {
  font-family: var(--font-heading);
  font-size: 1.1em;
  font-weight: 700;
  color: var(--text-strong);
  margin-bottom: 20px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.list-state {
  text-align: center;
  padding: 32px 0;
  color: var(--text-muted);
  font-family: var(--font-heading);
  font-size: 0.95em;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.list-state.empty {
  color: var(--text-muted);
}

/* Items */
.comment-items {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.comment-item {
  display: flex;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 2px solid var(--shadow-color);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-item.is-reply {
  padding-left: 0;
}

.comment-avatar {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  background: #1a1a1a;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-heading);
  font-size: 0.85em;
  font-weight: 700;
  border: 2px solid var(--shadow-color);
}

[data-theme='dark'] .comment-avatar {
  background: var(--accent);
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.comment-nickname {
  font-family: var(--font-heading);
  font-size: 0.9em;
  font-weight: 600;
  color: var(--text-strong);
}

.comment-time {
  font-size: 0.78em;
  color: var(--text-muted);
}

.comment-reply-to {
  font-size: 0.82em;
  color: var(--text-muted);
  margin-bottom: 2px;
}

.reply-target {
  color: var(--accent);
  font-weight: 600;
}

.comment-content {
  font-size: 0.95em;
  color: var(--text-main);
  line-height: 1.8;
  word-break: break-word;
}

.reply-btn {
  margin-top: 6px;
  font-size: 0.78em;
  font-weight: 600;
  color: var(--text-muted);
  background: none;
  border: 2px solid var(--shadow-color);
  cursor: pointer;
  padding: 2px 10px;
  transition: all 0.1s ease;
}

.reply-btn:hover {
  background: var(--accent-yellow);
  color: #1a1a1a;
  border-color: #1a1a1a;
}

[data-theme='dark'] .reply-btn:hover {
  background: var(--accent);
  color: #fff;
  border-color: var(--accent);
}

@media (max-width: 900px) {
  .comment-form-card {
    padding: 16px;
  }

  .form-row {
    flex-direction: column;
    gap: 10px;
  }

  .comment-list {
    padding: 16px;
  }
}
</style>
