<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import { seatApi, type Seat } from '@/api/seat'
import { areaApi, type Area } from '@/api/area'

const seats = ref<Seat[]>([])
const areas = ref<Area[]>([])
const loading = ref(false)
const selectedAreaId = ref<number | null>(null)
const dialogVisible = ref(false)
const editingSeat = ref<Seat | null>(null)
const seatForm = ref<Seat>({
  seatNumber: '',
  type: 0,
  priceCoefficient: 1.0,
  status: 0,
  description: '',
})

const loadAreas = async () => {
  try {
    const res = await areaApi.getList()
    areas.value = res.data
    if (areas.value.length > 0) {
      selectedAreaId.value = areas.value[0].id || null
      loadSeats()
    }
  } catch (error) {
    console.error(error)
  }
}

const loadSeats = async () => {
  try {
    loading.value = true
    const res = await seatApi.getList(selectedAreaId.value || undefined)
    seats.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingSeat.value = null
  seatForm.value = {
    seatNumber: '',
    areaId: selectedAreaId.value || undefined,
    type: 0,
    priceCoefficient: 1.0,
    status: 0,
    description: '',
  }
  dialogVisible.value = true
}

const handleEdit = (seat: Seat) => {
  editingSeat.value = { ...seat }
  seatForm.value = { ...seat }
  dialogVisible.value = true
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该座位吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await seatApi.delete(id)
    ElMessage.success('删除成功')
    loadSeats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleUpdateStatus = async (seat: Seat, status: number) => {
  try {
    await seatApi.updateStatus(seat.id!, String(status))
    ElMessage.success('状态更新成功')
    loadSeats()
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!seatForm.value.seatNumber) {
    ElMessage.warning('请输入座位编号')
    return
  }
  try {
    if (editingSeat.value?.id) {
      await seatApi.update(seatForm.value)
      ElMessage.success('更新成功')
    } else {
      await seatApi.create(seatForm.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadSeats()
  } catch (error) {
    console.error(error)
  }
}

const getStatusText = (status?: number) => {
  switch (status) {
    case 0: return '空闲'
    case 1: return '已占用'
    case 2: return '维护中'
    default: return '未知'
  }
}

const getStatusType = (status?: number) => {
  switch (status) {
    case 0: return 'success'
    case 1: return 'warning'
    case 2: return 'info'
    default: return 'info'
  }
}

const getTypeText = (type?: number) => {
  return type === 0 ? '普通座' : '靠窗座'
}

onMounted(() => {
  loadAreas()
})
</script>

<template>
  <div class="content">
    <el-card>
      <div class="toolbar">
        <el-select 
          v-model="selectedAreaId" 
          placeholder="选择区域"
          @change="loadSeats"
          style="width: 200px; margin-right: 10px"
        >
          <el-option 
            v-for="area in areas" 
            :key="area.id" 
            :label="area.name" 
            :value="area.id ?? 0" 
          />
        </el-select>
        <el-button type="primary" @click="handleAdd" :disabled="!selectedAreaId">
          <el-icon><Plus /></el-icon>
          新增座位
        </el-button>
      </div>

      <el-table :data="seats" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="seatNumber" label="座位编号" />
        <el-table-column prop="type" label="座位类型" width="100">
          <template #default="{ row }">
            {{ getTypeText(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="priceCoefficient" label="价格系数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-dropdown v-if="row.status !== 2" @command="(cmd) => handleUpdateStatus(row, cmd)">
              <el-button link type="primary">
                更多<el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="2" :disabled="row.status === 2">设为维护</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-dropdown v-else @command="(cmd) => handleUpdateStatus(row, cmd)">
              <el-button link type="success">
                更多<el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="0">恢复空闲</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button link type="danger" @click="handleDelete(row.id!)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingSeat ? '编辑座位' : '新增座位'" width="500px">
      <el-form label-position="top" :model="seatForm">
        <el-form-item label="座位编号">
          <el-input v-model="seatForm.seatNumber" placeholder="请输入座位编号" />
        </el-form-item>
        <el-form-item label="所属区域">
          <el-select v-model="seatForm.areaId" placeholder="请选择区域">
            <el-option 
              v-for="area in areas" 
              :key="area.id" 
              :label="area.name" 
              :value="area.id ?? 0" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="座位类型">
          <el-radio-group v-model="seatForm.type">
            <el-radio :value="0">普通座</el-radio>
            <el-radio :value="1">靠窗座</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="价格系数">
          <el-input-number v-model="seatForm.priceCoefficient" :min="0.5" :max="3" :step="0.1" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="seatForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
