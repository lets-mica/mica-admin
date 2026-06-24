<script setup lang="ts">
import { onLaunch, onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { useImStore } from '@/modules/im/stores/im'

const auth = useAuthStore()
const im = useImStore()

onLaunch(() => {
  console.log('[App] launched')
  // 1. 恢复登录态
  auth.bootstrap()
  // 2. 已登录 → 建立 MQTT 长连接
  if (auth.isLoggedIn) {
    im.connectMqtt()
  }
})

onShow(() => {
  console.log('[App] show')
})
</script>

<style lang="scss">
/* uni-ui 样式 */
@import '@dcloudio/uni-ui/lib/uni-icons/uniicons.css';

/* 全局样式 */
page {
  background-color: #f5f6f8;
  color: #1f2329;
  font-size: 28rpx;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica,
    'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.flex-row {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.flex-col {
  display: flex;
  flex-direction: column;
}

.flex-1 {
  flex: 1;
}

.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-muted {
  color: #8f959e;
}

.text-danger {
  color: #f53f3f;
}

.text-primary {
  color: #18a37e;
}
</style>