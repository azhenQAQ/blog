<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addArticle, editArticle, getArticle } from '@/api/modules/article'
import { listAllTags } from '@/api/modules/tag'
import { uploadAttachment } from '@/api/modules/attachment'
import { ARTICLE_STATUS } from '@/types/article'
import type { TagVO } from '@/types/tag'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)
const pageTitle = computed(() => (isEdit.value ? '编辑文章' : '创建文章'))

const allTags = ref<TagVO[]>([])
const saving = ref(false)
const loadingDetail = ref(false)

const form = reactive({
  id: 0,
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  status: ARTICLE_STATUS.DRAFT,
  isTop: 0 as number,
  tagIds: [] as number[],
})

onMounted(async () => {
  try {
    allTags.value = await listAllTags()
  } catch {
    // 标签加载失败不影响编辑
  }

  if (isEdit.value) {
    loadingDetail.value = true
    try {
      const article = await getArticle(Number(route.params.id))
      form.id = article.id
      form.title = article.title
      form.content = article.content
      form.summary = article.summary || ''
      form.coverImage = article.coverImage || ''
      form.status = article.status
      form.isTop = article.isTop
      form.tagIds = article.tags?.map((t) => t.id) || []
    } catch {
      ElMessage.error('加载文章失败')
      router.push('/admin/posts')
    } finally {
      loadingDetail.value = false
    }
  }
})

function validate(requireContent: boolean): boolean {
  if (!form.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return false
  }
  if (form.title.length > 200) {
    ElMessage.warning('标题不能超过200字')
    return false
  }
  if (requireContent && !form.content.trim()) {
    ElMessage.warning('发布文章需要填写正文内容')
    return false
  }
  // 摘要自动生成
  if (!form.summary.trim() && form.content.trim()) {
    const plainText = form.content
      .replace(/[#*`>[\]()\-_|~]/g, '')
      .replace(/\s+/g, ' ')
      .trim()
    form.summary = plainText.substring(0, 200)
  }
  return true
}

async function handleSaveAsDraft() {
  if (!validate(false)) return
  saving.value = true
  try {
    form.status = ARTICLE_STATUS.DRAFT
    if (isEdit.value) {
      await editArticle({
        id: form.id,
        title: form.title,
        content: form.content,
        summary: form.summary,
        coverImage: form.coverImage,
        status: form.status,
        isTop: form.isTop,
        tagIds: form.tagIds,
      })
    } else {
      await addArticle({
        title: form.title,
        content: form.content,
        summary: form.summary,
        coverImage: form.coverImage,
        status: form.status,
        isTop: form.isTop,
        tagIds: form.tagIds,
      })
    }
    ElMessage.success('已保存为草稿')
    router.push('/admin/posts')
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '保存失败'
    ElMessage.error(msg)
  } finally {
    saving.value = false
  }
}

async function handlePublish() {
  if (!validate(true)) return
  saving.value = true
  try {
    form.status = ARTICLE_STATUS.PUBLISHED
    if (isEdit.value) {
      await editArticle({
        id: form.id,
        title: form.title,
        content: form.content,
        summary: form.summary,
        coverImage: form.coverImage,
        status: form.status,
        isTop: form.isTop,
        tagIds: form.tagIds,
      })
    } else {
      await addArticle({
        title: form.title,
        content: form.content,
        summary: form.summary,
        coverImage: form.coverImage,
        status: form.status,
        isTop: form.isTop,
        tagIds: form.tagIds,
      })
    }
    ElMessage.success('文章已发布')
    router.push('/admin/posts')
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '发布失败'
    ElMessage.error(msg)
  } finally {
    saving.value = false
  }
}

async function handleImageUpload(event: Event, insertImage: (url: string) => void, files: File[]) {
  for (const file of files) {
    try {
      const result = await uploadAttachment(file)
      insertImage(result.url)
    } catch {
      ElMessage.error('图片上传失败')
    }
  }
}

function handleCancel() {
  router.push('/admin/posts')
}

// v-md-editor 配置
const editorOptions = {
  height: '500px',
  placeholder: '开始写作...',
  disabledMenus: [],
}
</script>

<template>
  <div class="post-edit" v-loading="loadingDetail">
    <el-card shadow="never" class="edit-card">
      <template #header>
        <div class="edit-header">
          <span>{{ pageTitle }}</span>
          <div class="edit-actions">
            <el-button @click="handleCancel">取消</el-button>
            <el-button @click="handleSaveAsDraft" :loading="saving">保存草稿</el-button>
            <el-button type="primary" @click="handlePublish" :loading="saving">
              {{ isEdit ? '更新发布' : '发布' }}
            </el-button>
          </div>
        </div>
      </template>

      <el-form label-position="left" label-width="100px" class="edit-form">
        <el-form-item label="标题" required>
          <el-input
            v-model="form.title"
            placeholder="输入文章标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面图（URL）">
          <el-input v-model="form.coverImage" placeholder="输入封面图片URL（可选）" clearable />
        </el-form-item>

        <el-form-item label="标签">
          <el-select
            v-model="form.tagIds"
            multiple
            placeholder="选择标签"
            style="width: 100%"
            clearable
          >
            <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="正文" required>
          <v-md-editor
            v-model="form.content"
            :height="editorOptions.height"
            :disabled-menus="editorOptions.disabledMenus"
            :placeholder="editorOptions.placeholder"
            @upload-image="handleImageUpload"
          />
        </el-form-item>

        <el-form-item label="摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="可选，不填则自动从正文取前200字"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="置顶">
              <el-switch
                v-model="form.isTop"
                :active-value="1"
                :inactive-value="0"
                active-text="是"
                inactive-text="否"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.edit-card {
  border: 1px solid var(--el-border-color-light, #e8e8e8);
  border-radius: 8px;
}

.edit-card :deep(.el-card__body) {
  padding: 20px;
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-header span {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary, #303133);
}

.edit-actions {
  display: flex;
  gap: 8px;
}

.edit-form {
  margin-top: 8px;
}

.edit-form :deep(.v-md-editor) {
  box-shadow: none;
}

.edit-form :deep(.v-md-editor__editor-wrapper) {
  border: 1px solid var(--el-border-color, #dcdfe6);
  border-radius: 4px;
}
</style>
