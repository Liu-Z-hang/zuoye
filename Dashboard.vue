<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Money, TrendCharts, Tickets, Odometer, Top, User, Calendar } from '@element-plus/icons-vue'
import { orderApi, type Statistics, type SeatRanking, type UserRanking } from '@/api/order'

const statistics = ref<Statistics | null>(null)
const seatRanking = ref<SeatRanking[]>([])
const userRanking = ref<UserRanking[]>([])
const loading = ref(false)

const loadStatistics = async () => {
  try {
    loading.value = true
    const [statsRes, seatRes, userRes] = await Promise.all([
      orderApi.getStatistics(),
      orderApi.getSeatRanking(),
      orderApi.getUserRanking()
    ])
    statistics.value = statsRes.data
    seatRanking.value = seatRes.data.ranking || []
    userRanking.value = userRes.data.ranking || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getRankClass = (index: number) => {
  const classes = ['rank-gold', 'rank-silver', 'rank-bronze']
  return classes[index] || ''
}

const maxSeatUsage = computed(() => {
  if (seatRanking.value.length === 0) return 1
  return Math.max(...seatRanking.value.map(s => s.usageCount))
})

const maxConsumption = computed(() => {
  if (userRanking.value.length === 0) return 1
  return Math.max(...userRanking.value.map(u => u.consumption))
})

const getUsagePercentage = (count: number) => {
  return Math.round((count / maxSeatUsage.value) * 100)
}

const getConsumptionPercentage = (amount: number) => {
  return Math.round((amount / maxConsumption.value) * 100)
}

const getProgressColor = (count: number) => {
  if (count >= 10) return '#67c23a'
  if (count >= 5) return '#e6a23c'
  return '#909399'
}

const getConsumptionColor = (amount: number) => {
  if (amount >= 100) return '#f56c6c'
  if (amount >= 50) return '#e6a23c'
  return '#409eff'
}

onMounted(() => {
  loadStatistics()
})
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <div class="stat-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon revenue">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ statistics?.totalRevenue?.toFixed(2) || '0.00' }}</div>
            <div class="stat-label">总收入</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon today">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ statistics?.todayRevenue?.toFixed(2) || '0.00' }}</div>
            <div class="stat-label">今日收入</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon orders">
            <el-icon><Tickets /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics?.totalOrders || 0 }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon usage">
            <el-icon><Odometer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ ((statistics?.seatUsageRate || 0) * 100).toFixed(1) }}%</div>
            <div class="stat-label">座位使用率</div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="ranking-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card class="ranking-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span><el-icon><Top /></el-icon> 座位使用率排行 TOP 10</span>
              </div>
            </template>
            <el-table :data="seatRanking" style="width: 100%" :show-header="true">
              <el-table-column prop="seatNumber" label="座位号" width="120" align="center">
                <template #default="{ row, $index }">
                  <div class="rank-badge" :class="getRankClass($index)">
                    {{ $index + 1 }}
                  </div>
                  {{ row.seatNumber }}
                </template>
              </el-table-column>
              <el-table-column prop="usageCount" label="使用次数" align="center">
                <template #default="{ row }">
                  <el-progress
                    :percentage="getUsagePercentage(row.usageCount)"
                    :color="getProgressColor(row.usageCount)"
                    :stroke-width="10"
                  />
                  <span class="usage-count">{{ row.usageCount }} 次</span>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
          <el-card class="ranking-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span><el-icon><User /></el-icon> 用户消费排行 TOP 10</span>
              </div>
            </template>
            <el-table :data="userRanking" style="width: 100%" :show-header="true">
              <el-table-column prop="username" label="用户名" width="120" align="center">
                <template #default="{ row, $index }">
                  <div class="rank-badge" :class="getRankClass($index)">
                    {{ $index + 1 }}
                  </div>
                  {{ row.username }}
                </template>
              </el-table-column>
              <el-table-column prop="consumption" label="消费金额" align="center">
                <template #default="{ row }">
                  <el-progress
                    :percentage="getConsumptionPercentage(row.consumption)"
                    :color="getConsumptionColor(row.consumption)"
                    :stroke-width="10"
                  />
                  <span class="consumption-amount">¥{{ row.consumption?.toFixed(2) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="quick-stats">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card class="quick-stat-card" shadow="hover">
            <div class="quick-stat">
              <el-icon class="quick-stat-icon"><Calendar /></el-icon>
              <div class="quick-stat-info">
                <div class="quick-stat-value">{{ statistics?.todayOrders || 0 }}</div>
                <div class="quick-stat-label">今日订单</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px 0;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.stat-icon.revenue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.today {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-icon.orders {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.usage {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
  font-weight: 500;
}

.ranking-section {
  margin-bottom: 20px;
}

.ranking-card {
  border-radius: 12px;
  margin-bottom: 20px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.ranking-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header .el-icon {
  font-size: 22px;
  color: #409eff;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #fff;
  font-weight: bold;
  font-size: 14px;
  margin-right: 10px;
}

.rank-badge.rank-gold {
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
}

.rank-badge.rank-silver {
  background: linear-gradient(135deg, #c9d6ff 0%, #e2e2e2 100%);
  color: #666;
}

.rank-badge.rank-bronze {
  background: linear-gradient(135deg, #d299c2 0%, #fef9d7 100%);
}

.usage-count,
.consumption-amount {
  display: block;
  margin-top: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.quick-stats {
  margin-top: 20px;
}

.quick-stat-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.quick-stat {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 0;
}

.quick-stat-icon {
  font-size: 40px;
  color: #409eff;
}

.quick-stat-info {
  flex: 1;
}

.quick-stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.quick-stat-label {
  font-size: 14px;
  color: #909399;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-table th) {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table td) {
  padding: 16px 0;
}

:deep(.el-progress) {
  margin-bottom: 5px;
}

:deep(.el-progress__text) {
  display: none;
}
</style>
