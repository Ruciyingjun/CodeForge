<template>
  <div class="code-analyzer">
    <el-card class="box-card">
      <div slot="header" class="header">
        <span class="title">
          <i class="el-icon-cpu"></i>
          代码质量分析
        </span>
        <el-button type="text" icon="el-icon-question" @click="showHelp">
          使用说明
        </el-button>
      </div>
      
      <!-- 代码输入区域 -->
      <el-form :model="form" label-width="100px">
        <el-form-item label="编程语言">
          <el-select v-model="form.language" placeholder="请选择语言">
            <el-option v-for="lang in languages" 
              :key="lang.value" 
              :label="lang.label" 
              :value="lang.value">
              <span class="language-option">
                <i :class="lang.icon"></i>
                {{ lang.label }}
                <span class="language-desc">{{ lang.desc }}</span>
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="代码输入">
          <div class="code-input-area">
            <el-tabs v-model="activeTab" type="card">
              <el-tab-pane label="直接输入" name="input">
                <div class="code-editor-toolbar">
                  <el-button 
                    type="text" 
                    icon="el-icon-delete"
                    @click="clearCode">
                    清空
                  </el-button>
                  <el-button 
                    type="text" 
                    icon="el-icon-document-copy"
                    v-clipboard:copy="form.code"
                    v-clipboard:success="onCopy"
                    v-clipboard:error="onCopyError">
                    复制
                  </el-button>
                </div>
                <el-input
                  type="textarea"
                  :rows="12"
                  v-model="form.code"
                  placeholder="请输入或粘贴代码..."
                  :spellcheck="false"
                  class="code-editor">
                </el-input>
              </el-tab-pane>
              <el-tab-pane label="文件上传" name="upload">
                <el-upload
                  class="upload-area"
                  drag
                  action="#"
                  :auto-upload="false"
                  :on-change="handleFileChange"
                  :before-upload="beforeUpload"
                  :show-file-list="false">
                  <i class="el-icon-upload"></i>
                  <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                  <div class="el-upload__tip">
                    支持的文件类型：
                    <el-tag size="mini" v-for="ext in fileExtensions" :key="ext">
                      {{ ext }}
                    </el-tag>
                  </div>
                </el-upload>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            :loading="analyzing" 
            @click="analyzeCode"
            :disabled="!form.code.trim() || analyzing"
            icon="el-icon-s-operation">
            {{ analyzing ? '分析中...' : '开始分析' }}
          </el-button>
          <el-button 
            type="info" 
            icon="el-icon-refresh"
            @click="resetForm"
            :disabled="analyzing">
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 分析结果展示 -->
      <transition name="fade">
        <div v-if="report" class="analysis-report">
          <div class="report-header">
            <h3>
              <i class="el-icon-document"></i>
              代码质量报告
            </h3>
            <div class="report-actions">
              <el-button 
                type="text" 
                icon="el-icon-download"
                @click="downloadReport">
                导出报告
              </el-button>
              <el-button 
                type="text" 
                icon="el-icon-close"
                @click="closeReport">
                关闭
              </el-button>
            </div>
          </div>

          <!-- 总体评分 -->
          <div class="score-section">
            <el-progress type="circle" 
              :percentage="report.score"
              :color="scoreColor"
              :stroke-width="10">
              <div class="progress-content">
                {{ report.score }}
                <span class="progress-label">分</span>
              </div>
            </el-progress>
            <div class="score-desc">
              <h4>代码质量评分</h4>
              <p>{{ getScoreDesc(report.score) }}</p>
              <div class="score-tags">
                <el-tag 
                  v-for="(metric, key) in report.metrics"
                  :key="key"
                  :type="getTagType(metric.score)"
                  size="small"
                  effect="plain">
                  {{ getMetricName(key) }}: {{ metric.score }}分
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 详细指标 -->
          <el-collapse v-model="activeMetrics" accordion>
            <!-- 代码规范 -->
            <el-collapse-item name="style">
              <template slot="title">
                <i class="el-icon-s-check"></i>
                代码规范 
                <el-tag :type="getTagType(report.metrics.style.score)" size="small">
                  {{ report.metrics.style.score }}分
                </el-tag>
              </template>
              <div class="metric-details">
                <el-alert
                  v-for="(issue, index) in report.metrics.style.issues"
                  :key="index"
                  :title="issue.message"
                  :type="issue.severity"
                  :description="issue.suggestion"
                  show-icon>
                </el-alert>
              </div>
            </el-collapse-item>

            <!-- 复杂度分析 -->
            <el-collapse-item name="complexity">
              <template slot="title">
                <i class="el-icon-s-data"></i>
                复杂度分析
                <el-tag :type="getTagType(report.metrics.complexity.score)" size="small">
                  {{ report.metrics.complexity.score }}分
                </el-tag>
              </template>
              <div class="metric-details">
                <el-descriptions border>
                  <el-descriptions-item label="圈复杂度">
                    {{ report.metrics.complexity.details.cyclomaticComplexity }}
                  </el-descriptions-item>
                  <el-descriptions-item label="最大嵌套深度">
                    {{ report.metrics.complexity.details.maxNestingDepth }}
                  </el-descriptions-item>
                </el-descriptions>
                <div class="suggestions">
                  <p v-for="(tip, index) in report.metrics.complexity.suggestions"
                     :key="index" class="suggestion-item">
                    <i class="el-icon-info"></i>
                    {{ tip }}
                  </p>
                </div>
              </div>
            </el-collapse-item>

            <!-- 命名规范 -->
            <el-collapse-item name="naming">
              <template slot="title">
                <i class="el-icon-edit"></i>
                命名规范
                <el-tag :type="getTagType(report.metrics.naming.score)" size="small">
                  {{ report.metrics.naming.score }}分
                </el-tag>
              </template>
              <div class="metric-details">
                <el-alert
                  v-for="(issue, index) in report.metrics.naming.issues"
                  :key="index"
                  :title="issue.message"
                  :type="issue.severity"
                  :description="issue.suggestion"
                  show-icon>
                </el-alert>
              </div>
            </el-collapse-item>

            <!-- 语言特性 -->
            <el-collapse-item v-if="form.language === 'javascript'" name="es6">
              <template slot="title">
                <i class="el-icon-star-on"></i>
                ES6+特性
                <el-tag :type="getTagType(report.metrics.es6.score)" size="small">
                  {{ report.metrics.es6.score }}分
                </el-tag>
              </template>
              <div class="metric-details">
                <div class="suggestions">
                  <p v-for="(tip, index) in report.metrics.es6.suggestions"
                     :key="index" class="suggestion-item">
                    <i class="el-icon-info"></i>
                    {{ tip }}
                  </p>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </transition>
    </el-card>

    <!-- 帮助对话框 -->
    <el-dialog
      title="代码质量分析使用说明"
      :visible.sync="helpDialogVisible"
      width="600px">
      <div class="help-content">
        <h4>功能介绍</h4>
        <p>代码质量分析工具可以帮助您：</p>
        <ul>
          <li>检查代码规范性</li>
          <li>分析代码复杂度</li>
          <li>评估命名规范</li>
          <li>提供改进建议</li>
        </ul>

        <h4>使用步骤</h4>
        <ol>
          <li>选择代码的编程语言</li>
          <li>输入或上传代码文件</li>
          <li>点击"开始分析"按钮</li>
          <li>查看分析报告和建议</li>
        </ol>

        <h4>支持的语言</h4>
        <ul>
          <li>Java - Java语言代码分析</li>
          <li>Python - Python代码分析</li>
          <li>JavaScript - JavaScript/ES6+代码分析</li>
        </ul>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'CodeAnalyzer',
  data() {
    return {
      form: {
        language: 'java',
        code: ''
      },
      languages: [
        { 
          label: 'Java',
          value: 'java',
          icon: 'el-icon-s-platform',
          desc: '分析Java语言代码质量'
        },
        { 
          label: 'Python',
          value: 'python',
          icon: 'el-icon-s-data',
          desc: '分析Python代码规范性'
        },
        { 
          label: 'JavaScript',
          value: 'javascript',
          icon: 'el-icon-s-promotion',
          desc: '分析JS/ES6+代码质量'
        }
      ],
      activeTab: 'input',
      analyzing: false,
      report: null,
      activeMetrics: ['style', 'complexity', 'naming', 'es6'],
      helpDialogVisible: false,
      fileExtensions: ['.java', '.py', '.js', '.jsx', '.ts', '.vue']
    }
  },
  computed: {
    scoreColor() {
      return (percentage) => {
        if (percentage > 80) return '#67C23A'
        if (percentage > 60) return '#E6A23C'
        return '#F56C6C'
      }
    }
  },
  methods: {
    getLanguages() {
      request({
        url: '/tool/codeanalyzer/supported-languages',
        method: 'get'
      }).then(response => {
        this.languages = response.data
      })
    },
    handleFileChange(file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        this.form.code = e.target.result
      }
      reader.readAsText(file.raw)
    },
    analyzeCode() {
      if (!this.form.code.trim()) {
        this.$message.warning('请输入或上传代码')
        return
      }

      this.analyzing = true
      request({
        url: '/tool/codeanalyzer/analyze',
        method: 'post',
        data: this.form
      }).then(response => {
        if (response.code === 200) {
          this.report = response.data
          this.$message.success('代码分析完成')
        } else {
          this.$message.error(response.msg || '分析失败')
        }
      }).catch(error => {
        this.$message.error('分析过程出错：' + error.message)
      }).finally(() => {
        this.analyzing = false
      })
    },
    getTagType(score) {
      if (score >= 80) return 'success'
      if (score >= 60) return 'warning'
      return 'danger'
    },
    getScoreDesc(score) {
      if (score >= 80) return '代码质量良好，符合规范要求'
      if (score >= 60) return '代码质量一般，建议优化'
      return '代码质量需要改进，请参考建议进行修改'
    },
    downloadReport() {
      // 实现报告导出逻辑
      const reportText = this.generateReportText()
      const blob = new Blob([reportText], { type: 'text/plain;charset=utf-8' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = '代码质量分析报告.txt'
      link.click()
    },
    generateReportText() {
      // 生成报告文本
      let text = '代码质量分析报告\n'
      text += '================\n\n'
      text += `总体评分：${this.report.score}分\n\n`
      
      // 添加各维度详情
      Object.entries(this.report.metrics).forEach(([key, metric]) => {
        text += `${this.getMetricName(key)}（${metric.score}分）：\n`
        if (metric.issues) {
          metric.issues.forEach(issue => {
            text += `- ${issue.message}\n  建议：${issue.suggestion}\n`
          })
        }
        if (metric.suggestions) {
          metric.suggestions.forEach(suggestion => {
            text += `- ${suggestion}\n`
          })
        }
        text += '\n'
      })
      
      return text
    },
    getMetricName(key) {
      const names = {
        style: '代码规范',
        complexity: '复杂度分析',
        naming: '命名规范',
        es6: 'ES6+特性'
      }
      return names[key] || key
    },
    showHelp() {
      this.helpDialogVisible = true
    },
    clearCode() {
      this.form.code = ''
    },
    resetForm() {
      this.form.language = 'java'
      this.form.code = ''
      this.report = null
      this.activeTab = 'input'
      this.analyzing = false
    },
    closeReport() {
      this.report = null
    },
    beforeUpload(file) {
      const extension = file.name.split('.').pop().toLowerCase()
      const validExtensions = this.fileExtensions
      if (!validExtensions.includes(`.${extension}`)) {
        this.$message.error(`只支持 ${validExtensions.join(', ')} 格式的文件`)
        return false
      }
      return true
    },
    onCopy() {
      this.$message.success('代码已复制到剪贴板')
    },
    onCopyError() {
      this.$message.error('复制失败')
    }
  }
}
</script>

<style scoped>
.code-analyzer {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.title i {
  margin-right: 8px;
  color: #409EFF;
}

.code-input-area {
  border: 1px solid #DCDFE6;
  border-radius: 4px;
}

.upload-area {
  padding: 20px;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.language-option {
  display: flex;
  align-items: center;
}

.language-option i {
  margin-right: 8px;
  font-size: 16px;
}

.code-editor-toolbar {
  padding: 8px;
  border-bottom: 1px solid #EBEEF5;
  display: flex;
  justify-content: flex-end;
}

.code-editor {
  font-family: 'Consolas', 'Monaco', monospace;
}

.language-desc {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.analysis-report {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #EBEEF5;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-actions {
  display: flex;
  align-items: center;
}

.score-section {
  display: flex;
  align-items: center;
  margin: 20px 0;
  padding: 20px;
  background-color: #F5F7FA;
  border-radius: 4px;
}

.score-desc {
  margin-left: 30px;
}

.score-desc h4 {
  margin: 0 0 10px 0;
}

.score-desc p {
  margin: 0;
  color: #606266;
}

.metric-details {
  padding: 10px;
}

.suggestion-item {
  margin: 5px 0;
  color: #606266;
}

.suggestion-item i {
  margin-right: 5px;
  color: #409EFF;
}

.progress-content {
  font-size: 24px;
  font-weight: bold;
}

.progress-label {
  font-size: 14px;
  margin-left: 2px;
}

.score-tags {
  margin-top: 10px;
}

.score-tags .el-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.help-content {
  line-height: 1.6;
}

.help-content h4 {
  margin: 16px 0 8px;
  color: #303133;
}

.help-content ul,
.help-content ol {
  padding-left: 20px;
  margin: 8px 0;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity .3s;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}
</style> 