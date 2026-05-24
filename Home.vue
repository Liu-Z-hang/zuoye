<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { seatApi, type Area, type Seat } from '@/api/seat'

const router = useRouter()
const userStore = useUserStore()

const areas = ref<Area[]>([])
const seats = ref<Seat[]>([])
const selectedAreaId = ref<number>()
const loading = ref(false)

const selectedArea = computed((): Area | undefined => {
  return areas.value.find(area => area.id === selectedAreaId.value)
})

const availableCount = computed(() => {
  return seats.value.filter(s => isSeatAvailable(s.status)).length
})

const occupiedCount = computed(() => {
  return seats.value.filter(s => {
    const status = s.status
    if (typeof status === 'string') return status === 'OCCUPIED'
    return status === 1
  }).length
})

const maintenanceCount = computed(() => {
  return seats.value.filter(s => {
    const status = s.status
    if (typeof status === 'string') return status === 'MAINTENANCE'
    return status === 2
  }).length
})

const formatAttribute = (attr: string) => {
  const attrMap: Record<string, string> = {
    power: '插座',
    window: '靠窗',
    light: '灯',
    usb: 'USB',
    fan: '风扇'
  }
  return attrMap[attr.trim().toLowerCase()] || attr.trim()
}

const getSeatAttributeList = (attrs?: string) => {
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

const formatAttributes = (attrs?: string) => {
  if (!attrs) return ''
  return attrs.split(',').map(formatAttribute).slice(0, 2).join(' ')
}

const loadAreas = async () => {
  try {
    loading.value = true
    const res = await seatApi.getAreaList()
    areas.value = res.data
    if (areas.value.length > 0) {
      selectedAreaId.value = areas.value[0]?.id ?? 0
      loadSeats()
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadSeats = async () => {
  try {
    loading.value = true
    const res = await seatApi.getSeatList(selectedAreaId.value)
    seats.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const selectSeat = (seat: Seat) => {
  if (selectedArea.value?.status !== 'ENABLE') {
    ElMessage.warning('该区域已关闭，暂不可预约')
    return
  }
  if (!isSeatAvailable(seat.status)) {
    ElMessage.warning('该座位暂不可用')
    return
  }
  router.push(`/seat/${seat.id}`)
}

const getSeatStatusClass = (status?: number | string) => {
  if (typeof status === 'string') {
    switch (status) {
      case 'AVAILABLE':
        return 'available'
      case 'OCCUPIED':
        return 'occupied'
      case 'MAINTENANCE':
        return 'maintenance'
      default:
        return 'available'
    }
  }
  switch (status) {
    case 0:
      return 'available'
    case 1:
      return 'occupied'
    case 2:
      return 'maintenance'
    default:
      return 'available'
  }
}

const isSeatAvailable = (status?: number | string) => {
  if (typeof status === 'string') {
    return status === 'AVAILABLE'
  }
  return status === 0
}

onMounted(() => {
  if (!userStore.isLoggedIn()) {
    router.push('/login')
    return
  }
  if (!userStore.user) {
    userStore.fetchUserInfo()
  }
  loadAreas()
})
</script>

<template>
  <div class="home-container">
    <div class="bg-decoration">
      <div class="deco-circle circle-1"></div>
      <div class="deco-circle circle-2"></div>
      <div class="deco-circle circle-3"></div>
    </div>
    
    <div class="header">
      <div class="header-left">
        <div class="logo">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path>
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path>
          </svg>
          <h1>共享自习室</h1>
        </div>
      </div>
      <div class="header-right">
        <div class="balance-card">
          <div class="balance-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <polyline points="12 6 12 12 16 14"></polyline>
            </svg>
          </div>
          <div class="balance-info">
            <span class="balance-label">余额</span>
            <span class="balance-amount">¥{{ userStore.user?.balance?.toFixed(2) || '0.00' }}</span>
          </div>
        </div>
        <div class="nav-buttons">
          <el-button @click="$router.push('/orders')" class="nav-btn" type="text">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
              <line x1="3" x2="21" y1="6" y2="6"></line>
              <path d="M16 10a4 4 0 0 1-8 0"></path>
            </svg>
            <span>订单</span>
          </el-button>
          <el-button @click="$router.push('/profile')" class="nav-btn primary" type="primary">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span>个人中心</span>
          </el-button>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon available">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" x2="12" y1="8" y2="12"></line>
              <line x1="12" x2="12.01" y1="16" y2="16"></line>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ availableCount }}</span>
            <span class="stat-label">可预约座位</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon occupied">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <path d="M12 6v6l4 2"></path>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ occupiedCount }}</span>
            <span class="stat-label">已占用座位</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon maintenance">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
              <path d="M3 3v5h5"></path>
              <path d="M3 16a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16"></path>
              <path d="M16 21h5v-5"></path>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ maintenanceCount }}</span>
            <span class="stat-label">维护中座位</span>
          </div>
        </div>
      </div>

      <div class="area-section">
        <div class="section-header">
          <h2 class="section-title">选择区域</h2>
          <span class="area-count">{{ areas.length }} 个区域</span>
        </div>
        <div class="area-tabs">
          <el-radio-group v-model="selectedAreaId" @change="loadSeats">
            <el-radio-button 
              v-for="area in areas" 
              :key="area.id" 
              :value="area.id"
              class="area-tab"
            >
              {{ area.name }}
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div class="area-info-card" v-if="selectedArea">
        <div class="area-header">
          <h3>{{ selectedArea.name }}</h3>
          <span class="area-status" :class="selectedArea.status?.toLowerCase() || 'disable'">
            {{ selectedArea.status === 'ENABLE' ? '开放中' : '已关闭' }}
          </span>
        </div>
        <p class="area-description">{{ selectedArea.description || '暂无描述' }}</p>
        <div class="area-stats">
          <div class="area-stat">
            <span class="area-stat-value">{{ availableCount }}</span>
            <span class="area-stat-label">可预约</span>
          </div>
          <div class="area-stat">
            <span class="area-stat-value">{{ seats.length }}</span>
            <span class="area-stat-label">总座位</span>
          </div>
        </div>
      </div>

      <div class="seat-section">
        <div class="section-header">
          <h2 class="section-title">座位列表</h2>
        </div>
        <div class="seat-grid" v-loading="loading">
          <div 
            v-for="seat in seats" 
            :key="seat.id"
            class="seat-item"
            :class="[getSeatStatusClass(seat.status), { disabled: selectedArea?.status !== 'ENABLE' }]"
            @click="selectSeat(seat)"
          >
            <div class="seat-status-bar"></div>
            <div class="seat-content">
              <div class="seat-number">{{ seat.seatNumber }}</div>
              <div class="seat-type">{{ seat.type === 0 ? '普通座' : '靠窗座' }}</div>
              <div class="seat-price">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ ((seat.priceFactor || 1) * 10).toFixed(1) }}</span>
                <span class="price-unit">/小时</span>
              </div>
              <div class="seat-attributes" v-if="getSeatAttributeList(seat.attributes).length > 0">
                <span v-for="attr in getSeatAttributeList(seat.attributes)" :key="attr" class="attr-tag">{{ attr }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="empty-state" v-if="!loading && seats.length === 0">
          <div class="empty-icon">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path>
              <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path>
            </svg>
          </div>
          <p class="empty-text">该区域暂无座位</p>
          <el-button type="primary" @click="loadSeats" class="empty-btn">刷新</el-button>
        </div>

        <div class="seat-legend">
          <div class="legend-item">
            <span class="legend-dot available"></span>
            <span>可预订</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot occupied"></span>
            <span>已占用</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot maintenance"></span>
            <span>维护中</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
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
  opacity: 0.6;
}

.deco-circle.circle-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.15) 0%, transparent 70%);
  top: -200px;
  right: -100px;
}

.deco-circle.circle-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(118, 75, 162, 0.1) 0%, transparent 70%);
  bottom: -100px;
  left: -50px;
}

.deco-circle.circle-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(143, 80, 229, 0.08) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
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

.header h1 {
  margin: 0;
  color: #333;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo svg {
  color: #667eea;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 30px;
}

.balance-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #f0f5ff 0%, #e8f0fe 100%);
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(64, 128, 255, 0.15);
}

.balance-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.balance-info {
  display: flex;
  flex-direction: column;
}

.balance-label {
  font-size: 12px;
  color: #888;
}

.balance-amount {
  font-size: 18px;
  font-weight: 700;
  color: #667eea;
}

.nav-buttons {
  display: flex;
  gap: 12px;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.nav-btn:hover {
  background: #f5f7fa;
}

.nav-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.nav-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.main-content {
  padding: 40px 60px;
  max-width: 1500px;
  margin: 0 auto;
  position: relative;
  z-index: 10;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px 30px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
}

.stat-icon.available {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #4caf50;
}

.stat-icon.occupied {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  color: #ff9800;
}

.stat-icon.maintenance {
  background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
  color: #9e9e9e;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #888;
  margin-top: 4px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.area-count {
  font-size: 14px;
  color: #888;
  padding: 6px 16px;
  background: #f5f7fa;
  border-radius: 20px;
}

.area-section {
  margin-bottom: 30px;
}

.area-tabs {
  padding: 24px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.area-tab {
  padding: 12px 28px;
  font-size: 15px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

:deep(.el-radio-button__inner):hover {
  color: #667eea;
}

:deep(.el-radio-button__orig-radio:checked) + .el-radio-button__inner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.area-info-card {
  padding: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  color: white;
  margin-bottom: 30px;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.3);
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.area-header h3 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.area-status {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.area-status.enable {
  background: rgba(255, 255, 255, 0.2);
}

.area-status.disable {
  background: rgba(255, 255, 255, 0.1);
}

.area-description {
  font-size: 15px;
  opacity: 0.9;
  margin: 0 0 20px 0;
  line-height: 1.6;
}

.area-stats {
  display: flex;
  gap: 40px;
}

.area-stat {
  display: flex;
  flex-direction: column;
}

.area-stat-value {
  font-size: 28px;
  font-weight: 700;
}

.area-stat-label {
  font-size: 13px;
  opacity: 0.8;
}

.seat-section {
  margin-top: 20px;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.seat-item {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
}

.seat-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 30px rgba(102, 126, 234, 0.15);
}

.seat-item.available {
  border: 2px solid transparent;
}

.seat-item.available:hover {
  border-color: #67c23a;
}

.seat-item.available .seat-status-bar {
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
}

.seat-item.occupied {
  background: linear-gradient(135deg, #fff5f5 0%, #fff0f0 100%);
  cursor: not-allowed;
  opacity: 0.7;
}

.seat-item.occupied .seat-status-bar {
  background: linear-gradient(90deg, #f56c6c 0%, #f89898 100%);
}

.seat-item.maintenance {
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  cursor: not-allowed;
  opacity: 0.7;
}

.seat-item.maintenance .seat-status-bar {
  background: linear-gradient(90deg, #909399 0%, #b4b4b4 100%);
}

.seat-item.disabled {
  background: linear-gradient(135deg, #f5f5f5 0%, #eeeeee 100%);
  cursor: not-allowed;
  opacity: 0.6;
  pointer-events: none;
}

.seat-item.disabled .seat-status-bar {
  background: linear-gradient(90deg, #dcdcdc 0%, #c0c0c0 100%);
}

.seat-status-bar {
  height: 4px;
}

.seat-content {
  padding: 20px;
  text-align: center;
}

.seat-number {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin-bottom: 6px;
}

.seat-type {
  font-size: 13px;
  color: #888;
  margin-bottom: 12px;
}

.seat-price {
  font-size: 16px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 12px;
}

.price-symbol {
  font-size: 14px;
}

.price-value {
  font-size: 20px;
}

.price-unit {
  font-size: 12px;
  margin-left: 4px;
}

.seat-attributes {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.attr-tag {
  padding: 4px 12px;
  background: #f0f5ff;
  color: #667eea;
  border-radius: 20px;
  font-size: 12px;
}

.empty-state {
  text-align: center;
  padding: 80px 40px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
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

.seat-legend {
  display: flex;
  justify-content: center;
  gap: 50px;
  padding: 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #666;
}

.legend-dot {
  width: 20px;
  height: 20px;
  border-radius: 6px;
}

.legend-dot.available {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.legend-dot.occupied {
  background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);
}

.legend-dot.maintenance {
  background: linear-gradient(135deg, #909399 0%, #b4b4b4 100%);
}

@media (max-width: 768px) {
  .header {
    padding: 16px 20px;
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .header h1 {
    font-size: 22px;
  }
  
  .header-right {
    gap: 16px;
  }
  
  .main-content {
    padding: 20px;
  }
  
  .stats-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .seat-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 16px;
  }
  
  .seat-legend {
    gap: 20px;
    flex-wrap: wrap;
  }
}
</style>