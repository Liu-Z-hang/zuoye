<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)

const menuItems = [
  { path: '/dashboard', title: '数据概览', icon: 'DataAnalysis' },
  { path: '/areas', title: '区域管理', icon: 'MapLocation' },
  { path: '/seats', title: '座位管理', icon: 'Grid' },
  { path: '/orders', title: '订单管理', icon: 'Document' },
  { path: '/pricing', title: '计费规则', icon: 'Wallet' },
]

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div v-if="route.path === '/login'" class="login-container">
    <RouterView />
  </div>
  <div v-else class="admin-layout">
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '200px'">
        <div class="logo">
          <h2 v-if="!isCollapse">自习室管理</h2>
          <h2 v-else>管</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item 
            v-for="item in menuItems" 
            :key="item.path"
            :index="item.path"
          >
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <div class="header-left">
            <el-icon class="toggle-icon" @click="isCollapse = !isCollapse">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>
          <div class="header-right">
            <span>管理员</span>
            <el-button type="text" @click="handleLogout">退出登录</el-button>
          </div>
        </el-header>
        <el-main>
          <RouterView />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
}
.admin-layout {
  min-height: 100vh;
}
.el-aside {
  background-color: #304156;
  height: 100vh;
  transition: width 0.3s;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  border-bottom: 1px solid #1f2d3d;
}
.logo h2 {
  margin: 0;
  font-size: 18px;
}
.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-bottom: 1px solid #e6e6e6;
}
.toggle-icon {
  font-size: 20px;
  cursor: pointer;
  color: #666;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}
.el-main {
  background: #f0f2f5;
}
</style>
