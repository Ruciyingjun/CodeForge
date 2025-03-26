<template>
  <div class="chat-container">
    <div class="chat-header">
      <h2><i class="el-icon-chat-dot-round"></i> AI 助手</h2>
    </div>
    
    <div class="chat-messages-wrapper">
      <div class="chat-messages" ref="messageContainer">
        <div v-for="(msg, index) in displayMessages" :key="index"
             :class="['message', msg.role === 'user' ? 'user-message' : 'ai-message']">
          <div class="avatar">
            <i v-if="msg.role === 'user'" class="el-icon-user"></i>
            <img v-else src="@/assets/images/pool-logo.png" alt="AI助手" class="ai-avatar">
          </div>
          <div class="message-content" v-if="msg.role === 'user'">{{ msg.content }}</div>
          <div class="message-content" v-else>
            <div v-if="msg.isTyping" class="typing">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <div v-else v-html="msg.displayContent || msg.content"></div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        :placeholder="sending ? '发送中...' : '请输入消息...'"
        @keyup.enter.native="!sending && sendMessage()"
        :disabled="sending"
        resize="none"
        class="chat-textarea"
      />
      <el-button 
        type="primary" 
        @click="sendMessage" 
        :loading="sending" 
        :disabled="!inputMessage.trim()"
        icon="el-icon-s-promotion">
        {{ sending ? '发送中...' : '发送' }}
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.chat-container {
  height: calc(100vh - 120px); /* 适应页面高度 */
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  margin: 20px;
}

.chat-messages-wrapper {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: #f9fafb;
}

.chat-messages {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow-y: auto;
  padding: 20px;
}

/* 添加滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f5f7fa;
}

.chat-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.chat-header h2 {
  margin: 0;
  color: #409EFF;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f9fafb;
}

.message {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
  max-width: 85%;
  gap: 8px;
}

.avatar {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
  border: 2px solid #ffffff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.ai-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  background-color: #ffffff;
}

.user-message .avatar {
  background: #409EFF;
  color: white;
  border-color: #66b1ff;
}

.ai-message .avatar {
  background: #ffffff;
  border-color: #e4e7ed;
}

.user-message {
  margin-left: auto;
  flex-direction: row-reverse;
}

.user-message .avatar {
  background: #409EFF;
  color: white;
}

.ai-message .avatar {
  background: #95a5a6;
  color: white;
}

.message-content {
  padding: 12px 16px;
  border-radius: 12px;
  position: relative;
  word-break: break-word;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.user-message .message-content {
  background: #409EFF;
  color: white;
  border-top-right-radius: 2px;
}

.ai-message .message-content {
  background: white;
  color: #333;
  border-top-left-radius: 2px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.typing {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 60px;
}

.typing span {
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #95a5a6;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.chat-input {
  padding: 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 12px;
}

.chat-input .el-textarea__inner {
  border-radius: 8px;
  resize: none;
}

.chat-input .el-button {
  align-self: flex-end;
  height: 40px;
  border-radius: 8px;
  padding: 0 24px;
}
</style>

<script>
import { sendMessage, getChatHistory } from '@/api/system/ai'
import { v4 as uuidv4 } from 'uuid'

export default {
  name: 'AIChat',
  data() {
    return {
      sessionId: '',
      messages: [],
      inputMessage: '',
      sending: false,
      systemRole: {
        role: 'system',
        content: '你是一个CodeForge平台助手，CodeForge是一个集成了 AI 能力的代码辅助生成平台，一个优化原生代码生成器生成模糊问题且具备二次开发能力的 RuoYi 框架，并且集成了一些好用的开发工具。'
      }
    }
  },
  computed: {  // 修正：computed 的正确语法
    displayMessages() {
      return this.messages.filter(msg => msg.role !== 'system');
    }
  },
  created() {
    this.sessionId = uuidv4()
    this.messages.push(this.systemRole)
    this.loadHistory()
  },
  methods: {
    async typeWriter(message, fullContent) {
      let currentLength = 0;
      const contentLength = fullContent.length;
      
      return new Promise((resolve) => {
        const timer = setInterval(() => {
          currentLength++;
          this.$set(message, 'displayContent', fullContent.slice(0, currentLength));
          
          if (currentLength >= contentLength) {
            clearInterval(timer);
            resolve();
          }
        }, this.typingSpeed);
      });
    },

    async sendMessage() {
      if (!this.inputMessage.trim() || this.sending) return

      const messageToSend = this.inputMessage
      this.inputMessage = ''
      
      const userMessage = {
        role: 'user',
        content: messageToSend
      }
      this.messages.push(userMessage)

      // 添加一个带有打字动画的临时AI消息
      const aiMessage = {
        role: 'assistant',
        content: '',
        displayContent: '',
        isTyping: true
      }
      this.messages.push(aiMessage)
      this.$nextTick(() => {
        this.scrollToBottom()
      })

      this.sending = true
      try {
        const response = await sendMessage({
          sessionId: this.sessionId,
          content: messageToSend,
          systemRole: this.systemRole.content
        })
      
        if (response.code === 200) {
          // 更新现有的AI消息而不是创建新的
          aiMessage.isTyping = false
          await this.typeWriter(aiMessage, response.data.content)
          this.$nextTick(() => {
            this.scrollToBottom()
          })
        } else {
          this.$message.error(response.msg || '发送失败')
          // 移除临时消息
          this.messages.pop()
        }
      } catch (error) {
        this.$message.error('发送失败：' + error.message)
        // 移除临时消息
        this.messages.pop()
      } finally {
        this.sending = false
      }
    }
  }
}
</script>

.ai-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.ai-message .avatar {
  background: transparent;  /* 移除原来的背景色 */
}
