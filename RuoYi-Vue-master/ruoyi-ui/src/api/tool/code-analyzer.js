import request from '@/utils/request'

// 获取支持的编程语言列表
export function getLanguages() {
  return request({
    url: '/system/code-analyzer/supported-languages',
    method: 'get'
  })
}

// 分析代码
export function analyzeCode(data) {
  return request({
    url: '/system/code-analyzer/analyze',
    method: 'post',
    data: data,
    timeout: 30000
  })
} 