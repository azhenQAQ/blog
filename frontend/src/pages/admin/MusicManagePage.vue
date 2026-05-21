<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMusicByPage, addMusic, updateMusic, deleteMusic } from '@/api/modules/music'
import type { MusicVO, MusicQueryRequest } from '@/types/music'
import type { UploadUserFile, UploadFile } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

defineOptions({ name: 'MusicManagePage' })

const loading = ref(false)
const musicList = ref<MusicVO[]>([])
const total = ref(0)

const queryForm = reactive<MusicQueryRequest>({
  keyword: '',
  current: 1,
  pageSize: 10,
})

// 弹窗状态
const dialogVisible = ref(false)
const dialogTitle = ref('新建音乐')
const dialogLoading = ref(false)
const editingMusic = reactive({
  id: 0,
  title: '',
  artist: '',
  sortOrder: 0,
})

// 上传文件
const audioFile = ref<UploadUserFile | null>(null)
const coverFile = ref<UploadUserFile | null>(null)
const lrcFile = ref<UploadUserFile | null>(null)

async function fetchList() {
  loading.value = true
  try {
    const page = await listMusicByPage({ ...queryForm })
    musicList.value = page.records ?? []
    total.value = page.total ?? 0
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '未知错误'
    ElMessage.error(`获取音乐列表失败，${msg}`)
    musicList.value = []
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
  queryForm.keyword = ''
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

function openCreateDialog() {
  dialogTitle.value = '新建音乐'
  editingMusic.id = 0
  editingMusic.title = ''
  editingMusic.artist = ''
  editingMusic.sortOrder = 0
  audioFile.value = null
  coverFile.value = null
  lrcFile.value = null
  dialogVisible.value = true
}

function openEditDialog(music: MusicVO) {
  dialogTitle.value = '编辑音乐'
  editingMusic.id = music.id
  editingMusic.title = music.title
  editingMusic.artist = music.artist
  editingMusic.sortOrder = music.sortOrder
  audioFile.value = null
  coverFile.value = null
  lrcFile.value = null
  dialogVisible.value = true
}

// 文件上传处理
function handleAudioChange(uploadFile: UploadFile) {
  const rawFile = uploadFile.raw
  if (!rawFile) return
  const validTypes = ['audio/mpeg', 'audio/mp3', 'audio/aac', 'audio/wav']
  if (!validTypes.includes(rawFile.type) && !rawFile.name.endsWith('.mp3') && !rawFile.name.endsWith('.aac')) {
    ElMessage.error('只能上传 mp3 或 aac 格式的音频文件')
    return
  }
  audioFile.value = uploadFile as unknown as UploadUserFile
}

function handleCoverChange(uploadFile: UploadFile) {
  const rawFile = uploadFile.raw
  if (!rawFile) return
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }
  coverFile.value = uploadFile as unknown as UploadUserFile
}

function handleLrcChange(uploadFile: UploadFile) {
  const rawFile = uploadFile.raw
  if (!rawFile) return
  if (!rawFile.name.endsWith('.lrc') && !rawFile.name.endsWith('.txt')) {
    ElMessage.error('只能上传 .lrc 或 .txt 格式的歌词文件')
    return
  }
  lrcFile.value = uploadFile as unknown as UploadUserFile
}

function handleAudioRemove() {
  audioFile.value = null
}

function handleCoverRemove() {
  coverFile.value = null
}

function handleLrcRemove() {
  lrcFile.value = null
}

async function handleSave() {
  if (!editingMusic.title.trim()) {
    ElMessage.warning('请输入歌名')
    return
  }
  if (editingMusic.title.length > 200) {
    ElMessage.warning('歌名不能超过200个字符')
    return
  }
  if (editingMusic.artist.length > 200) {
    ElMessage.warning('歌手名不能超过200个字符')
    return
  }

  if (!editingMusic.id && !audioFile.value) {
    ElMessage.warning('请上传音频文件')
    return
  }

  dialogLoading.value = true
  try {
    const formData = new FormData()
    if (editingMusic.id) {
      formData.append('id', String(editingMusic.id))
    }
    formData.append('title', editingMusic.title)
    if (editingMusic.artist) {
      formData.append('artist', editingMusic.artist)
    }
    if (editingMusic.sortOrder !== undefined) {
      formData.append('sortOrder', String(editingMusic.sortOrder))
    }
    if (audioFile.value?.raw) {
      formData.append('audioFile', audioFile.value.raw)
    }
    if (coverFile.value?.raw) {
      formData.append('coverFile', coverFile.value.raw)
    }
    if (lrcFile.value?.raw) {
      formData.append('lrcFile', lrcFile.value.raw)
    }

    if (editingMusic.id) {
      await updateMusic(formData)
      ElMessage.success('音乐已更新')
    } else {
      await addMusic(formData)
      ElMessage.success('音乐已创建')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '保存失败'
    ElMessage.error(msg)
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(music: MusicVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除音乐「${music.title}」吗？`,
      '确认删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await deleteMusic(music.id)
    ElMessage.success('音乐已删除')
    fetchList()
  } catch {
    // 用户取消
  }
}

function formatDuration(seconds: number): string {
  if (!seconds) return '--:--'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="music-manage">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="关键字">
          <el-input
            v-model="queryForm.keyword"
            placeholder="歌名模糊搜索"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openCreateDialog">新建音乐</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="musicList"
        stripe
        :border="true"
        style="width: 100%"
        :header-cell-style="{
          background: 'var(--el-bg-color-overlay, #fff)',
          color: 'var(--el-text-color-secondary, #999)',
        }"
        empty-text="暂无音乐数据"
      >
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="封面" width="80" align="center">
          <template #default="{ row }">
            <div class="cover-wrapper">
              <img v-if="row.coverUrl" :src="row.coverUrl" class="music-cover" alt="" />
              <div v-else class="music-cover-placeholder">
                <el-icon :size="24"><UploadFilled /></el-icon>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="歌名" min-width="180" />
        <el-table-column prop="artist" label="歌手" min-width="120" />
        <el-table-column label="时长" width="100" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.duration) }}
          </template>
        </el-table-column>
        <el-table-column label="歌词" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.lrcUrl" type="success" size="small">有</el-tag>
            <el-tag v-else type="info" size="small">无</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      @close="dialogVisible = false"
    >
      <el-form label-width="80px">
        <el-form-item label="歌名" required>
          <el-input
            v-model="editingMusic.title"
            placeholder="歌曲名称"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="歌手">
          <el-input
            v-model="editingMusic.artist"
            placeholder="歌手名称"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="editingMusic.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="音频文件">
          <el-upload
            :auto-upload="false"
            @change="handleAudioChange"
            :on-remove="handleAudioRemove"
            :file-list="audioFile ? [audioFile] : []"
            :limit="1"
            accept="audio/mp3,audio/aac,.mp3,.aac"
          >
            <el-button type="primary">选择音频文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 mp3、aac 格式，编辑时可选</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="封面图片">
          <el-upload
            :auto-upload="false"
            @change="handleCoverChange"
            :on-remove="handleCoverRemove"
            :file-list="coverFile ? [coverFile] : []"
            :limit="1"
            accept="image/*"
          >
            <el-button type="primary">选择封面图片</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 jpg、png 等图片格式，可选</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="歌词文件">
          <el-upload
            :auto-upload="false"
            @change="handleLrcChange"
            :on-remove="handleLrcRemove"
            :file-list="lrcFile ? [lrcFile] : []"
            :limit="1"
            accept=".lrc,.txt"
          >
            <el-button type="primary">选择歌词文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 .lrc 或 .txt 格式的歌词文件，可选</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="handleSave">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.music-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card,
.table-card {
  border: 1px solid var(--el-border-color-light, #e8e8e8);
  border-radius: 8px;
}

.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}

.table-card :deep(.el-card__body) {
  padding: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.cover-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.music-cover {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

.music-cover-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  background: var(--el-fill-color-light, #f5f7fa);
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--el-text-color-placeholder, #c0c4cc);
}
</style>
