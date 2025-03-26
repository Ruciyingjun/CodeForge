<template>
  <div class="chat-container">
    <div class="chat-messages" ref="messageContainer">
      <div v-for="(msg, index) in messages" :key="index" 
           :class="['message', msg.role === 'user' ? 'user-message' : 'ai-message']">
        <div class="message-content">{{ msg.content }}</div>
      </div>
    </div>
    
    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        :disabled="sending"
        placeholder="请输入消息..."
        @keyup.enter.native="sendMessage"
      />
      <el-button type="primary" @click="sendMessage" :loading="sending">
        {{ sending ? '发送中...' : '发送' }}
      </el-button>
    </div>
  </div>
</template>

<script>
import { sendMessage } from '@/api/system/ai'
import { v4 as uuidv4 } from 'uuid'

export default {
  name: 'AIChat',
  data() {
    return {
      sessionId: '',
      messages: [],
      inputMessage: '',
      sending: false
    }
  },
  created() {
    this.sessionId = uuidv4()
  },
  methods: {
    async sendMessage() {
      if (!this.inputMessage.trim()) return
      
      this.sending = true
      try {
        // 先添加用户消息
        const userMessage = {
          role: 'user',
          content: this.inputMessage
        }
        this.messages.push(userMessage)
        
        const response = await sendMessage({
          sessionId: this.sessionId,
          content: this.inputMessage
        })
        
        if (response.code === 200) {
          // 添加AI响应
          this.messages.push(response.data)
          this.inputMessage = ''
          this.$nextTick(() => {
            this.scrollToBottom()
          })
        } else {
          this.$message.error(response.msg || '发送失败')
        }
      } catch (error) {
        this.$message.error('网络错误：' + error.message)
        console.error('发送消息失败：', error)
      } finally {
        this.sending = false
      }
    },
    scrollToBottom() {
      const container = this.$refs.messageContainer
      container.scrollTop = container.scrollHeight
    }
  }
}
</script>

<style scoped>
.chat-container {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.message {
  margin-bottom: 15px;
  max-width: 80%;
}

.user-message {
  margin-left: auto;
  text-align: right;
}

.ai-message {
  margin-right: auto;
  text-align: left;
}

.message-content {
  display: inline-block;
  padding: 10px 15px;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.user-message .message-content {
  background: #409EFF;
  color: white;
}

.chat-input {
  display: flex;
  gap: 10px;
  margin-top: auto;
}
</style>