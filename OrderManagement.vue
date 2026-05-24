<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi, type OrderInfo } from '@/api/order'

const orders = ref<OrderInfo[]>([])
const loading = ref(false)

const loadOrders = async () => {
  try {
    loading.value = true
    const res = await orderApi.getList()
    orders.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleCancel = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await orderApi.cancel(id)
    ElMessage.success('取消成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const getStatusText = (status?: number) => {
  switch (status) {
    case 0: return '待签到'
    case 1: return '使用中'
    case 2: return '已完成'
    case 3: return '已取消'
    default: return '未知'
  }
}

const getStatusType = (status?: number) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'info'
    case 3: return 'danger'
    default: return 'info'
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<template>
  <div class="content">
    <el-card>
      <el-table :data="orders" v-loading="loading" stripe>
        <el-table-column prop="id" label="订单ID" width="100" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="seatId" label="座位ID" width="100" />
        <el-table-column prop="startTime" label="预约开始" />
        <el-table-column prop="endTime" label="预约结束" />
        <el-table-column prop="duration" label="时长(小时)" width="120" />
        <el-table-column prop="price" label="金额" width="120">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 0"
              link 
              type="warning" 
              @click="handleCancel(row.id!)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.content {
  padding: 20px;
}
</style>
