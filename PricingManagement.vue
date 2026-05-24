<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { pricingApi, type PricingRule } from '@/api/pricing'

const rules = ref<PricingRule[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingRule = ref<PricingRule | null>(null)
const ruleForm = ref<{
  startTime: string
  endTime: string
  pricePerHour: number
  priority: number
}>({
  startTime: '08:00',
  endTime: '12:00',
  pricePerHour: 10,
  priority: 1,
})

const loadRules = async () => {
  try {
    loading.value = true
    const res = await pricingApi.getList()
    rules.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingRule.value = null
  ruleForm.value = {
    startTime: '08:00',
    endTime: '12:00',
    pricePerHour: 10,
    priority: 1,
  }
  dialogVisible.value = true
}

const handleEdit = (rule: PricingRule) => {
  editingRule.value = { ...rule }
  ruleForm.value = {
    startTime: rule.startTime || '08:00',
    endTime: rule.endTime || '12:00',
    pricePerHour: rule.pricePerHour || 10,
    priority: rule.priority || 1,
  }
  dialogVisible.value = true
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await pricingApi.delete(id)
    ElMessage.success('删除成功')
    loadRules()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleSubmit = async () => {
  if (!ruleForm.value.startTime || !ruleForm.value.endTime) {
    ElMessage.warning('请选择时间段')
    return
  }
  if (ruleForm.value.pricePerHour <= 0) {
    ElMessage.warning('请输入有效的价格')
    return
  }
  try {
    const data = {
      ...ruleForm.value,
      id: editingRule.value?.id,
    }
    if (editingRule.value?.id) {
      await pricingApi.update(data as PricingRule)
      ElMessage.success('更新成功')
    } else {
      await pricingApi.create(data as PricingRule)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRules()
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadRules()
})
</script>

<template>
  <div class="content">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增规则
        </el-button>
      </div>

      <el-table :data="rules" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="时间段" width="200">
          <template #default="{ row }">
            {{ row.startTime }} - {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="pricePerHour" label="价格(元/小时)" width="150" />
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id!)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingRule ? '编辑规则' : '新增规则'" width="500px">
      <el-form label-position="top" :model="ruleForm">
        <el-form-item label="开始时间">
          <el-time-picker
            v-model="ruleForm.startTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择开始时间"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker
            v-model="ruleForm.endTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="选择结束时间"
          />
        </el-form-item>
        <el-form-item label="价格(元/小时)">
          <el-input-number v-model="ruleForm.pricePerHour" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="ruleForm.priority" :min="0" :max="100" />
          <div class="form-tip">数值越大优先级越高</div>
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
.form-tip {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}
</style>
