<template>
  <div class="code-generation dark-theme">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-title">
        <h2>CodeForge AI</h2>
        <div class="model-info">
          <span>由</span>
          <div class="model-badge">
            <i class="el-icon-cpu"></i>
            <span class="model-name">{{ aiModelName }}</span>
            <el-tooltip effect="dark" placement="bottom">
              <div slot="content">
                <p>当前AI模型: {{ aiModelName }}</p>
                <p>类型: {{ aiModelType }}</p>
                <p>可在系统设置中修改</p>
              </div>
              <i class="el-icon-info model-info-icon"></i>
            </el-tooltip>
          </div>
          <span>驱动</span>
        </div>
      </div>
    </div>

    <!-- 主生成按钮 -->
    <div class="generate-button-wrapper">
      <el-button class="main-generate-btn" @click="openDialog">
        <div class="btn-content">
          <div class="btn-main">
            <i class="el-icon-magic-stick"></i>
            <span class="btn-text">点击生成</span>
          </div>
        </div>
      </el-button>
    </div>

    <!-- 快速选择区域改为常用场景 -->
    <div class="scenario-section">
      <h3 class="section-title">
        <i class="el-icon-star-off"></i>
        常用场景
      </h3>
      <div class="scenario-grid">
        <div v-for="scenario in commonScenarios"
             :key="scenario.id"
             class="scenario-card"
             @click="useScenario(scenario)">
          <div class="scenario-icon">
            <i :class="scenario.icon"></i>
          </div>
          <div class="scenario-info">
            <h4>{{ scenario.title }}</h4>
            <p>{{ scenario.description }}</p>
          </div>
          <div class="scenario-tag" :class="scenario.type">
            {{ scenario.language }}
          </div>
        </div>
      </div>
    </div>

    <!-- 历史记录轮播图 -->
    <div class="history-carousel">
      <div class="section-header">
        <h3 class="section-title">
          <i class="el-icon-time"></i>
          历史生成案例
        </h3>
      </div>

      <el-carousel
        :interval="2000"
        type="card"
        height="300px"
        :autoplay="true"
        arrow="never"
        :initial-index="0"
        indicator-position="none"
        :pause-on-hover="false">
        <el-carousel-item v-for="item in historyRecords" :key="item.id">
          <div class="history-card" :class="getCardClass(item.language)">
            <div class="card-header">
              <div class="header-left">
                <i :class="getLanguageIcon(item.language)"></i>
                <span class="language-tag">{{ item.language.toUpperCase() }}</span>
                <span class="create-time">{{ item.createTime }}</span>
              </div>
            </div>

            <div class="card-content">
              <h4 class="card-title">{{ item.title }}</h4>
              <p class="card-desc">{{ item.description }}</p>

              <div class="code-preview">
                <div class="preview-header">
                  <span>代码示例</span>
                </div>
                <pre class="code-block"><code v-html="highlightCode(item.code, item.language)"></code></pre>
              </div>
            </div>

            <div class="card-footer">
              <div class="tags">
                <el-tag size="mini" type="info">{{ item.language }}</el-tag>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 生成对话框 -->
    <el-dialog
      :visible.sync="dialogVisible"
      title="代码生成"
      width="70%"
      custom-class="code-dialog"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="!generating">

      <div class="dialog-content">
        <div class="input-section">
          <div class="section-header">
            <h3>编写需求</h3>
            <div class="language-select">
              <span>当前语言：</span>
              <el-select v-model="form.language" size="small">
                <el-option v-for="lang in languages"
                          :key="lang.value"
                          :label="lang.label"
                          :value="lang.value">
                  <i :class="lang.icon"></i>
                  <span>{{ lang.label }}</span>
                </el-option>
              </el-select>
            </div>
          </div>

          <el-input
            v-model="form.userInput"
            type="textarea"
            :rows="8"
            :placeholder="getInputPlaceholder()"
            :disabled="generating">
          </el-input>

          <div class="input-tips">
            <i class="el-icon-info"></i>
            <span>提示：描述越详细，生成的代码质量越高</span>
          </div>
        </div>

        <div v-if="generating" class="generating-animation">
          <div class="animation-content">
            <i class="el-icon-loading"></i>
            <span>正在生成代码，请稍候...</span>
            <div class="progress-bar">
              <div class="progress" :style="{ width: generatingProgress + '%' }"></div>
            </div>
          </div>
        </div>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelGeneration"
                   :disabled="generating">
          取消
        </el-button>
        <el-button type="primary"
                   @click="generateCode"
                   :loading="generating"
                   :disabled="!form.userInput.trim()">
          {{ generating ? '生成中...' : '开始生成' }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 生成结果展示 -->
    <transition name="fade">
      <div v-if="generatedCode" class="result-container">
        <div class="result-header">
          <div class="header-left">
            <i :class="getLanguageIcon(form.language)"></i>
            <span class="language-tag">{{ form.language.toUpperCase() }}</span>
            <span class="generate-time">
              <i class="el-icon-time"></i>
              {{ getCurrentTime() }}
            </span>
          </div>
          <div class="header-actions">
            <el-tooltip content="复制代码" placement="top">
              <el-button type="text" icon="el-icon-document-copy" @click="copyCode">
                复制代码
              </el-button>
            </el-tooltip>
            <el-tooltip content="下载代码" placement="top">
              <el-button type="text" icon="el-icon-download" @click="downloadCode">
                下载代码
              </el-button>
            </el-tooltip>
          </div>
        </div>

        <div class="code-container">
          <div class="code-header">
            <div class="file-info">
              <i class="el-icon-document"></i>
              <span class="file-name">generated_code.{{ getFileExtension() }}</span>
            </div>
            <div class="line-numbers">{{ getCodeLines() }} 行代码</div>
          </div>
          <pre class="generated-code" v-highlightjs>
            <code :class="form.language">{{ generatedCode }}</code>
          </pre>
        </div>

        <div v-if="codeExplanation" class="code-explanation">
          <div class="explanation-header">
            <i class="el-icon-info"></i>
            <span>代码说明</span>
          </div>
          <div class="explanation-content markdown-body" v-html="formatExplanation(codeExplanation)"></div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import request from '@/utils/request'
import hljs from 'highlight.js'
import 'highlight.js/styles/vs2015.css'
import 'highlight.js/styles/atom-one-dark.css'
import { AI_CONFIG } from '@/constants/ai'  // 添加导入

export default {
  name: 'CodeGeneration',
  data() {
    return {
      aiModelName: AI_CONFIG.MODEL_NAME,  // 添加配置
      aiModelType: AI_CONFIG.MODEL_TYPE,  // 添加模型类型配置
      dialogVisible: false,
      generating: false,
      form: {
        language: 'java',
        userInput: ''
      },
      generatedCode: '',
      codeExplanation: '',
      languages: [
        { label: 'Java', value: 'java', icon: 'el-icon-s-platform', desc: '后端开发' },
        { label: 'Python', value: 'python', icon: 'el-icon-s-data', desc: '数据处理' },
        { label: 'JavaScript', value: 'javascript', icon: 'el-icon-s-promotion', desc: '前端开发' },
        { label: 'Vue', value: 'vue', icon: 'el-icon-s-grid', desc: '组件开发' },
        { label: 'SQL', value: 'sql', icon: 'el-icon-s-order', desc: '数据库操作' }
      ],  // 添加逗号
      historyRecords: [
        {
          id: 1,
          language: 'java',
          title: 'Spring Boot REST API',
          description: '生成了一个包含用户CRUD操作的RESTful API接口',
          code: 'public class UserController {\n    @GetMapping("/users")...',
          createTime: '2024-01-15 14:30'
        },
        {
          id: 2,
          language: 'python',
          title: '数据分析脚本',
          description: '生成了一个处理CSV文件并进行数据分析的Python脚本',
          code: 'import pandas as pd\ndef analyze_data()...',
          createTime: '2024-01-15 15:20'
        },
        {
          id: 3,
          language: 'vue',
          title: '表单组件',
          description: '生成了一个带验证的动态表单组件',
          code: '<template>\n  <el-form :model="form"...',
          createTime: '2024-01-15 16:10'
        },
        {
          id: 4,
          language: 'javascript',
          title: '数据可视化图表',
          description: '生成了一个使用ECharts的交互式数据展示图表',
          code: 'const option = {\n  title: { text: "销售数据分析" }...',
          createTime: '2024-01-15 16:45'
        },
        {
          id: 5,
          language: 'sql',
          title: '数据库查询优化',
          description: '生成了一个包含多表关联的高性能SQL查询',
          code: 'SELECT u.name, o.order_id\nFROM users u\nLEFT JOIN orders o...',
          createTime: '2024-01-15 17:20'
        },
        {
          id: 6,
          language: 'java',
          title: '文件上传服务',
          description: '生成了一个支持断点续传的文件上传服务',
          code: '@PostMapping("/upload")\npublic ResponseEntity<String> uploadFile...',
          createTime: '2024-01-15 17:50'
        },
        {
          id: 7,
          language: 'python',
          title: '机器学习模型',
          description: '生成了一个基于scikit-learn的分类模型训练脚本',
          code: 'from sklearn.ensemble import RandomForestClassifier\n...',
          createTime: '2024-01-15 18:15'
        }
      ],
      // 添加常用场景数据
      commonScenarios: [
        {
          id: 1,
          title: 'CRUD 接口',
          description: '生成标准的增删改查 RESTful API',
          icon: 'el-icon-s-operation',
          language: 'Java',
          type: 'backend',
          template: {
            language: 'java',
            userInput: '请生成一个包含标准CRUD操作的RESTful API控制器，包括：\n1. 列表查询（分页）\n2. 单条查询\n3. 新增\n4. 修改\n5. 删除\n需要包含参数校验和异常处理'
          }
        },
        {
          id: 2,
          title: 'Vue 组件',
          description: '生成包含数据表格的Vue组件',
          icon: 'el-icon-s-grid',
          language: 'Vue',
          type: 'frontend',
          template: {
            language: 'vue',
            userInput: '请生成一个Vue组件，包含：\n1. 数据表格\n2. 搜索表单\n3. 新增/编辑弹窗\n4. 分页功能\n需要包含基础的增删改查功能'
          }
        },
        {
          id: 3,
          title: '数据处理',
          description: '生成数据处理和分析脚本',
          icon: 'el-icon-s-data',
          language: 'Python',
          type: 'data',
          template: {
            language: 'python',
            userInput: '请生成一个数据处理脚本，功能包括：\n1. 读取CSV文件\n2. 数据清洗\n3. 基础统计分析\n4. 结果导出'
          }
        },
        {
          id: 4,
          title: '工具函数',
          description: '生成常用工具函数集合',
          icon: 'el-icon-s-tools',
          language: 'JavaScript',
          type: 'util',
          template: {
            language: 'javascript',
            userInput: '请生成一个工具函数集合，包含：\n1. 日期格式化\n2. 数据验证\n3. 数组处理\n4. 对象操作\n需要包含详细的注释和使用示例'
          }
        }
      ]
    }
  },
  created() {
    // 确保没有手动控制侧边栏的代码
    // ❌ 不应该有类似这样的代码：
    // this.$store.dispatch('app/toggleSideBar', false)
  },
  beforeDestroy() {
    // 确保组件销毁时不影响侧边栏
  },
  directives: {
    highlightjs: {
      inserted: function(el) {
        let blocks = el.querySelectorAll('code')
        blocks.forEach((block) => {
          hljs.highlightBlock(block)
        })
      },
      componentUpdated: function(el) {
        let blocks = el.querySelectorAll('code')
        blocks.forEach((block) => {
          hljs.highlightBlock(block)
        })
      }
    }
  },
  methods: {
    openDialog() {
      this.dialogVisible = true
      this.form.userInput = ''
      this.generatedCode = ''
      this.codeExplanation = ''
    },

    getCardClass(language) {
      const classMap = {
        java: 'java-card',
        python: 'python-card',
        javascript: 'javascript-card',
        vue: 'vue-card',
        sql: 'sql-card'
      }
      return classMap[language] || 'default-card'
    },

    async generateCode() {
      if (!this.form.userInput.trim()) {
        this.$message.warning('请输入代码需求描述')
        return
      }

      this.generating = true
      try {
        const response = await request({
          url: '/system/ai/generateCode',
          method: 'post',
          data: {
            language: this.form.language,
            prompt: this.form.userInput
          }
        })

        if (response.code === 200) {
          this.generatedCode = response.data.code
          this.codeExplanation = response.data.explanation
          this.dialogVisible = false
          this.$message.success('代码生成成功')
        } else {
          this.$message.error(response.msg || '代码生成失败')
        }
      } catch (error) {
        this.$message.error('代码生成失败：' + (error.message || '未知错误'))
      } finally {
        this.generating = false
      }
    },
    copyCode() {
      const textarea = document.createElement('textarea')
      textarea.value = this.generatedCode
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('代码已复制到剪贴板')
    },  // 添加逗号
    getLanguageIcon() {
      const lang = this.languages.find(l => l.value === this.form.language)
      return lang ? lang.icon : 'el-icon-code'
    },
    downloadCode() {
      const blob = new Blob([this.generatedCode], { type: 'text/plain' })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `generated_code.${this.form.language}`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(url)
      this.$message.success('代码文件已下载')
    },  // 添加逗号
    viewHistoryCode(item) {
      this.generatedCode = item.code
      this.codeExplanation = item.description
      this.form.language = item.language
    },
    copyHistoryCode(item) {
      const textarea = document.createElement('textarea')
      textarea.value = item.code
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      this.$message.success('代码已复制到剪贴板')
    },
    openDocumentation() {
      window.open('/documentation', '_blank')
    },
    formatPreviewCode(code) {
      return code.length > 150 ? code.slice(0, 150) + '...' : code;
    },
    formatExplanation(text) {
      return text.replace(/\n/g, '<br>');
    },
    getCurrentTime() {
      return new Date().toLocaleString();
    },
    getInputPlaceholder() {
      return `请详细描述您的代码需求，例如：
1. 实现什么功能？
2. 需要哪些输入参数？
3. 期望的输出结果是什么？
4. 有什么特殊要求或限制？
5. 是否需要特定的设计模式？
6. 是否需要异常处理？`;
    },
    selectLanguage(lang) {
      this.form.language = lang;
      this.openDialog();
    },
    cancelGeneration() {
      if (this.generating) {
        // 添加取消确认
        this.$confirm('确定要取消代码生成吗？', '提示', {
          type: 'warning'
        }).then(() => {
          this.dialogVisible = false;
        }).catch(() => {});
      } else {
        this.dialogVisible = false;
      }
    },
    deleteHistoryItem(item) {
      this.$confirm('确定要删除这条记录吗？', '提示', {
        type: 'warning'
      }).then(() => {
        const index = this.historyRecords.findIndex(record => record.id === item.id);
        if (index > -1) {
          this.historyRecords.splice(index, 1);
        }
      }).catch(() => {});
    },
    clearHistory() {
      this.$confirm('确定要清除所有历史记录吗？', '提示', {
        type: 'warning'
      }).then(() => {
        this.historyRecords = [];
      }).catch(() => {});
    },
    // 添加使用场景模板的方法
    useScenario(scenario) {
      this.form.language = scenario.template.language;
      this.form.userInput = scenario.template.userInput;
      this.openDialog();
    },
    handleCommand(command, item) {
      if (command === 'view') {
        this.viewHistoryCode(item);
      } else if (command === 'copy') {
        this.copyHistoryCode(item);
      } else if (command === 'delete') {
        this.deleteHistoryItem(item);
      }
    },
    regenerateCode(item) {
      this.form.language = item.language;
      this.form.userInput = item.description;
      this.openDialog();
    },
    // 修改高亮方法
    highlightCode(code, language) {
      try {
        const lang = this.getHighlightLanguage(language);
        const highlighted = hljs.highlight(code.slice(0, 150), {
          language: lang
        }).value;
        return highlighted + '...';
      } catch (e) {
        return code.slice(0, 150) + '...';
      }
    },
    getHighlightLanguage(language) {
      const langMap = {
        'java': 'java',
        'python': 'python',
        'javascript': 'javascript',
        'vue': 'html',  // Vue 文件使用 HTML 高亮
        'sql': 'sql'
      };
      return langMap[language.toLowerCase()] || 'plaintext';
    },
    getFileExtension() {
      const extMap = {
        'java': 'java',
        'python': 'py',
        'javascript': 'js',
        'vue': 'vue',
        // 'sql': 'sql'
      };
      return extMap[this.form.language.toLowerCase()] || 'txt';
    },
    getCodeLines() {
      return this.generatedCode.split('\n').length;
    }
  }
}
</script>

<style lang="scss" scoped>
/* 修改整体样式 */
.code-generation {
  padding: 24px;
  background: #0D0D0D;
  min-height: calc(100vh - 100px);
  color: #ffffff;
}

/* 页面标题样式 */
.page-header {
  margin-bottom: 32px;
  text-align: center;

  .header-title {
    h2 {
      margin: 0 0 8px;
      font-size: 32px;
      font-weight: 600;
      background: linear-gradient(45deg, #409EFF, #66b1ff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }

    .model-info {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      margin-top: 12px;

      > span {
        color: #8E8EA0;
        font-size: 14px;
      }

      .model-badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 6px 12px;
        background: rgba(64, 158, 255, 0.1);
        border: 1px solid rgba(64, 158, 255, 0.2);
        border-radius: 20px;
        transition: all 0.3s ease;

        &:hover {
          background: rgba(64, 158, 255, 0.15);
          border-color: rgba(64, 158, 255, 0.3);
          transform: translateY(-1px);
        }

        i.el-icon-cpu {
          font-size: 14px;
          color: #409EFF;
        }

        .model-name {
          color: #409EFF;
          font-weight: 500;
          font-size: 14px;
        }

        .model-info-icon {
          font-size: 14px;
          color: #8E8EA0;
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            color: #409EFF;
          }
        }
      }
    }
  }
}

/* 生成按钮样式优化 */
.generate-button-wrapper {
  text-align: center;
  margin: 32px 0;
}

.main-generate-btn {
  width: auto;
  min-width: 180px;
  height: 56px;
  padding: 0 28px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);

    &::before {
      opacity: 1;
    }
  }

  &:active {
    transform: translateY(1px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  }

  .btn-content {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;

    .btn-main {
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon-magic-stick {
        font-size: 20px;
      }

      .btn-text {
        font-size: 16px;
        font-weight: 600;
        color: #ffffff;
      }
    }
  }
}

/* 历史记录轮播样式 */
.history-carousel {
  margin: 40px 0;
  padding: 32px;
  background: rgba(26, 26, 26, 0.6);
  border-radius: 16px;
  border: 1px solid #262626;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .section-title {
      margin: 0;
      font-size: 20px;
      color: #E0E0E0;
      display: flex;
      align-items: center;
      gap: 8px;

      i {
        color: #409EFF;
      }
    }
  }

  .history-card {
    height: 100%;
    padding: 20px;
    background: #262626;
    border-radius: 12px;
    border: 1px solid #404040;
    display: flex;
    flex-direction: column;
    pointer-events: none;
    user-select: none;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;

    .card-content {
      flex: 1;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }

    .card-title {
      margin: 0 0 8px;
      font-size: 16px;
      color: #E0E0E0;
      line-height: 1.4;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .card-desc {
      margin: 0 0 12px;
      font-size: 13px;
      color: #8E8EA0;
      line-height: 1.5;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .code-preview {
      flex: 1;
      display: flex;
      flex-direction: column;
      background: #1A1A1A;
      border-radius: 6px;
      border: 1px solid #333333;
      overflow: hidden;

      .preview-header {
        padding: 8px 12px;
        border-bottom: 1px solid #333333;

        span {
          font-size: 12px;
          color: #8E8EA0;
        }
      }

      .code-block {
        margin: 0;
        padding: 12px;
        background: #1a1a1a !important;
        border-radius: 4px;

        code {
          font-family: 'Fira Code', monospace;
          font-size: 12px;
          line-height: 1.5;

          :deep(.hljs) {
            background: transparent;
            color: #abb2bf;

            .hljs-keyword { color: #c678dd; }
            .hljs-string { color: #98c379; }
            .hljs-number { color: #d19a66; }
            .hljs-function { color: #61afef; }
            .hljs-title { color: #61afef; }
            .hljs-params { color: #e06c75; }
            .hljs-comment { color: #5c6370; }
            .hljs-tag { color: #e06c75; }
            .hljs-attr { color: #d19a66; }
          }
        }
      }
    }
  }
}

.el-carousel__arrow {
  background-color: rgba(0, 0, 0, 0.6) !important;
  border: 1px solid #404040;

  &:hover {
    background-color: rgba(64, 158, 255, 0.8) !important;
  }
}

/* 修改对话框样式 */
.code-dialog {
  background: #1A1A1A !important;
  border: 1px solid #262626;
  border-radius: 12px;
}

/* 修改输入框样式 */
.input-field {
  background: #262626;
  border: 1px solid #404040;
  color: #ffffff;
}

.input-field:focus {
  border-color: #ffffff;
  box-shadow: none;
}

/* 修改历史记录区域样式 */
.history-carousel {
  margin: 32px 0;
  padding: 24px;
  background: #1A1A1A;
  border-radius: 12px;
  border: 1px solid #262626;
}

.section-title {
  margin: 0 0 20px;
  font-size: 18px;
  color: #ffffff;
  display: flex;
  align-items: center;
}

.section-title i {
  margin-right: 8px;
  color: #ffffff;
}

/* 统一历史卡片样式 */
.history-card,
.history-card.java-card,
.history-card.python-card,
.history-card.javascript-card,
.history-card.vue-card,
.history-card.sql-card {
  height: 100%;
  padding: 16px;
  border-radius: 8px;
  background: #262626;
  border: 1px solid #404040;
  box-shadow: none;
  transition: all 0.3s ease;
}

.history-card:hover {
  transform: none;
  border-color: #404040;
  box-shadow: none;
}

.card-header {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #404040;
  color: #ffffff;
}

.card-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 500;
  color: #ffffff;
}

.card-desc {
  margin: 0 0 12px;
  font-size: 13px;
  color: #8E8EA0;
  line-height: 1.5;
}

.language-tag {
  font-size: 12px;
  color: #ffffff;
  background: #404040;
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: 8px;
}

.create-time {
  color: #8E8EA0;
  font-size: 12px;
}

/* 修改代码预览样式 */
.code-preview {
  margin-bottom: 12px;
  padding: 12px;
  background: #1A1A1A;
  border-radius: 6px;
  border: 1px solid #404040;
}

.code-preview pre,
.code-preview code {
  margin: 0;
  font-family: 'Fira Code', monospace;
  font-size: 12px;
  color: #A9A9B3;
  line-height: 1.5;
}

/* 修改按钮样式 */
.card-actions {
  padding-top: 12px;
  border-top: 1px solid #404040;
}

.el-button--text {
  color: #A9A9B3;
  font-size: 13px;
}

.el-button--text:hover {
  color: #ffffff;
}

/* 修改轮播样式 */
.el-carousel__item {
  opacity: 0.4;
  transform: scale(0.9);
  transition: all 0.4s ease;
  pointer-events: none;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;

  &.is-active {
    opacity: 1;
    transform: scale(1);
  }
}

.el-carousel--card {
  padding: 8px 0 32px;
}

.el-carousel__indicators {
  bottom: 0;
}

.el-carousel__indicator .el-carousel__button {
  background: #404040;
}

.el-carousel__indicator.is-active .el-carousel__button {
  background: #ffffff;
}

/* 修改对话框样式 */
.dialog-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #404040;
  color: #ffffff;
}

.dialog-header i {
  color: #ffffff;
}

.generation-form {
  padding: 0 20px;
}

/* 修改表单元素样式 */
.el-form-item__label {
  color: #ffffff !important;
}

.language-select :deep(.el-input__inner) {
  background: #262626;
  border: 1px solid #404040;
  color: #ffffff;
}

.language-option {
  background: #262626;
  color: #ffffff;
}

.language-desc {
  color: #8E8EA0;
}

.input-tips {
  color: #8E8EA0;
}

/* 修改生成结果容器样式 */
.result-container {
  margin-top: 24px;
  background: #1A1A1A;
  border: 1px solid #262626;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.result-header {
  padding: 16px 20px;
  background: #262626;
  border-bottom: 1px solid #333;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.el-button {
  padding: 8px 16px;
  border: 1px solid #404040;
  border-radius: 4px;
  transition: all 0.3s;

  &:hover {
    background: rgba(64, 158, 255, 0.1);
    border-color: #409EFF;
    color: #409EFF;
  }

  i {
    margin-right: 4px;
  }
}

.code-container {
  .code-header {
    padding: 12px 20px;
    background: #262626;
    border-bottom: 1px solid #333;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .file-info {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #8E8EA0;
      font-size: 13px;

      i {
        font-size: 16px;
      }
    }

    .line-numbers {
      font-size: 12px;
      color: #666;
      padding: 2px 8px;
      background: #1A1A1A;
      border-radius: 4px;
    }
  }

  .generated-code {
    margin: 0;
    padding: 20px;
    background: #1A1A1A;
    font-family: 'Fira Code', monospace;
    font-size: 14px;
    line-height: 1.6;
    overflow-x: auto;

    &::-webkit-scrollbar {
      height: 8px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: #333;
      border-radius: 4px;

      &:hover {
        background: #444;
      }
    }
  }
}

.code-explanation {
  padding: 20px;
  background: #262626;
  border-top: 1px solid #333;
}

.explanation-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: #E0E0E0;
  font-size: 16px;
}

.explanation-content {
  color: #8E8EA0;
  font-size: 14px;
  line-height: 1.6;
  padding: 16px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  border: 1px solid #333;
}

.fade-enter-active, .fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter, .fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 修改场景选择区域样式 */
.scenario-section {
  margin: 32px 0;
  padding: 24px;
  background: rgba(26, 26, 26, 0.6);
  border-radius: 16px;
  border: 1px solid #262626;

  .scenario-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 20px;
    margin-top: 20px;
  }

  .scenario-card {
    position: relative;
    padding: 20px;
    background: #262626;
    border-radius: 12px;
    border: 1px solid #404040;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 16px;

    &:hover {
      transform: translateY(-2px);
      border-color: #409EFF;
      background: #2c2c2c;
    }

    .scenario-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      background: rgba(64, 158, 255, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;

      i {
        font-size: 24px;
        color: #409EFF;
      }
    }

    .scenario-info {
      flex: 1;

      h4 {
        margin: 0 0 4px;
        font-size: 16px;
        color: #E0E0E0;
      }

      p {
        margin: 0;
        font-size: 13px;
        color: #8E8EA0;
      }
    }

    .scenario-tag {
      position: absolute;
      top: 12px;
      right: 12px;
      padding: 2px 8px;
      font-size: 12px;
      border-radius: 4px;
      font-weight: 500;

      &.backend {
        background: rgba(103, 194, 58, 0.15);
        color: #67C23A;
      }

      &.frontend {
        background: rgba(64, 158, 255, 0.15);
        color: #409EFF;
      }

      &.data {
        background: rgba(255, 184, 0, 0.15);
        color: #FFB800;
        border: 1px solid rgba(255, 184, 0, 0.3);
      }

      &.util {
        background: rgba(230, 162, 60, 0.15);
        color: #E6A23C;
      }
    }

    // 为数据处理卡片添加特殊悬停效果
    &:has(.scenario-tag.data):hover {
      border-color: #FFB800;
      box-shadow: 0 4px 12px rgba(255, 184, 0, 0.1);

      .scenario-icon {
        background: rgba(255, 184, 0, 0.15);

        i {
          color: #FFB800;
        }
      }
    }
  }
}

/* 确保轮播不会被鼠标事件影响 */
.el-carousel {
  pointer-events: none;

  &:hover .el-carousel__item {
    transform: scale(0.9);

    &.is-active {
      transform: scale(1);
    }
  }
}

/* 添加语言特定的卡片样式 */
.java-card {
  background: linear-gradient(145deg, #262626, #1a1a1a);
  border-color: rgba(255, 99, 71, 0.3);

  .language-tag {
    background: rgba(255, 99, 71, 0.15);
    color: #ff6347;
  }
}

.python-card {
  background: linear-gradient(145deg, #262626, #1a1a1a);
  border-color: rgba(65, 105, 225, 0.3);

  .language-tag {
    background: rgba(65, 105, 225, 0.15);
    color: #4169e1;
  }
}

.javascript-card {
  background: linear-gradient(145deg, #262626, #1a1a1a);
  border-color: rgba(255, 215, 0, 0.3);

  .language-tag {
    background: rgba(255, 215, 0, 0.15);
    color: #ffd700;
  }
}

.vue-card {
  background: linear-gradient(145deg, #262626, #1a1a1a);
  border-color: rgba(64, 184, 131, 0.3);

  .language-tag {
    background: rgba(64, 184, 131, 0.15);
    color: #40b883;
  }
}

.sql-card {
  background: linear-gradient(145deg, #262626, #1a1a1a);
  border-color: rgba(106, 90, 205, 0.3);

  .language-tag {
    background: rgba(106, 90, 205, 0.15);
    color: #6a5acd;
  }
}

.code-preview {
  .code-block {
    margin: 0;
    padding: 12px;
    background: #1a1a1a !important;
    border-radius: 4px;

    code {
      font-family: 'Fira Code', monospace;
      font-size: 12px;
      line-height: 1.5;

      :deep(.hljs) {
        background: transparent;
        color: #abb2bf;

        .hljs-keyword { color: #c678dd; }
        .hljs-string { color: #98c379; }
        .hljs-number { color: #d19a66; }
        .hljs-function { color: #61afef; }
        .hljs-title { color: #61afef; }
        .hljs-params { color: #e06c75; }
        .hljs-comment { color: #5c6370; }
        .hljs-tag { color: #e06c75; }
        .hljs-attr { color: #d19a66; }
      }
    }
  }
}
</style>
