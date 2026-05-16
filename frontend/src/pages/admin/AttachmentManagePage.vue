<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile } from 'element-plus'
import {
  uploadAttachment,
  listAttachmentByPage,
  deleteAttachment,
} from '@/api/modules/attachment'
import type { AttachmentVO, AttachmentQueryRequest } from '@/types/attachment'
import ImageLightbox from '@/components/admin/ImageLightbox.vue'

const loading = ref(false)
const uploading = ref(false)
const attachmentList = ref<AttachmentVO[]>([])
const total = ref(0)

const lightboxVisible = ref(false)
const viewIndex = ref(0)

const imageList = computed(() =>
  attachmentList.value.filter((item) => isImage(item))
)

function handlePreview(item: AttachmentVO) {
  const idx = imageList.value.findIndex((img) => img.id === item.id)
  viewIndex.value = idx >= 0 ? idx : 0
  lightboxVisible.value = true
}

const queryForm = reactive<AttachmentQueryRequest>({
  fileName: '',
  fileType: '',
  current: 1,
  pageSize: 12,
})

async function fetchList() {
  loading.value = true
  try {
    const page = await listAttachmentByPage({ ...queryForm })
    attachmentList.value = page.records ?? []
    total.value = page.total ?? 0
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '未知错误'
    ElMessage.error(`获取附件列表失败，${msg}`)
    attachmentList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.current = 1
  fetchList()
}

function handleReset() {
  queryForm.fileName = ''
  queryForm.fileType = ''
  queryForm.current = 1
  fetchList()
}

function handlePageChange(page: number) {
  queryForm.current = page
  fetchList()
}

function handleSizeChange(size: number) {
  queryForm.pageSize = size
  queryForm.current = 1
  fetchList()
}

async function handleUpload(file: UploadFile) {
  if (!file.raw) return
  uploading.value = true
  try {
    await uploadAttachment(file.raw)
    ElMessage.success('上传成功')
    fetchList()
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '上传失败'
    ElMessage.error(msg)
  } finally {
    uploading.value = false
  }
}

function handleDelete(attachment: AttachmentVO) {
  ElMessageBox.confirm(`确定要删除「${attachment.originalName}」吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteAttachment(attachment.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : '删除失败'
      ElMessage.error(msg)
    }
  }).catch(() => {
    // 用户取消
  })
}

/** 是否为图片 */
function isImage(attachment: AttachmentVO): boolean {
  return attachment.fileType?.startsWith('image/') ?? false
}

/** 文件大小格式化 */
function formatSize(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB'
}

/** 格式化时间 */
function formatDate(dateStr?: string): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/** 文件扩展名标签颜色映射 */
function extTagType(ext: string): string {
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp']
  const docExts = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'md', 'txt', 'csv']
  const archiveExts = ['zip', 'rar']
  if (imageExts.includes(ext)) return ''
  if (docExts.includes(ext)) return 'primary'
  if (archiveExts.includes(ext)) return 'warning'
  return 'info'
}

/** 文件类型选项 */
const fileTypeOptions = [
  { label: '全部', value: '' },
  { label: '图片', value: 'image' },
  { label: '文档', value: 'document' },
  { label: '其他', value: 'other' },
]

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="attachment-manage">
    <!-- 搜索和操作栏 -->
    <el-card class="search-card" shadow="never">
      <div class="search-bar">
        <div class="search-form">
          <el-input
            v-model="queryForm.fileName"
            placeholder="搜索文件名..."
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-model="queryForm.fileType"
            placeholder="文件类型"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="opt in fileTypeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
        <div class="search-actions">
          <el-upload
            :show-file-list="false"
            :before-upload="() => false"
            :on-change="handleUpload"
            accept="*"
          >
            <el-button type="primary" :loading="uploading">
              <el-icon style="margin-right: 6px">
                <svg viewBox="0 0 24 24" width="1em" height="1em">
                  <line x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2" />
                  <line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2" /></svg
              ></el-icon>
              上传文件
            </el-button>
          </el-upload>
        </div>
      </div>
    </el-card>

    <!-- 附件卡片网格 -->
    <el-card class="grid-card" shadow="never">
      <div v-loading="loading" class="grid-wrapper" element-loading-text="加载中...">
        <template v-if="attachmentList.length > 0">
          <el-row :gutter="16">
            <el-col v-for="item in attachmentList" :key="item.id" :span="6">
              <el-card class="attachment-card" shadow="hover" :body-style="{ padding: '0' }">
                <!-- 缩略图区域 -->
                <div class="card-thumb">
                  <img
                    v-if="isImage(item)"
                    :src="item.thumbnailUrl || item.url"
                    :alt="item.originalName"
                    class="thumb-img"
                    loading="lazy"
                  />
                  <div v-else class="thumb-icon">
                    <svg viewBox="0 0 24 24" width="48" height="48" fill="none">
                      <path
                        d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
                        stroke="var(--el-text-color-placeholder, #c0c4cc)"
                        stroke-width="2"
                      />
                      <polyline
                        points="14 2 14 8 20 8"
                        stroke="var(--el-text-color-placeholder, #c0c4cc)"
                        stroke-width="2"
                      />
                      <line
                        x1="16"
                        y1="13"
                        x2="8"
                        y2="13"
                        stroke="var(--el-text-color-placeholder, #c0c4cc)"
                        stroke-width="2"
                      />
                      <line
                        x1="16"
                        y1="17"
                        x2="8"
                        y2="17"
                        stroke="var(--el-text-color-placeholder, #c0c4cc)"
                        stroke-width="2"
                      /></svg
                    >
                    <span class="thumb-ext">{{ item.fileExt?.toUpperCase() }}</span>
                  </div>
                </div>

                <!-- 信息区域 -->
                <div class="card-info">
                  <el-tooltip :content="item.originalName" placement="top" :show-after="500">
                    <p class="card-name">{{ item.originalName }}</p>
                  </el-tooltip>
                  <div class="card-meta">
                    <el-tag
                      :type="extTagType(item.fileExt)"
                      size="small"
                      effect="plain"
                      class="card-ext-tag"
                    >
                      {{ item.fileExt?.toUpperCase() || '—' }}
                    </el-tag>
                    <span class="card-size">{{ formatSize(item.fileSize) }}</span>
                  </div>
                  <div class="card-footer">
                    <span class="card-time">{{ formatDate(item.createdAt) }}</span>
                    <span v-if="item.username" class="card-user">{{ item.username }}</span>
                  </div>
                  <div class="card-actions">
                    <el-tooltip
                      :content="isImage(item) ? '' : '暂不支持预览'"
                      :disabled="isImage(item)"
                      placement="top"
                    >
                      <el-button
                        type="primary"
                        size="small"
                        plain
                        :disabled="!isImage(item)"
                        @click="handlePreview(item)"
                      >
                        查看
                      </el-button>
                    </el-tooltip>
                    <el-button
                      type="danger"
                      size="small"
                      plain
                      @click="handleDelete(item)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </template>

        <el-empty v-else description="暂无附件，点击上方按钮上传" :image-size="120">
          <template #image>
            <svg viewBox="0 0 24 24" width="80" height="80" fill="none">
              <path
                d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
                stroke="var(--el-text-color-disabled, #c0c4cc)"
                stroke-width="1.5"
              />
              <polyline
                points="14 2 14 8 20 8"
                stroke="var(--el-text-color-disabled, #c0c4cc)"
                stroke-width="1.5"
              /></svg
          ></template>
        </el-empty>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <ImageLightbox
      v-model="lightboxVisible"
      :images="imageList.map((img) => img.url ?? '')"
      :initial-index="viewIndex"
    />
  </div>
</template>

<style scoped>
.attachment-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 搜索栏 */
.search-card {
  border: 1px solid var(--el-border-color-light, #e8e8e8);
  border-radius: 8px;
}

.search-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.search-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

/* 卡片网格 */
.grid-card {
  border: 1px solid var(--el-border-color-light, #e8e8e8);
  border-radius: 8px;
  flex: 1;
}

.grid-card :deep(.el-card__body) {
  padding: 20px;
}

.grid-wrapper {
  min-height: 400px;
}

/* 附件卡片 */
.attachment-card {
  margin-bottom: 16px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s, transform 0.2s;
}

.attachment-card:hover {
  transform: translateY(-2px);
}

.card-thumb {
  width: 100%;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light, #f5f7fa);
  overflow: hidden;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.thumb-ext {
  font-size: 0.8em;
  font-weight: 600;
  color: var(--el-text-color-placeholder, #c0c4cc);
  letter-spacing: 1px;
}

/* 卡片信息 */
.card-info {
  padding: 12px;
}

.card-name {
  margin: 0 0 8px 0;
  font-size: 0.9em;
  font-weight: 500;
  color: var(--el-text-color-primary, #303133);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.card-ext-tag {
  flex-shrink: 0;
}

.card-size {
  font-size: 0.82em;
  color: var(--el-text-color-secondary, #909399);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.card-time {
  font-size: 0.78em;
  color: var(--el-text-color-placeholder, #c0c4cc);
}

.card-user {
  font-size: 0.78em;
  color: var(--el-text-color-secondary, #909399);
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
  padding-top: 10px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
