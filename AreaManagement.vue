<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { areaApi, type Area } from '@/api/area'

const areas = ref<Area[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingArea = ref<Area | null>(null)
const areaForm = ref<{
  name: string
  description: string
  status: number
}>({
  name: '',
  description: '',
  status: 1,
})

const loadAreas = async () => {
  try {
    loading.value = true
    const res = await areaApi.getList()
    areas.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingArea.value = null
  areaForm.value = {
    name: '',
    description: '',
    status: 1,
  }
  dialogVisible.value = true
}

const handleEdit = (area: Area) => {
  editingArea.value = { ...area }
  areaForm.value = {
    name: area.name || '',
    description: area.description || '',
    status: area.status === 'ENABLE' || String(area.status) === '1' ? 1 : 0,
  }
  dialogVisible.value = true
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该区域吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await areaApi.delete(id)
    ElMessage.success('删除成功')
    loadAreas()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleSubmit = async () => {
  if (!areaForm.value.name) {
    ElMessage.warning('请输入区域名称')
    return
  }
  try {
    const data = {
      id: editingArea.value?.id,
      name: areaForm.value.name,
      description: areaForm.value.description,
      status: areaForm.value.status === 1 ? 'ENABLE' : 'DISABLE',
    }
    if (editingArea.value?.id) {
      await areaApi.update(data)
      ElMessage.success('更新成功')
    } else {
      await areaApi.create(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadAreas()
  } catch (error) {
    console.error(error)
  }
}

const getStatusText = (status?: number | string) => {
  if (typeof status === 'string') {
    return status === 'ENABLE' ? '启用' : '禁用'
  }
  return status === 1 ? '启用' : '禁用'
}

const getStatusType = (status?: number | string) => {
  if (typeof status === 'string') {
    return status === 'ENABLE' ? 'success' : 'info'
  }
  return status === 1 ? 'success' : 'info'
}

onMounted(() => {
  loadAreas()
})
</script>

<template>
  <div class="content">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增区域
        </el-button>
      </div>

      <el-table :data="areas" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="区域名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id!)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingArea ? '编辑区域' : '新增区域'" width="500px">
      <el-form label-position="top" :model="areaForm">
        <el-form-item label="区域名称">
          <el-input v-model="areaForm.name" placeholder="请输入区域名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="areaForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="areaForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.content {
  padding: 20px;
}
.toolbar {
  margin-bottom: 20px;
}
</style>
