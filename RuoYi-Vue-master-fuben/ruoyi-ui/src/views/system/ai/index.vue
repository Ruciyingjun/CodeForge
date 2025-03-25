<template>
  <div class="app-container">
    <div class="test-panel">
      <div class="test-display">
        <div class="status-circle" :class="{ success: result === 'success', error: result === 'error' }">
          <i class="el-icon-connection"></i>
          <svg class="status-ring" viewBox="0 0 100 100">
            <circle cx="50" cy="50" r="45" />
          </svg>
        </div>
        <div class="status-text">
          <span class="main-status">{{ getStatusText() }}</span>
          <span class="sub-status">{{ getConnectionStatus() }}</span>
          <span class="time-status">
            {{ lastTestTime || '等待测试中' }}
            <span class="jumping-dots" v-if="!lastTestTime">
              <span>.</span><span>.</span><span>.</span>
            </span>
            <el-tooltip v-if="errorMessage" effect="dark" placement="top">
              <div slot="content">{{ errorMessage }}</div>
              <i class="el-icon-warning-outline"></i>
            </el-tooltip>
          </span>
        </div>
      </div>

      <div class="test-logs">
        <div class="logs-header">
          <span>测试日志</span>
          <el-button type="text" @click="clearLogs" size="small">
            <i class="el-icon-delete"></i> 清除日志
          </el-button>
        </div>
        <div class="logs-content" ref="logsContent">
          <div v-for="(log, index) in logs" :key="index" 
               class="log-item" :class="log.type">
            <span class="log-time">{{ log.time }}</span>
            <span class="log-type">{{ log.type.toUpperCase() }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
          <div v-if="!logs.length" class="empty-logs">
            暂无测试日志
          </div>
        </div>
      </div>

      <div class="test-control">
        <el-button
          type="primary"
          :loading="loading"
          @click="testConnection"
          class="test-btn">
          <i class="el-icon-refresh"></i>
          开始测试
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { testAIConnection } from '@/api/system/ai'

export default {
  name: 'AITest',
  data() {
    return {
      loading: false,
      result: '',
      lastTestTime: '',
      errorMessage: '',
      logs: []
    }
  },
  methods: {
    addLog(type, message) {
      const time = new Date().toLocaleTimeString();
      this.logs.push({ type, message, time });
      this.$nextTick(() => {
        if (this.$refs.logsContent) {
          this.$refs.logsContent.scrollTop = this.$refs.logsContent.scrollHeight;
        }
      });
    },
    clearLogs() {
      this.logs = [];
    },
    getStatusText() {
      if (!this.result) return 'API 状态未知';
      return this.result === 'success' ? 'API 连接正常' : 'API 连接异常';
    },
    getConnectionStatus() {
      if (!this.result) return '请点击按钮开始测试连接';
      if (this.result === 'error') return this.errorMessage || '服务连接失败';
      return '所有服务运行正常';
    },
    async testConnection() {
      this.loading = true;
      this.errorMessage = '';
      this.lastTestTime = '';
      this.addLog('info', '开始测试 API 连接...');
      
      try {
        const response = await testAIConnection();
        this.addLog('info', `收到服务器响应: ${JSON.stringify(response)}`);
        
        if (response.code === 200) {
          this.result = 'success';
          this.lastTestTime = new Date().toLocaleString();
          this.addLog('success', 'API 连接测试成功！');
        } else {
          this.result = 'error';
          this.errorMessage = response.msg || '服务响应异常';
          this.addLog('error', `测试失败: ${this.errorMessage}`);
          this.lastTestTime = new Date().toLocaleString();
        }
      } catch (error) {
        this.result = 'error';
        this.errorMessage = error.message || '网络请求失败';
        this.addLog('error', `请求异常: ${this.errorMessage}`);
        this.$message.error(this.errorMessage);
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.test-panel {
  max-width: 600px;
  margin: 40px auto;
  padding: 40px;
  background: rgba(28, 28, 28, 0.6);
  border-radius: 16px;
  border: 1px solid #2c2c2c;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba(64, 158, 255, 0.2);
  }
}

.test-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;
}

.status-circle {
  position: relative;
  width: 120px;
  height: 120px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: center;

  i {
    font-size: 40px;
    color: #8E8EA0;
    z-index: 1;
    transition: all 0.3s ease;
  }

  .status-ring {
    position: absolute;
    width: 100%;
    height: 100%;
    transform: rotate(-90deg);

    circle {
      fill: none;
      stroke: #2c2c2c;
      stroke-width: 4;
      stroke-dasharray: 283;
      stroke-dashoffset: 283;
      transition: all 0.6s ease;
    }
  }

  &.success {
    i {
      color: #67C23A;
    }
    .status-ring circle {
      stroke: #67C23A;
      stroke-dashoffset: 0;
    }
  }

  &.error {
    i {
      color: #F56C6C;
    }
    .status-ring circle {
      stroke: #F56C6C;
      stroke-dashoffset: 0;
    }
  }
}

.status-text {
  text-align: center;

  .main-status {
    display: block;
    font-size: 28px;
    font-weight: 500;
    background: linear-gradient(45deg, #409EFF, #66b1ff);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    margin-bottom: 12px;
    letter-spacing: 1px;
  }

  .sub-status {
    display: block;
    font-size: 15px;
    color: rgba(255, 255, 255, 0.8);
    margin-bottom: 8px;
    font-weight: 400;
    letter-spacing: 0.5px;
  }

  .time-status {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: rgba(255, 255, 255, 0.6);
    font-size: 14px;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    padding: 6px 12px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 15px;
    backdrop-filter: blur(5px);
    
    .jumping-dots {
      margin-left: 2px;
      
      span {
        color: #409EFF;
        font-weight: bold;
        text-shadow: 0 0 8px rgba(64, 158, 255, 0.4);
      }
    }
    
    i {
      margin-left: 4px;
      color: #F56C6C;
      filter: drop-shadow(0 0 2px rgba(245, 108, 108, 0.4));
    }
  }
}

.test-logs {
  margin: 20px 0 30px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  border: 1px solid #2c2c2c;

  .logs-header {
    padding: 12px 16px;
    border-bottom: 1px solid #2c2c2c;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    span {
      color: #E0E0E0;
      font-size: 14px;
      font-weight: 500;
    }
  }

  .logs-content {
    height: 200px;
    overflow-y: auto;
    padding: 12px;

    .log-item {
      padding: 8px;
      font-size: 12px;
      font-family: monospace;
      display: flex;
      gap: 8px;
      border-radius: 4px;
      margin-bottom: 4px;

      &:hover {
        background: rgba(255, 255, 255, 0.05);
      }

      .log-time {
        color: #8E8EA0;
        min-width: 80px;
      }

      .log-type {
        min-width: 60px;
        font-weight: 500;
      }

      .log-message {
        color: #E0E0E0;
        word-break: break-all;
      }

      &.info {
        .log-type { color: #409EFF; }
      }

      &.success {
        .log-type { color: #67C23A; }
      }

      &.error {
        .log-type { color: #F56C6C; }
      }
    }

    .empty-logs {
      text-align: center;
      color: #666;
      padding: 20px;
      font-style: italic;
    }
  }
}

.test-control {
  text-align: center;

  .test-btn {
    min-width: 140px;
    height: 40px;
    border-radius: 20px;
    font-size: 14px;
    background: linear-gradient(45deg, #409EFF, #66b1ff);
    border: none;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    }

    &:active {
      transform: translateY(1px);
    }

    i {
      margin-right: 6px;
    }
  }
}

.logs-content {
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: #2c2c2c;
    border-radius: 3px;
    
    &:hover {
      background: #409EFF;
    }
  }
}

.jumping-dots {
  display: inline-flex;
  
  span {
    animation: jumping 1.4s infinite;
    display: inline-block;
    transform-origin: bottom;
    
    &:nth-child(1) {
      animation-delay: 0s;
    }
    &:nth-child(2) {
      animation-delay: 0.2s;
    }
    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

@keyframes jumping {
  0% {
    transform: translateY(0) scale(1);
  }
  25% {
    transform: translateY(-4px) scale(1.1);
  }
  50% {
    transform: translateY(0) scale(1);
  }
  100% {
    transform: translateY(0) scale(1);
  }
}
</style>
