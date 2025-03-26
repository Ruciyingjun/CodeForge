import request from '@/utils/request'

// 检查是否已经实现了这个接口

// 测试AI连接
export function testAIConnection() {
  return request({
    url: '/system/ai/test',  // 确认这个URL是否正确
    method: 'get'
  })
}

// 发送聊天消息
export function sendMessage(data) {
  return request({
    url: '/system/ai/chat/send',
    method: 'post',
    data: data
  })
}

// 获取聊天历史
export function getChatHistory(sessionId) {
  return request({
    url: `/system/ai/chat/history/${sessionId}`,
    method: 'get'
  })
}