<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon :size="24"><Sunny /></el-icon>
        <span>碳排管理系统</span>
      </div>

      <!-- 用户信息卡片 -->
      <div class="user-card">
        <el-avatar :size="44" class="user-avatar">
          {{ userStore.username?.charAt(0)?.toUpperCase() }}
        </el-avatar>
        <div class="user-name">{{ userStore.username }}</div>
        <el-tag
            :type="userStore.isAdmin ? 'danger' : 'success'"
            effect="dark"
            size="small"
            class="user-role"
        >
          {{ userStore.isAdmin ? '管理员' : '企业用户' }}
        </el-tag>
        <div class="user-factory">
          <el-icon><OfficeBuilding /></el-icon>
          <span class="factory-text">
            {{ userStore.factoryName || '未填写工厂' }}
          </span>
        </div>
      </div>

      <!-- 导航菜单（动态、支持多级、按角色过滤） -->
      <el-menu
          :default-active="activeMenu"
          :default-openeds="defaultOpeneds"
          router
          background-color="#001529"
          text-color="#b7bdc3"
          active-text-color="#fff"
          class="side-menu"
          unique-opened
      >
        <template v-for="item in visibleMenus" :key="item.path">
          <!-- 有子菜单 -->
          <el-sub-menu v-if="item.children?.length" :index="item.path">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item
                v-for="child in item.children"
                :key="child.path"
                :index="child.path"
            >
              <el-icon v-if="child.icon"><component :is="child.icon" /></el-icon>
              <span>{{ child.title }}</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 单级菜单 -->
          <el-menu-item v-else :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>


    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">
              {{ $route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              <span>{{ userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 显示嵌套子级页面：Dashboard/Factor等页面渲染在这里-->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userstore.js'


const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前激活项：优先取 meta.activeMenu（详情页高亮列表项），否则取 route.path
const activeMenu = computed(() => route.meta.activeMenu || route.path)

// 根据角色过滤菜单（含子菜单）
const visibleMenus = computed(() => {
  // 找到 path 为 '/' 的那条路由（即 MainLayout）
  const layoutRoute = router.options.routes.find(r => r.path === '/')
  const children = layoutRoute?.children || []
  return children
      // 1. 排除 meta.hidden 的项（详情页、编辑页等不显示的）
      .filter(r => !r.meta?.hidden)
      // 2. 按角色过滤
      .filter(r => {
        const roles = r.meta?.roles
        return !roles || roles.includes(userStore.role)
      })
      // 3. 转成菜单需要的字段，path 拼成绝对路径
      .map(r => ({
        path: '/' + r.path.replace(/^\//, ''),   // 'dashboard' -> '/dashboard'
        title: r.meta?.title || r.name || '',
        icon: r.meta?.icon,
        // 如果以后有嵌套 children，这里递归处理；当前没有就留 undefined
        children: r.children?.length
            ? r.children
                .filter(c => !c.meta?.hidden)
                .filter(c => {
                  const roles = c.meta?.roles
                  return !roles || roles.includes(userStore.role)
                })
                .map(c => ({
                  path: '/' + r.path.replace(/^\//, '') + '/' + c.path.replace(/^\//, ''),
                  title: c.meta?.title || c.name || '',
                  icon: c.meta?.icon
                }))
            : undefined
      }))
})

// 默认展开所有含子菜单的父项
const defaultOpeneds = computed(() =>
    visibleMenus.value.filter(i => i.children?.length)   // 只留有子菜单的父项
        .map(i => i.path)   // 取出它们的 path
)

// 下拉菜单命令
async function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出登录吗？', '提示', {
        type: 'warning'
      })
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch (e) {
      /* 取消 */
    }
  }
}
</script>

<style scoped>
.layout { height: 100vh; }

.aside {
  background: #001529;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* 用户信息卡片 */
.user-card {
  padding: 20px 16px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}

.user-avatar {
  background: linear-gradient(135deg, #1e3c72, #4caf50);
  color: #fff;
  font-weight: bold;
  font-size: 18px;
}

.user-name {
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  margin-top: 10px;
}

.user-role {
  margin-top: 6px;
}

.user-factory {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #b7bdc3;
  font-size: 12px;
}

.factory-text {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 菜单占满剩余空间 */
.side-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
}

.side-menu:not(.el-menu--collapse) {
  width: 220px;
}

/* 菜单 hover / active 效果优化 */
.side-menu :deep(.el-menu-item:hover),
.side-menu :deep(.el-sub-menu__title:hover) {
  background-color: #0b2a45 !important;
}

.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #1890ff33, transparent) !important;
  border-left: 3px solid #1890ff;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #333;
  font-size: 14px;
  outline: none;
}

.main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>