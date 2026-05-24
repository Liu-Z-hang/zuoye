<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, ArrowRight, Setting, Wallet, TurnOff } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { User } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const rechargeAmount = ref<number>(100)
const editDialogVisible = ref(false)
const editForm = ref<User>({
  username: '',
  phone: '',
})

const handleRecharge = async () => {
  if (!rechargeAmount.value || rechargeAmount.value <= 0) {
    ElMessage.warning('请输入有效的金额')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要充值 ¥${rechargeAmount.value.toFixed(2)} 吗？`,
      '充值确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success',
      },
    )
    await userStore.recharge(rechargeAmount.value)
    ElMessage.success('充值成功')
    rechargeAmount.value = 100
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const openEditDialog = () => {
  if (userStore.user) {
    editForm.value = { ...userStore.user }
  }
  editDialogVisible.value = true
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    userStore.logout()
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleSaveProfile = async () => {
  try {
    await userStore.updateProfile(editForm.value)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  if (!userStore.isLoggedIn()) {
    router.push('/login')
    return
  }
  if (!userStore.user) {
    userStore.fetchUserInfo()
  }
})
</script>

<template>
  <div class="profile-container">
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
        <h1>个人中心</h1>
      </div>
      <div class="header-right">
        <el-button type="danger" icon="TurnOff" class="logout-btn" @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </div>

    <div class="profile-content">
      <div class="profile-card">
        <div class="profile-header">
          <div class="avatar">
            {{ userStore.user?.username?.charAt(0).toUpperCase() || 'U' }}
          </div>
          <div class="user-info">
            <h2>{{ userStore.user?.username }}</h2>
            <p class="phone">{{ userStore.user?.phone || '未绑定手机号' }}</p>
          </div>
          <el-button type="primary" icon="Setting" class="edit-btn" @click="openEditDialog">
            编辑资料
          </el-button>
        </div>
        
        <div class="balance-section">
          <div class="balance-card">
            <div class="balance-icon">
              <Wallet />
            </div>
            <div class="balance-info">
              <span class="balance-label">账户余额</span>
              <span class="balance-amount">¥{{ userStore.user?.balance?.toFixed(2) || '0.00' }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="recharge-card">
        <div class="card-header">
          <h3>账户充值</h3>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 11 16 11 14 17 8 17 6 11"></polyline>
            <circle cx="12" cy="12" r="10"></circle>
          </svg>
        </div>
        <div class="recharge-content">
          <div class="quick-amounts">
            <el-button 
              v-for="amount in [50, 100, 200, 500]" 
              :key="amount"
              :class="['amount-btn', { active: rechargeAmount === amount }]"
              @click="rechargeAmount = amount"
            >
              ¥{{ amount }}
            </el-button>
          </div>
          <div class="custom-amount">
            <span>其他金额：</span>
            <div class="amount-input">
              <span class="amount-prefix">¥</span>
              <el-input-number 
                v-model="rechargeAmount" 
                :min="10" 
                :step="10"
                class="number-input"
              />
            </div>
          </div>
          <el-button type="success" size="large" class="recharge-btn" @click="handleRecharge">
            立即充值
          </el-button>
        </div>
      </div>

      <div class="menu-card">
        <div class="menu-item" @click="$router.push('/orders')">
          <div class="menu-icon orders">
            <Document />
          </div>
          <div class="menu-content">
            <span class="menu-title">我的订单</span>
            <span class="menu-desc">查看我的预约记录</span>
          </div>
          <ArrowRight class="menu-arrow" />
        </div>
        <div class="menu-item" @click="openEditDialog">
          <div class="menu-icon settings">
            <Settings />
          </div>
          <div class="menu-content">
            <span class="menu-title">账号设置</span>
            <span class="menu-desc">修改个人信息</span>
          </div>
          <ArrowRight class="menu-arrow" />
        </div>
      </div>
    </div>

    <el-dialog v-model="editDialogVisible" title="编辑资料" width="500px" class="edit-dialog">
      <el-form label-position="top" class="edit-form">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-container {
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

.logout-btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
}

.profile-content {
  max-width: 600px;
  margin: 0 auto;
  padding: 40px 20px;
  position: relative;
  z-index: 10;
}

.profile-card {
  background: white;
  border-radius: 24px;
  padding: 36px;
  margin-bottom: 24px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 30px;
}

.avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 40px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.user-info {
  flex: 1;
}

.user-info h2 {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  color: #333;
}

.user-info .phone {
  margin: 0;
  font-size: 15px;
  color: #888;
}

.edit-btn {
  padding: 12px 24px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.balance-section {
  margin-top: 20px;
}

.balance-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(135deg, #f0f5ff 0%, #e8f0fe 100%);
  border-radius: 16px;
  border: 1px solid #d6e4ff;
}

.balance-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 14px;
  color: white;
  font-size: 24px;
}

.balance-info {
  display: flex;
  flex-direction: column;
}

.balance-label {
  font-size: 14px;
  color: #888;
  margin-bottom: 4px;
}

.balance-amount {
  font-size: 32px;
  font-weight: 700;
  color: #667eea;
}

.recharge-card {
  background: white;
  border-radius: 24px;
  padding: 36px;
  margin-bottom: 24px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.card-header svg {
  color: #667eea;
}

.quick-amounts {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.amount-btn {
  padding: 14px 28px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  background: #f5f7fa;
  color: #666;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.amount-btn:hover {
  background: #f0f5ff;
  border-color: #667eea;
}

.amount-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.custom-amount {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  font-size: 15px;
  color: #666;
}

.amount-input {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 12px;
  padding: 0 16px;
}

.amount-prefix {
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
}

.number-input {
  width: 120px;
}

:deep(.number-input .el-input-number__decrease),
:deep(.number-input .el-input-number__increase) {
  background: transparent;
}

.recharge-btn {
  width: 100%;
  padding: 16px;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(103, 194, 58, 0.3);
}

.recharge-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(103, 194, 58, 0.4);
}

.menu-card {
  background: white;
  border-radius: 24px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 24px 30px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background: #fafbfc;
}

.menu-icon {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  margin-right: 20px;
  font-size: 22px;
}

.menu-icon.orders {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #4caf50;
}

.menu-icon.settings {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  color: #ff9800;
}

.menu-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.menu-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.menu-desc {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
}

.menu-arrow {
  color: #ccc;
  font-size: 20px;
}

.edit-dialog {
  border-radius: 20px;
}

.edit-form {
  padding: 20px 0;
}

@media (max-width: 768px) {
  .header {
    padding: 16px 20px;
  }
  
  .header h1 {
    font-size: 22px;
  }
  
  .profile-content {
    padding: 20px;
  }
  
  .profile-card,
  .recharge-card {
    padding: 24px;
  }
  
  .profile-header {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .avatar {
    width: 80px;
    height: 80px;
    font-size: 32px;
  }
  
  .quick-amounts {
    gap: 10px;
  }
  
  .amount-btn {
    padding: 12px 20px;
    font-size: 14px;
  }
}
</style>