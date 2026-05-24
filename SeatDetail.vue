<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { seatApi, type Seat } from '@/api/seat'
import { orderApi } from '@/api/order'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const seat = ref<Seat | null>(null)
const startTime = ref('')
const endTime = ref('')
const loading = ref(false)

const estimatedPrice = ref(0)

const calculatePrice = async () => {
  if (!seat.value?.id || !startTime.value || !endTime.value) {
    estimatedPrice.value = 0
    return
  }
  
  try {
    const res = await orderApi.calculatePrice({
      seatId: seat.value.id,
      startTime: startTime.value,
      endTime: endTime.value,
    })
    estimatedPrice.value = res.data.price || 0
  } catch (error) {
    console.error('计算价格失败:', error)
    estimatedPrice.value = 0
  }
}

const duration = computed(() => {
  if (!startTime.value || !endTime.value) return 0
  const start = new Date(startTime.value)
  const end = new Date(endTime.value)
  return Math.max(0, Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60)))
})

const canAfford = computed(() => {
  const balance = userStore.user?.balance || 0
  return estimatedPrice.value <= balance
})

const isAvailable = computed(() => {
  const status = seat.value?.status
  if (typeof status === 'string') {
    return status === 'AVAILABLE'
  }
  return status === 0
})

const getSeatAttributes = () => {
  const attrs = seat.value?.attributes
  if (!attrs) return []
  
  const attrMap: Record<string, string> = {
    power: '插座',
    window: '靠窗',
    light: '灯',
    usb: 'USB',
    fan: '风扇'
  }
  
  try {
    const parsed = JSON.parse(attrs)
    if (typeof parsed === 'object') {
      return Object.keys(parsed).filter(k => parsed[k]).map(k => attrMap[k] || k)
    }
  } catch {
  }
  
  return attrs.split(',').map(s => {
    const key = s.trim().toLowerCase()
    return attrMap[key] || s.trim()
  }).filter(Boolean)
}

const loadSeat = async () => {
  try {
    loading.value = true
    const id = Number(route.params.id)
    const res = await seatApi.getSeatById(id)
    seat.value = res.data
    
    const now = new Date()
    startTime.value = now.toISOString().slice(0, 16)
    const future = new Date(now.getTime() + 2 * 60 * 60 * 1000)
    endTime.value = future.toISOString().slice(0, 16)
    
    await calculatePrice()
  } catch (error) {
    console.error(error)
    ElMessage.error('加载座位信息失败')
  } finally {
    loading.value = false
  }
}

const handleBook = async () => {
  if (!startTime.value || !endTime.value) {
    ElMessage.warning('请选择预约时间')
    return
  }
  if (duration.value < 1) {
    ElMessage.warning('预约时长至少为1小时')
    return
  }
  if (!canAfford.value) {
    ElMessage.warning('余额不足，请先充值')
    return
  }
  if (!seat.value?.id) return
  
  try {
    loading.value = true
    await orderApi.create({
      seatId: seat.value.id,
      startTime: startTime.value,
      endTime: endTime.value,
    })
    ElMessage.success('预约成功')
    await userStore.fetchUserInfo()
    router.push('/orders')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn()) {
    router.push('/login')
    return
  }
  loadSeat()
})
</script>

<template>
  <div class="seat-detail-container">
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
        <h1>座位详情</h1>
      </div>
    </div>

    <div class="detail-content" v-loading="loading">
      <div class="detail-card" v-if="seat">
        <div class="seat-header">
          <div class="seat-title">
            <h2>座位 {{ seat.seatNumber }}</h2>
            <span class="seat-type-tag">{{ seat.type === 0 ? '普通座' : '靠窗座' }}</span>
          </div>
          <el-tag :type="isAvailable ? 'success' : 'danger'" class="status-tag">
            {{ isAvailable ? '可预订' : '不可用' }}
          </el-tag>
        </div>

        <div class="seat-info-section">
          <h3 class="section-title">座位信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <div class="info-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                  <polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
              </div>
              <div class="info-content">
                <span class="info-label">座位类型</span>
                <span class="info-value">{{ seat.type === 0 ? '普通座' : '靠窗座' }}</span>
              </div>
            </div>
            <div class="info-item">
              <div class="info-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 20h9"></path>
                  <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7.5 19.5a2.121 2.121 0 0 1-3-3z"></path>
                </svg>
              </div>
              <div class="info-content">
                <span class="info-label">价格系数</span>
                <span class="info-value">{{ seat.priceFactor || 1 }} 倍</span>
              </div>
            </div>
            <div class="info-item">
              <div class="info-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"></circle>
                  <polyline points="12 6 12 12 16 14"></polyline>
                </svg>
              </div>
              <div class="info-content">
                <span class="info-label">基础价格</span>
                <span class="info-value">¥10.0/小时</span>
              </div>
            </div>
            <div class="info-item">
              <div class="info-icon price">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="1" x2="12" y2="23"></line>
                  <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                </svg>
              </div>
              <div class="info-content">
                <span class="info-label">实际价格</span>
                <span class="info-value price">¥{{ ((seat.priceFactor || 1) * 10).toFixed(1) }}/小时</span>
              </div>
            </div>
          </div>
          
          <div class="description-box" v-if="seat.description">
            <div class="description-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
                <polyline points="10 9 9 9 8 9"></polyline>
              </svg>
            </div>
            <div class="description-content">
              <span class="description-label">座位描述</span>
              <p class="description-text">{{ seat.description }}</p>
            </div>
          </div>

          <div class="attributes-box" v-if="getSeatAttributes().length > 0">
            <span class="attributes-label">座位属性</span>
            <div class="attributes-list">
              <span 
                v-for="attr in getSeatAttributes()" 
                :key="attr" 
                class="attr-tag"
              >{{ attr }}</span>
            </div>
          </div>
        </div>

        <div class="booking-section">
          <h3 class="section-title">预约时间</h3>
          <el-form label-position="top" class="booking-form">
            <div class="time-picker-row">
              <el-form-item label="开始时间">
                <el-date-picker
                  v-model="startTime"
                  type="datetime"
                  placeholder="选择开始时间"
                  class="time-picker"
                  :disabled="!isAvailable"
                  @change="calculatePrice"
                />
              </el-form-item>
              <div class="time-arrow">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg>
              </div>
              <el-form-item label="结束时间">
                <el-date-picker
                  v-model="endTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  class="time-picker"
                  :disabled="!isAvailable"
                  @change="calculatePrice"
                />
              </el-form-item>
            </div>
          </el-form>

          <div class="price-summary-card">
            <div class="summary-row">
              <div class="summary-item">
                <span class="summary-label">预约时长</span>
                <span class="summary-value">{{ duration }} 小时</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">预计费用</span>
                <span class="summary-value price">¥{{ estimatedPrice.toFixed(2) }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">当前余额</span>
                <span class="summary-value" :class="{ warning: !canAfford }">
                  ¥{{ userStore.user?.balance?.toFixed(2) || '0.00' }}
                </span>
              </div>
            </div>
            
            <div class="balance-warning" v-if="!canAfford && duration > 0">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 9v2m0 4h.01"></path>
                <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z"></path>
              </svg>
              <span>余额不足，请先充值</span>
            </div>
          </div>

          <el-button 
            type="primary" 
            size="large" 
            class="book-btn"
            :disabled="!seat || !isAvailable || duration < 1 || !canAfford"
            :loading="loading"
            @click="handleBook"
          >
            立即预约
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.seat-detail-container {
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

.detail-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
  position: relative;
  z-index: 10;
}

.detail-card {
  background: white;
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

.seat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 36px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.seat-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.seat-title h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.seat-type-tag {
  padding: 6px 16px;
  background: linear-gradient(135deg, #f0f5ff 0%, #e8f0fe 100%);
  color: #667eea;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.status-tag {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 24px 0;
}

.seat-info-section {
  margin-bottom: 40px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fafbfc;
  border-radius: 16px;
  transition: all 0.3s ease;
}

.info-item:hover {
  background: #f5f7fa;
}

.info-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f5ff 0%, #e8f0fe 100%);
  border-radius: 12px;
  color: #667eea;
  flex-shrink: 0;
}

.info-icon.price {
  background: linear-gradient(135deg, #fff5f5 0%, #fff0f0 100%);
  color: #f56c6c;
}

.info-content {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 4px;
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.info-value.price {
  color: #f56c6c;
  font-size: 18px;
}

.description-box {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #fafbfc;
  border-radius: 16px;
  margin-bottom: 20px;
}

.description-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border-radius: 12px;
  color: #4caf50;
  flex-shrink: 0;
}

.description-content {
  flex: 1;
}

.description-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 8px;
  display: block;
}

.description-text {
  margin: 0;
  font-size: 15px;
  color: #555;
  line-height: 1.6;
}

.attributes-box {
  padding: 20px;
  background: #fafbfc;
  border-radius: 16px;
}

.attributes-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 12px;
  display: block;
}

.attributes-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.attr-tag {
  padding: 8px 16px;
  background: linear-gradient(135deg, #f0f5ff 0%, #e8f0fe 100%);
  color: #667eea;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.booking-section {
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.booking-form {
  margin-bottom: 24px;
}

.time-picker-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.time-picker {
  flex: 1;
}

.time-arrow {
  color: #ddd;
  padding: 0 10px;
}

.price-summary-card {
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid #e8ecf0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 6px;
}

.summary-value {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}

.summary-value.price {
  color: #f56c6c;
  font-size: 24px;
}

.summary-value.warning {
  color: #f56c6c;
}

.balance-warning {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #fff5f5;
  border-radius: 10px;
  color: #f56c6c;
  font-size: 14px;
}

.book-btn {
  width: 100%;
  padding: 18px;
  border-radius: 16px;
  font-size: 17px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.book-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.book-btn:disabled {
  background: #e8ecf0;
  color: #999;
  box-shadow: none;
}

@media (max-width: 768px) {
  .header {
    padding: 16px 20px;
  }
  
  .header h1 {
    font-size: 22px;
  }
  
  .detail-content {
    padding: 20px;
  }
  
  .detail-card {
    padding: 24px;
  }
  
  .seat-header {
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .seat-title h2 {
    font-size: 24px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .time-picker-row {
    flex-direction: column;
    gap: 16px;
  }
  
  .time-arrow {
    transform: rotate(90deg);
  }
  
  .summary-row {
    flex-direction: column;
    gap: 16px;
  }
}
</style>