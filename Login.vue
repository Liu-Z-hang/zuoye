<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  username: '',
  password: '',
})

const loading = ref(false)

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  try {
    loading.value = true
    await userStore.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="bg-container">
      <div class="bg-blur"></div>
      <div class="bg-gradient-1"></div>
      <div class="bg-gradient-2"></div>
      <div class="bg-gradient-3"></div>
      <div class="floating-elements">
        <div class="orb orb-1"></div>
        <div class="orb orb-2"></div>
        <div class="orb orb-3"></div>
        <div class="orb orb-4"></div>
        <div class="grid-lines"></div>
      </div>
    </div>

    <div class="content-wrapper">
      <div class="left-section">
        <div class="logo-section">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="3" width="7" height="7" rx="1"/>
              <rect x="14" y="3" width="7" height="7" rx="1"/>
              <rect x="14" y="14" width="7" height="7" rx="1"/>
              <rect x="3" y="14" width="7" height="7" rx="1"/>
            </svg>
          </div>
          <h1>自习室管理系统</h1>
          <p class="subtitle">智能管理，高效运营</p>
        </div>

        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon-wrap">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z"/>
                <path d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7Z"/>
              </svg>
            </div>
            <h3>实时监控</h3>
            <p>实时追踪座位状态，随时掌握运营情况</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon-wrap">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
              </svg>
            </div>
            <h3>数据统计</h3>
            <p>多维度数据分析，助力科学决策</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon-wrap">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/>
                <line x1="12" x2="12" y1="3" y2="15"/>
              </svg>
            </div>
            <h3>订单管理</h3>
            <p>订单全流程管理，高效处理业务</p>
          </div>
        </div>

        <div class="stats-section">
          <div class="stat-item">
            <span class="stat-number">5000+</span>
            <span class="stat-label">订单</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">99.9%</span>
            <span class="stat-label">系统稳定</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">7x24</span>
            <span class="stat-label">全天候</span>
          </div>
        </div>
      </div>

      <div class="right-section">
        <div class="login-card">
          <div class="card-header">
            <h2>管理员登录</h2>
            <p>欢迎回来，请登录您的账户</p>
          </div>

          <form @submit.prevent="handleLogin" class="login-form">
            <div class="form-group">
              <label>用户名</label>
              <div class="input-wrapper">
                <User class="input-icon" />
                <input 
                  v-model="loginForm.username" 
                  type="text" 
                  placeholder="请输入用户名"
                  autocomplete="username"
                />
              </div>
            </div>
            <div class="form-group">
              <label>密码</label>
              <div class="input-wrapper">
                <Lock class="input-icon" />
                <input 
                  v-model="loginForm.password" 
                  type="password" 
                  placeholder="请输入密码"
                  autocomplete="current-password"
                  @keyup.enter="handleLogin"
                />
              </div>
            </div>
            <button type="submit" class="submit-btn" :disabled="loading">
              <span v-if="loading">登录中...</span>
              <span v-else>登录系统</span>
            </button>
          </form>

          <div class="card-footer">
            <div class="footer-divider">
              <span>或</span>
            </div>
            <a href="http://localhost:5173" target="_blank" class="user-link">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <span>用户端登录</span>
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.bg-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.bg-blur {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #0c1929 0%, #1a2d4a 50%, #0a1628 100%);
}

.bg-gradient-1 {
  position: absolute;
  top: -200px;
  right: -100px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(34, 211, 238, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 8s ease-in-out infinite;
}

.bg-gradient-2 {
  position: absolute;
  bottom: -150px;
  left: -100px;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.25) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 10s ease-in-out infinite reverse;
}

.bg-gradient-3 {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 800px;
  height: 800px;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.15), transparent);
  backdrop-filter: blur(10px);
}

.orb-1 {
  width: 120px;
  height: 120px;
  top: 15%;
  right: 15%;
  animation: float1 15s ease-in-out infinite;
}

.orb-2 {
  width: 80px;
  height: 80px;
  top: 65%;
  left: 20%;
  animation: float2 18s ease-in-out infinite;
}

.orb-3 {
  width: 60px;
  height: 60px;
  top: 35%;
  right: 60%;
  animation: float3 12s ease-in-out infinite;
}

.orb-4 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  right: 30%;
  animation: float4 20s ease-in-out infinite;
}

@keyframes float1 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  25% { transform: translate(-30px, -30px) rotate(90deg); }
  50% { transform: translate(20px, -20px) rotate(180deg); }
  75% { transform: translate(-20px, 30px) rotate(270deg); }
}

@keyframes float2 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  25% { transform: translate(20px, 20px) rotate(-90deg); }
  50% { transform: translate(-30px, 10px) rotate(-180deg); }
  75% { transform: translate(10px, -30px) rotate(-270deg); }
}

@keyframes float3 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  25% { transform: translate(-20px, 20px) rotate(180deg); }
  50% { transform: translate(30px, -10px) rotate(360deg); }
  75% { transform: translate(-10px, -20px) rotate(540deg); }
}

@keyframes float4 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(20px, -30px) scale(1.1); }
  50% { transform: translate(-20px, -10px) scale(0.9); }
  75% { transform: translate(10px, 20px) scale(1.05); }
}

.grid-lines {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
}

.content-wrapper {
  display: flex;
  width: 100%;
  max-width: 1200px;
  gap: 60px;
  position: relative;
  z-index: 10;
}

.left-section {
  flex: 1;
  color: white;
}

.logo-section {
  margin-bottom: 60px;
}

.logo-icon {
  width: 72px;
  height: 72px;
  background: rgba(34, 211, 238, 0.15);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(34, 211, 238, 0.2);
}

.logo-icon svg {
  width: 40px;
  height: 40px;
  color: #22d3ee;
}

.logo-section h1 {
  font-size: 44px;
  font-weight: 700;
  margin: 0 0 12px 0;
  background: linear-gradient(135deg, #fff 0%, rgba(255,255,255,0.8) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.features-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 50px;
}

.feature-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 14px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
}

.feature-card:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateX(5px);
}

.feature-icon-wrap {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, rgba(34, 211, 238, 0.3), rgba(59, 130, 246, 0.3));
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-icon-wrap svg {
  width: 22px;
  height: 22px;
  color: #22d3ee;
}

.feature-card h3 {
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 6px 0;
}

.feature-card p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
  line-height: 1.5;
}

.stats-section {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 24px 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #22d3ee, #3b82f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
}

.right-section {
  flex: 1;
}

.login-card {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.3);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: white;
  margin: 0 0 8px 0;
}

.card-header p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 0 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: rgba(34, 211, 238, 0.5);
  box-shadow: 0 0 20px rgba(34, 211, 238, 0.15);
}

.input-icon {
  width: 20px;
  height: 20px;
  color: rgba(255, 255, 255, 0.4);
  margin-right: 12px;
}

.input-wrapper input {
  flex: 1;
  height: 48px;
  background: transparent !important;
  border: none !important;
  outline: none !important;
  font-size: 15px;
  color: white;
  padding: 0;
  margin: 0;
}

.input-wrapper input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.input-wrapper input:-webkit-autofill,
.input-wrapper input:-webkit-autofill:hover,
.input-wrapper input:-webkit-autofill:focus {
  -webkit-box-shadow: none !important;
  background-color: transparent !important;
  -webkit-text-fill-color: white !important;
  transition: background-color 5000s ease-in-out 0s;
}

.submit-btn {
  height: 52px;
  background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 10px;
  box-shadow: 0 4px 20px rgba(6, 182, 212, 0.4);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(6, 182, 212, 0.5);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.card-footer {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.footer-divider span {
  padding: 0 16px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.footer-divider::before,
.footer-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
}

.user-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 24px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.user-link:hover {
  background: rgba(255, 255, 255, 0.12);
  color: white;
}

.user-link svg {
  width: 16px;
  height: 16px;
}

@media (max-width: 1024px) {
  .content-wrapper {
    flex-direction: column;
    gap: 40px;
  }
  
  .left-section {
    text-align: center;
  }
  
  .feature-card {
    justify-content: center;
    text-align: left;
  }
  
  .stats-section {
    justify-content: center;
  }
}

@media (max-width: 640px) {
  .login-container {
    padding: 16px;
  }
  
  .logo-section h1 {
    font-size: 32px;
  }
  
  .login-card {
    padding: 28px;
  }
  
  .card-header h2 {
    font-size: 24px;
  }
}
</style>