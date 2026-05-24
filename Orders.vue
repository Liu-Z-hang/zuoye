<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { orderApi, type OrderInfo } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()

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

const getStatusValue = (status?: number | string) => {
  if (typeof status === 'string') {
    const upperStatus = status.toUpperCase().trim()
    switch (upperStatus) {
      case 'PAID':
        return 0
      case 'USING':
        return 1
      case 'FINISHED':
        return 2
      case 'CANCELLED':
      case 'CANCELED':
        return 3
      default:
        return -1
    }
  }
  return status ?? -1
}

const getStatusText = (status?: number | string) => {
  const statusValue = getStatusValue(status)
  switch (statusValue) {
    case 0:
      return '待签到'
    case 1:
      return '使用中'
    case 2:
      return '已完成'
    case 3:
      return '已取消'
    default:
      return '未知'
  }
}

const getStatusType = (status?: number | string) => {
  const statusValue = getStatusValue(status)
  switch (statusValue) {
    case 0:
      return 'warning'
    case 1:
      return 'success'
    case 2:
      return 'info'
    case 3:
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusBgClass = (status?: number | string) => {
  const statusValue = getStatusValue(status)
  switch (statusValue) {
    case 0:
      return 'status-warning'
    case 1:
      return 'status-success'
    case 2:
      return 'status-info'
    case 3:
      return 'status-danger'
    default:
      return 'status-info'
  }
}

const getDuration = (startTime?: string, endTime?: string) => {
  if (!startTime || !endTime) return 0
  const start = new Date(startTime).getTime()
  const end = new Date(endTime).getTime()
  return ((end - start) / (1000 * 60 * 60)).toFixed(1)
}

const handleCancel = async (order: OrderInfo) => {
  if (!order.id) return
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    loading.value = true
    await orderApi.cancel(order.id)
    ElMessage.success('取消成功')
    loadOrders()
    await userStore.fetchUserInfo()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

const handleCheckIn = async (order: OrderInfo) => {
  if (!order.id) return
  try {
    await ElMessageBox.confirm('确定要签到吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success',
    })
    loading.value = true
    await orderApi.checkIn(order.id)
    ElMessage.success('签到成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

const handleCheckOut = async (order: OrderInfo) => {
  if (!order.id) return
  try {
    await ElMessageBox.confirm('确定要签退吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success',
    })
    loading.value = true
    await orderApi.checkOut(order.id)
    ElMessage.success('签退成功')
    loadOrders()
    await userStore.fetchUserInfo()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn()) {
    router.push('/login')
    return
  }
  loadOrders()
})
</script>

<template>
  <div class="orders-container">
    <div class="bg-decoration">
      <div class="deco-circle circle-1"></div>
      <div class="deco-circle circle-2"></div>
    </div>
    
    <div class="header">
      <div class="header-left">
        <el-button @click="$router.back()" class="back-btn">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M15 18l-6-6 6-6"/>
          </svg>
          返回
        </el-button>
        <h1>我的订单</h1>
      </div>
      <div class="header-right">
        <div class="order-count">共 {{ orders.length }} 个订单</div>
      </div>
    </div>

    <div class="orders-content" v-loading="loading">
      <div v-if="orders.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
            <line x1="3" x2="21" y1="6" y2="6"></line>
            <path d="M16 10a4 4 0 0 1-8 0"></path>
          </svg>
        </div>
        <p class="empty-text">暂无订单</p>
        <el-button type="primary" @click="$router.push('/home')" class="empty-btn">去预约座位</el-button>
      </div>
      
      <div v-else class="orders-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <div class="order-id">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="7" height="7"></rect>
                <path d="M3 10h7a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1v-8a1 1 0 0 1 1-1z"></path>
                <path d="M14 3h7a1 1 0 0 1 1 1v18a1 1 0 0 1-1 1h-7a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z"></path>
              </svg>
              <span>订单号：{{ order.id }}</span>
            </div>
            <el-tag :type="getStatusType(order.status)" class="status-tag">
              {{ getStatusText(order.status) }}
            </el-tag>
          </div>
          
          <div class="order-info">
            <div class="info-row">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <span>预约时间：{{ order.startTime }} ~ {{ order.endTime }}</span>
              <span class="duration-badge">时长 {{ getDuration(order.startTime, order.endTime) }} 小时</span>
            </div>
            <div class="info-row" v-if="order.actualStartTime">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <span>实际签到：{{ order.actualStartTime }}</span>
            </div>
            <div class="info-row" v-if="order.actualEndTime">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <span>实际签退：{{ order.actualEndTime }}</span>
            </div>
          </div>
          
          <div class="order-summary">
            <div class="price-section">
              <span class="price-label">费用</span>
              <span class="price-value">¥{{ ((order.totalPrice || 0) as number).toFixed(2) }}</span>
            </div>
            <div class="order-actions">
              <el-button 
                v-if="getStatusValue(order.status) === 0" 
                type="warning" 
                size="small"
                class="action-btn cancel-btn"
                @click="handleCancel(order)"
              >
                取消订单
              </el-button>
              <el-button 
                v-if="getStatusValue(order.status) === 0" 
                type="success" 
                size="small"
                class="action-btn primary-btn"
                @click="handleCheckIn(order)"
              >
                签到
              </el-button>
              <el-button 
                v-if="getStatusValue(order.status) === 1" 
                type="primary" 
                size="small"
                class="action-btn primary-btn"
                @click="handleCheckOut(order)"
              >
                签退
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.orders-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9ff 0%, #f0f2ff 50%, #e8ecff 100%);
  position: relative;
  overflow-x: hidden;
}

.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.5;
}

.deco-circle.circle-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.12) 0%, transparent 70%);
  top: -150px;
  right: -50px;
}

.deco-circle.circle-2 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(118, 75, 162, 0.08) 0%, transparent 70%);
  bottom: -50px;
  left: -50px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 60px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 15px;
  color: #666;
  background: #f5f7fa;
  border: none;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #e8ecf0;
}

.header h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #333;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.order-count {
  font-size: 14px;
  color: #888;
  padding: 8px 20px;
  background: #f5f7fa;
  border-radius: 20px;
}

.orders-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
  position: relative;
  z-index: 10;
}

.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 30px;
}

.empty-icon {
  color: #ddd;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 16px;
  color: #999;
  margin-bottom: 24px;
}

.empty-btn {
  padding: 12px 36px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.order-card {
  background: white;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.order-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.order-id {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
  color: #333;
  font-size: 15px;
}

.order-id svg {
  color: #667eea;
}

.status-tag {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.order-info {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  font-size: 15px;
  color: #555;
}

.info-row svg {
  color: #888;
  flex-shrink: 0;
}

.duration-badge {
  margin-left: auto;
  padding: 4px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 13px;
  border-radius: 12px;
  font-weight: 500;
}

.order-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.price-section {
  display: flex;
  flex-direction: column;
}

.price-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 4px;
}

.price-value {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}

.order-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
}

.cancel-btn {
  background: #fff5f5;
  color: #f56c6c;
  border: 1px solid #feb2b2;
}

.cancel-btn:hover {
  background: #fff0f0;
}

.primary-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.primary-btn:hover {
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

@media (max-width: 768px) {
  .header {
    padding: 16px 20px;
  }
  
  .header h1 {
    font-size: 22px;
  }
  
  .orders-content {
    padding: 20px;
  }
  
  .order-card {
    padding: 20px;
  }
  
  .order-summary {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .order-actions {
    width: 100%;
  }
  
  .action-btn {
    flex: 1;
    text-align: center;
  }
}
</style>