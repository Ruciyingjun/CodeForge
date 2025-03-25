<template>
  <div class="app-container" v-hasPermi="['tool:code:list']" v-loading="loading" element-loading-text="加载中...">
    <!-- 简化的头部 -->
    <div class="page-header">
      <div class="header-title">
        <i class="el-icon-cpu"></i>
        代码质量分析
      </div>
    </div>

    <!-- 主要内容区域 -->
    <el-form ref="form" :model="form" :rules="rules">
      <!-- 语言选择 -->
      <el-form-item prop="language">
        <div class="language-select">
          <el-select 
            v-model="form.language" 
            placeholder="选择编程语言"
            @change="handleLanguageChange"
            class="dark-select">
            <el-option 
              v-for="lang in languages" 
              :key="lang.value" 
              :label="lang.label" 
              :value="lang.value">
              <span class="language-option">
                <i :class="lang.icon"></i>
                {{ lang.label }}
              </span>
            </el-option>
          </el-select>
        </div>
      </el-form-item>

      <!-- 代码输入区域 -->
      <el-form-item prop="code">
        <div class="code-input-container">
          <div class="input-tabs">
            <div 
              class="tab-item" 
              :class="{ active: activeTab === 'input' }"
              @click="activeTab = 'input'">
              <i class="el-icon-edit"></i>
              直接输入
            </div>
            <div 
              class="tab-item"
              :class="{ active: activeTab === 'upload' }"
              @click="activeTab = 'upload'">
              <i class="el-icon-upload"></i>
              文件上传
            </div>
          </div>

          <div class="input-content" v-loading="inputLoading">
            <!-- 直接输入面板 -->
            <div v-show="activeTab === 'input'" class="code-editor">
              <div class="editor-toolbar">
                <el-button-group>
                  <el-button 
                    type="text" 
                    icon="el-icon-delete"
                    @click="clearCode"
                    :disabled="!form.code">
                    清空
                  </el-button>
                  <el-button 
                    type="text" 
                    icon="el-icon-document"
                    @click="loadExample"
                    :disabled="!form.language">
                    加载示例
                  </el-button>
                </el-button-group>
              </div>
              <codemirror
                ref="cmEditor"
                v-model="form.code"
                :options="cmOptions"
                @input="onCodeChange">
              </codemirror>
            </div>

            <!-- 文件上传面板 -->
            <div v-show="activeTab === 'upload'" class="upload-panel">
              <el-upload
                class="code-uploader"
                drag
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
                :before-upload="beforeUpload"
                :show-file-list="false">
                <i class="el-icon-upload"></i>
                <div class="upload-text">
                  <p>将代码文件拖到此处，或<em>点击上传</em></p>
                  <p class="upload-tip">支持的文件类型：{{ fileExtensions.join(', ') }}</p>
                </div>
              </el-upload>
            </div>
          </div>
        </div>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <div class="action-bar">
          <el-button 
            type="primary" 
            :loading="analyzing" 
            @click="submitAnalysis"
            v-hasPermi="['tool:code:add']"
            :disabled="!form.code || !form.language">
            {{ analyzing ? '分析中...' : '开始分析' }}
          </el-button>
          <el-button 
            icon="el-icon-refresh"
            @click="resetForm"
            :disabled="analyzing">
            重置
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <!-- 分析结果展示 -->
    <transition name="fade">
      <analysis-report 
        v-if="report" 
        :report="report"
        @download="downloadReport"
        @close="closeReport"
      />
    </transition>
  </div>
</template>

<script>
import VueCodemirror from 'vue-codemirror'
// 导入基础样式
import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/monokai.css'
// 导入语言模式
import 'codemirror/mode/javascript/javascript.js'
import 'codemirror/mode/python/python.js'
import 'codemirror/mode/clike/clike.js'
import AnalysisReport from './components/AnalysisReport.vue'
import { getLanguages, analyzeCode } from "@/api/tool/code-analyzer"
import request from '@/utils/request'

export default {
  name: 'CodeAnalyzer',
  components: {
    AnalysisReport,
    codemirror: VueCodemirror.codemirror
  },
  data() {
    return {
      form: {
        language: '',
        code: '',
        fileName: '',
        options: {
          maxLineLength: 120,
          checkComments: true,
          checkNaming: true,
          checkComplexity: true,
          checkBestPractices: true
        }
      },
      rules: {
        language: [
          { required: true, message: '请选择编程语言', trigger: 'change' }
        ],
        code: [
          { required: true, message: '请输入或上传代码', trigger: 'blur' }
        ]
      },
      languages: [],
      analyzing: false,
      report: null,
      activeTab: 'input',
      activeMetrics: ['style', 'complexity'],
      helpDialogVisible: false,
      settingsDialogVisible: false,
      settings: {
        maxLineLength: 120,
        strictMode: false,
        checkItems: ['comments', 'naming', 'complexity', 'bestPractices']
      },
      cmOptions: {
        tabSize: 4,
        mode: 'text/javascript',
        theme: 'monokai',
        lineNumbers: true,
        line: true,
        lineWrapping: true,
        styleActiveLine: true,
        matchBrackets: true,
        scrollbarStyle: 'overlay'
      },
      fileExtensions: ['.java', '.py', '.js', '.jsx', '.ts', '.vue'],
      loading: false,
      analysisMode: 'basic',
      inputLoading: false
    }
  },
  computed: {
    scoreColor() {
      const score = this.report?.score || 0
      if (score >= 90) return '#67C23A'
      if (score >= 80) return '#409EFF'
      if (score >= 60) return '#E6A23C'
      return '#F56C6C'
    }
  },
  created() {
    this.getLanguages()
  },
  methods: {
    getLanguages() {
      getLanguages().then(res => {
        this.languages = res.data
      })
    },
    submitAnalysis() {
      this.report = null

      this.$refs.form.validate(valid => {
        if (!valid) {
          this.$message.warning('请完善表单信息')
          return
        }
        
        if (!this.form.code.trim()) {
          this.$message.warning('代码内容不能为空')
          return
        }

        this.analyzing = true
        
        const params = {
          language: this.form.language,
          code: this.form.code,
          options: this.form.options
        }

        analyzeCode(params)
          .then(res => {
            if (res.code === 200) {
              this.report = res.data
              this.$notify({
                title: '分析完成',
                message: '代码分析报告已生成',
                type: 'success'
              })
            } else {
              throw new Error(res.msg || '分析失败')
            }
          })
          .catch(error => {
            this.$message.error('分析失败：' + (error.message || '服务器错误'))
          })
          .finally(() => {
            this.analyzing = false
          })
      })
    },
    resetForm() {
      this.$refs.form.resetFields()
      this.report = null
      this.form.code = ''
      this.form.fileName = ''
    },
    clearCode() {
      this.form.code = ''
    },
    closeReport() {
      this.report = null
    },
    showHelp() {
      this.helpDialogVisible = true
    },
    showSettings() {
      this.settingsDialogVisible = true
    },
    saveSettings() {
      this.form.options = {
        maxLineLength: this.settings.maxLineLength,
        checkComments: this.settings.checkItems.includes('comments'),
        checkNaming: this.settings.checkItems.includes('naming'),
        checkComplexity: this.settings.checkItems.includes('complexity'),
        checkBestPractices: this.settings.checkItems.includes('bestPractices')
      }
      this.settingsDialogVisible = false
    },
    onCodeChange(code) {
      this.form.code = code
      this.$refs.form.validateField('code')
    },
    handleFileChange(file) {
      const ext = file.name.split('.').pop().toLowerCase()
      if (!this.fileExtensions.includes('.' + ext)) {
        this.$message.error('不支持的文件类型')
        return false
      }

      this.inputLoading = true
      
      const reader = new FileReader()
      reader.onload = (e) => {
        try {
          this.form.code = e.target.result
          this.form.fileName = file.name
          
          const langMap = {
            'java': { lang: 'java', mode: 'text/x-java' },
            'py': { lang: 'python', mode: 'text/x-python' },
            'js': { lang: 'javascript', mode: 'text/javascript' },
            'jsx': { lang: 'javascript', mode: 'text/javascript' },
            'ts': { lang: 'typescript', mode: 'text/typescript' },
            'vue': { lang: 'vue', mode: 'text/x-vue' }
          }
          
          if (langMap[ext]) {
            this.form.language = langMap[ext].lang
            this.cmOptions.mode = langMap[ext].mode
          }
          
          this.activeTab = 'input'
          this.$message.success('文件上传成功')

          this.submitAnalysis()
        } catch (error) {
          this.$message.error('文件读取失败：' + error.message)
        } finally {
          this.inputLoading = false
        }
      }
      
      reader.onerror = () => {
        this.$message.error('文件读取失败')
        this.inputLoading = false
      }
      
      reader.readAsText(file.raw)
      return false
    },
    beforeUpload(file) {
      const ext = file.name.split('.').pop().toLowerCase()
      const isValidType = this.fileExtensions.includes('.' + ext)
      
      if (!isValidType) {
        this.$message.error('只支持以下文件类型：' + this.fileExtensions.join(', '))
      }
      
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isLt2M) {
        this.$message.error('文件大小不能超过 2MB!')
      }
      
      return isValidType && isLt2M
    },
    onCopy() {
      this.$message.success('代码已复制到剪贴板')
    },
    onCopyError() {
      this.$message.error('复制失败')
    },
    loadExample() {
      // TODO: 根据选择的语言加载示例代码
    },
    getScoreDesc(score) {
      if (score >= 90) return '代码质量优秀'
      if (score >= 80) return '代码质量良好'
      if (score >= 60) return '代码质量一般'
      return '代码质量需要改进'
    },
    getTagType(score) {
      if (score >= 90) return 'success'
      if (score >= 80) return ''
      if (score >= 60) return 'warning'
      return 'danger'
    },
    getMetricName(key) {
      const names = {
        style: '代码规范',
        complexity: '复杂度',
        naming: '命名规范',
        oop: '面向对象',
        es6: 'ES6+特性',
        exception: '异常处理',
        bestPractices: '最佳实践'
      }
      return names[key] || key
    },
    getMetricIcon(key) {
      const icons = {
        style: 'el-icon-s-check',
        complexity: 'el-icon-s-data',
        naming: 'el-icon-edit',
        oop: 'el-icon-s-grid',
        es6: 'el-icon-s-promotion',
        exception: 'el-icon-warning',
        bestPractices: 'el-icon-s-management'
      }
      return icons[key] || 'el-icon-info'
    },
    getDetailLabel(key) {
      const labels = {
        cyclomaticComplexity: '圈复杂度',
        maxNestingDepth: '最大嵌套深度',
        methodCount: '方法数量',
        fieldCount: '字段数量',
        cohesion: '内聚度',
        inheritanceDepth: '继承深度',
        callbackNesting: '回调嵌套',
        totalLines: '总行数',
        codeLines: '代码行数',
        commentLines: '注释行数',
        emptyLines: '空行数'
      }
      return labels[key] || key
    },
    formatDetailValue(value) {
      if (typeof value === 'boolean') {
        return value ? '是' : '否'
      }
      if (typeof value === 'number') {
        if (value >= 0 && value <= 1) {
          return (value * 100).toFixed(1) + '%'
        }
        return value.toString()
      }
      return value
    },
    downloadReport() {
      if (!this.report) return
      
      let text = '代码质量分析报告\n'
      text += '=================\n\n'
      text += `分析时间：${this.report.analyzeTime}\n`
      text += `总体评分：${this.report.score}分\n\n`
      
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
      
      const blob = new Blob([text], { type: 'text/plain;charset=utf-8' })
      const fileName = this.form.fileName || 
        `code-analysis-report-${new Date().toISOString().slice(0,10)}.txt`
      
      if (window.navigator.msSaveOrOpenBlob) {
        window.navigator.msSaveBlob(blob, fileName)
      } else {
        const link = document.createElement('a')
        link.href = window.URL.createObjectURL(blob)
        link.download = fileName
        link.click()
        window.URL.revokeObjectURL(link.href)
      }
    },
    handleLanguageChange(value) {
      // 根据选择的语言更新编辑器模式
      const modeMap = {
        java: 'text/x-java',
        python: 'text/x-python',
        javascript: 'text/javascript'
      }
      this.cmOptions.mode = modeMap[value] || 'text/plain'
      
      // 清空代码
      this.form.code = ''
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
  background-color: #1e1e1e;
  min-height: calc(100vh - 84px);
  color: #fff;
}

.page-header {
  margin-bottom: 20px;
  
  .header-title {
    font-size: 16px;
    display: flex;
    align-items: center;
    
    i {
      margin-right: 8px;
      color: #409EFF;
    }
  }
}

.language-select {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  
  .label {
    margin-right: 10px;
    color: #909399;
  }
}

.code-input-container {
  background: #252526;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 20px;

  .input-tabs {
    display: flex;
    background: #2d2d2d;
    padding: 4px;
    gap: 4px;

    .tab-item {
      padding: 8px 16px;
      border-radius: 4px;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 6px;
      color: #909399;
      transition: all 0.3s ease;

      i {
        font-size: 16px;
      }

      &:hover {
        color: #fff;
        background: #363636;
      }

      &.active {
        color: #fff;
        background: #363636;
      }
    }
  }

  .input-content {
    min-height: 400px;
  }

  .code-editor {
    height: 100%;

    .editor-toolbar {
      padding: 8px 12px;
      background: #2d2d2d;
      border-bottom: 1px solid #363636;

      .el-button {
        color: #909399;
        padding: 6px 12px;

        &:hover:not([disabled]) {
          color: #fff;
        }

        &[disabled] {
          color: #606266;
        }

        i {
          margin-right: 4px;
        }
      }
    }

    ::v-deep .CodeMirror {
      height: 400px;
      background: #1e1e1e !important;
      color: #d4d4d4 !important;
      font-family: 'Fira Code', Consolas, monospace;
      padding: 12px;

      .CodeMirror-gutters {
        background: #252526 !important;
        border-right: 1px solid #363636;
      }

      .CodeMirror-linenumber {
        color: #858585;
      }

      .CodeMirror-cursor {
        border-left: 2px solid #fff;
      }

      // 语法高亮
      .cm-keyword { color: #569cd6; }
      .cm-operator { color: #d4d4d4; }
      .cm-string { color: #ce9178; }
      .cm-number { color: #b5cea8; }
      .cm-comment { color: #6a9955; }
    }
  }

  .upload-panel {
    height: 400px;
    display: flex;
    align-items: center;
    justify-content: center;

    .code-uploader {
      width: 100%;
      height: 100%;

      ::v-deep .el-upload {
        width: 100%;
        height: 100%;

        .el-upload-dragger {
          width: 100%;
          height: 100%;
          background: #1e1e1e;
          border: 2px dashed #363636;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          transition: all 0.3s;

          &:hover {
            border-color: #409EFF;
            background: #252526;
          }

          i {
            font-size: 48px;
            color: #409EFF;
            margin-bottom: 16px;
          }

          .upload-text {
            text-align: center;
            color: #909399;

            p {
              margin: 0;
              line-height: 1.6;

              em {
                color: #409EFF;
                font-style: normal;
                cursor: pointer;
              }
            }

            .upload-tip {
              margin-top: 8px;
              font-size: 13px;
              color: #606266;
            }
          }
        }
      }
    }
  }
}

.action-bar {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
}

// 深色主题覆盖
::v-deep {
  .el-select {
    .el-input__inner {
      background-color: #252526;
      border-color: #434343;
      color: #fff;
    }
  }
  
  .el-tabs {
    background-color: transparent;
    border: none;
    
    .el-tabs__header {
      background-color: #252526;
      border-bottom: 1px solid #333;
    }
    
    .el-tabs__item {
      color: #909399;
      border: none;
      
      &.is-active {
        color: #409EFF;
        background-color: #252526;
      }
    }
  }
  
  .CodeMirror {
    background-color: #1e1e1e !important;
    color: #d4d4d4 !important;
    border: 1px solid #333;
    
    .CodeMirror-gutters {
      background-color: #252526 !important;
      border-right: 1px solid #333;
    }
    
    .CodeMirror-linenumber {
      color: #858585 !important;
    }
  }
  
  .el-upload {
    background-color: #252526;
    border: 1px dashed #434343;
    
    &:hover {
      border-color: #409EFF;
    }
    
    .el-upload__text {
      color: #909399;
      
      em {
        color: #409EFF;
      }
    }
  }

  .el-select-dropdown {
    background-color: #252526;
    border-color: #434343;
    
    .el-select-dropdown__item {
      color: #d4d4d4;
      
      &.hover, &:hover {
        background-color: #2d2d2d;
      }
      
      &.selected {
        background-color: #094771;
        color: #fff;
      }
    }
  }

  .el-button--primary {
    background-color: #0e639c;
    border-color: #0e639c;
    
    &:hover, &:focus {
      background-color: #1177bb;
      border-color: #1177bb;
    }
  }

  .el-button--default {
    background-color: #313131;
    border-color: #3c3c3c;
    color: #cccccc;
    
    &:hover, &:focus {
      background-color: #3c3c3c;
      border-color: #4c4c4c;
      color: #ffffff;
    }
  }

  .el-tabs__item {
    &.is-active {
      background-color: #1e1e1e;
      border-bottom-color: #0e639c;
    }
  }

  .el-upload {
    color: #d4d4d4;
    
    &:hover {
      .el-upload-dragger {
        border-color: #0e639c;
      }
    }
  }

  .el-upload-dragger {
    background-color: #252526;
    border-color: #434343;
    
    &:hover {
      border-color: #0e639c;
    }
    
    .el-icon-upload {
      color: #0e639c;
    }
  }
}
</style> 