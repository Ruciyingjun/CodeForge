<template>
  <div class="app-container">
    <el-card>
      <div slot="header">
        <span>Excel转SQL工具</span>
      </div>

      <el-form :model="form" label-width="120px" :rules="rules" ref="form" calss="form-container">
        <el-form-item label="选择文件" prop="file">
          <el-upload
            ref="upload"
            :action="uploadUrl"
            :before-upload="beforeUpload"
            :on-success="handleSuccess"
            :on-error="handleError"
            :data="form"
            :show-file-list="true"
            :limit="1"
            :on-exceed="handleExceed"
            :auto-upload="false"
            accept=".xlsx,.xls"
          >
            <el-button type="primary">
              <i class="el-icon-upload"></i> 选择Excel文件
            </el-button>
            <div slot="tip" class="el-upload__tip">只能上传xlsx/xls文件，且不超过2MB</div>
          </el-upload>
        </el-form-item>

        <el-form-item label="表名" prop="tableName">
          <el-input
            v-model="form.tableName"
            placeholder="请输入表名"
            :maxlength="50"
            class="custom-input"
          >
            <template #suffix>
              <span class="word-count" :class="{'word-count--warning': form.tableName.length >= 40}">
                {{ form.tableName.length }}/50
              </span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="数据库类型" prop="dbType">
          <el-select v-model="form.dbType" placeholder="请选择数据库类型">
            <el-option label="MySQL" value="mysql"></el-option>
            <el-option label="Oracle" value="oracle"></el-option>
            <el-option label="SQLServer" value="sqlserver"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="convertToSql" :loading="loading" :disabled="loading">
            {{ loading ? '转换中...' : '转换' }}
          </el-button>
          <el-button @click="downloadTemplate">下载模板</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- SQL预览区域 -->
      <el-card v-if="sqlResult" class="sql-preview">
        <div slot="header" class="sql-header">
          <span>生成的SQL</span>
          <div class="header-operations">
            <el-button 
              size="small" 
              type="primary" 
              icon="el-icon-document-copy"
              class="operation-btn" 
              @click="copySql"
            >
              复制SQL
            </el-button>
            <el-button 
              size="small" 
              type="success" 
              icon="el-icon-download"
              class="operation-btn" 
              @click="downloadSql"
            >
              下载SQL
            </el-button>
          </div>
        </div>
        <pre class="sql-content" v-html="highlightedSql"></pre>
      </el-card>
    </el-card>
  </div>
</template>


<script>
import { Message } from 'element-ui'
import axios from 'axios'
import hljs from 'highlight.js';  // 引入高亮插件
import 'highlight.js/styles/atom-one-dark.css'; // 引入高亮样式


export default {
  name: 'Excel2Sql',
  data() {
    return {
      uploadUrl: process.env.VUE_APP_BASE_API + '/tool/excel2sql/upload',
      form: {
        tableName: '',
        dbType: 'mysql'
      },
      rules: {
        tableName: [
          { required: true, message: '请输入表名', trigger: 'blur' },
          { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '表名只能包含字母、数字和下划线，且必须以字母开头', trigger: 'blur' },
          { min: 2, max: 50, message: '表名长度应在2-50个字符之间', trigger: 'blur' }
        ],
        dbType: [
          { required: true, message: '请选择数据库类型', trigger: 'change' }
        ]
      },
      sqlResult: '',
      loading: false,
      uploadProgress: 0,
      highlightedSql: ''
    }
  },
  watch: {
    sqlResult: {
      handler(newVal) {
        if (newVal) {
          this.highlightSql();
        }
      },
      immediate: true
    }
  },
  methods: {
    // 复制sql方法
    copySql() {
      const textToCopy = this.sqlResult;
      navigator.clipboard.writeText(textToCopy).then(() => {
        this.$notify({
          title: '成功',
          message: 'SQL已复制到剪贴板',
          type: 'success',
          duration: 2000
        });
      }).catch(() => {
        this.$message.error('复制失败，请重试');
      });
    },

    // 自定义上传方法
    async customUpload(options) {
      const formData = new FormData()
      formData.append('file', options.file)
      formData.append('tableName', this.form.tableName)
      formData.append('dbType', this.form.dbType)

      try {
        const response = await axios.post(this.uploadUrl, formData, {
          timeout: 30000,
          headers: {
            'Content-Type': 'multipart/form-data'
          },
          onUploadProgress: progressEvent => {
            this.uploadProgress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          }
        })

        // 确保在处理完成后更新状态
        this.loading = false

        if (response.data.code === 200) {
          this.sqlResult = response.data.data
          this.$message.success('转换成功')
        } else {
          this.$message.error(response.data.msg || '转换失败')
        }
      } catch (error) {
        this.loading = false
        if (error.code === 'ECONNABORTED') {
          this.$message.error('请求超时，请重试')
        } else {
          this.$message.error(error.response?.data?.message || '上传失败')
        }
      } finally {
        this.uploadProgress = 0
      }
    },

    handleError(error) {
      this.loading = false
      this.$message.error('上传失败，请检查文件格式或重试')
    },

    convertToSql() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          if (!this.$refs.upload.uploadFiles.length) {
            this.$message.warning('请选择Excel文件')
            return
          }

          this.loading = true
          const formData = new FormData()
          const file = this.$refs.upload.uploadFiles[0].raw
          formData.append('file', file)
          formData.append('tableName', this.form.tableName)
          formData.append('dbType', this.form.dbType)

          try {
            const response = await axios.post(this.uploadUrl, formData, {
              headers: {
                'Content-Type': 'multipart/form-data'
              }
            })

            console.log('响应数据:', response) // 添加调试日志

            if (response.data.code === 200) {
              // 确保正确获取SQL结果
              this.sqlResult = response.data.data || response.data.msg
              if (!this.sqlResult) {
                throw new Error('未获取到SQL结果')
              }
              this.$message.success('转换成功')
            } else {
              throw new Error(response.data.msg || '转换失败')
            }
          } catch (error) {
            console.error('转换失败:', error)
            this.$message.error(error.message || '转换失败，请重试')
          } finally {
            this.loading = false
          }
        }
      })
    },

    // 修改handleSuccess方法
    handleSuccess(response) {
      this.loading = false;
      if (response.code === 200) {
        this.sqlResult = response.data || response.msg;
        if (!this.sqlResult) {
          this.$message.warning('未获取到SQL结果');
          return;
        }
        // 手动触发高亮
        this.$nextTick(() => {
          this.highlightSql();
        });
        this.$message.success('转换成功');
      } else {
        this.$message.error(response.msg || '转换失败');
      }
    },

    beforeUpload(file) {
      const isExcel = /\.(xlsx|xls)$/.test(file.name.toLowerCase())
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isExcel) {
        this.$message.error('只能上传 Excel 文件!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('文件大小不能超过 2MB!')
        return false
      }
      
      // 检查表单其他字段
      this.$refs.form.validate(valid => {
        if (!valid) {
          this.$message.warning('请先完善表单信息')
          return false
        }
      })
      
      return true
    },

    handleExceed() {
      this.$message.warning('只能上传一个文件')
    },

    downloadTemplate() {
      window.location.href = process.env.VUE_APP_BASE_API + '/tool/excel2sql/template'
    },

    mounted() {
      this.$nextTick(() => {
        this.highlightSql();
      });
    },

    highlightSql() {
      if (this.sqlResult) {
        try {
          // 使用 highlight.js 处理 SQL
          const highlighted = hljs.highlight(this.sqlResult, {
            language: 'sql',
            ignoreIllegals: true
          }).value;
          this.highlightedSql = highlighted;
        } catch (error) {
          console.error('SQL高亮处理失败:', error);
          this.highlightedSql = this.sqlResult; // 降级处理
        }
      }
    },

    resetForm() {
      this.$refs.form.resetFields()
      this.$refs.upload.clearFiles()
      this.sqlResult = ''
      this.loading = false
    },

    // 添加下载SQL功能
    downloadSql() {
      if (!this.sqlResult) {
        this.$message.warning('没有可下载的SQL内容');
        return;
      }
      
      try {
        const blob = new Blob([this.sqlResult], { type: 'text/plain;charset=utf-8' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        const fileName = `${this.form.tableName}_${this.form.dbType}_${new Date().toISOString().slice(0,10)}.sql`;
        
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        
        this.$message.success('SQL文件下载成功');
      } catch (error) {
        this.$message.error('下载失败，请重试');
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  .el-card {
    margin-bottom: 20px;
  }

  .sql-preview {
    margin-top: 20px;
    background: linear-gradient(135deg, #f0f4f8, #d6e1e9);
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);

    .sql-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 0;
      
      .header-operations {
        display: flex;
        gap: 10px;

        .operation-btn {
          transition: all 0.3s ease;
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
          }
          
          i {
            margin-right: 5px;
          }
        }

        .el-button--primary {
          background-color: #409EFF;
          border-color: #409EFF;
          color: white;
          
          &:hover {
            background-color: #66b1ff;
            border-color: #66b1ff;
          }
        }

        .el-button--success {
          background-color: #67C23A;
          border-color: #67C23A;
          color: white;
          
          &:hover {
            background-color: #85ce61;
            border-color: #85ce61;
          }
        }
      }
    }

    .sql-content {
      position: relative;
      background-color: #282c34;
      padding: 20px;
      border-radius: 8px;
      font-family: 'Fira Code', 'Source Code Pro', monospace;
      line-height: 1.6;
      font-size: 14px;
      overflow-x: auto;
      
      :deep(.hljs) {
        background: transparent;
        color: #abb2bf;
        
        .hljs-keyword {
          color: #c678dd;
        }
        
        .hljs-string {
          color: #98c379;
        }
        
        .hljs-number {
          color: #d19a66;
        }
        
        .hljs-operator {
          color: #56b6c2;
        }
        
        .hljs-comment {
          color: #5c6370;
          font-style: italic;
        }
      }
      
      &::-webkit-scrollbar {
        width: 8px;
        height: 8px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: #4c4c4c;
        border-radius: 4px;
        
        &:hover {
          background: #5a5a5a;
        }
      }
    }
  }

  .el-form {
    max-width: 600px;
  }

  .el-upload__tip {
    color: #909399;
    font-size: 12px;
    margin-top: 5px;
  }

  // SQL预览区域的按钮样式
  .sql-preview {
    .header-operations {
      .el-button {
        &.operation-btn {
          background-color: #409EFF;
          border-color: #409EFF;
          color: white;
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
          }
          
          &.el-button--success {
            background-color: #67C23A;
            border-color: #67C23A;
            
            &:hover {
              background-color: #85ce61;
              border-color: #85ce61;
            }
          }
        }
      }
    }
  }

  // 普通按钮的样式（包括汉堡按钮）
  .el-button {
    transition: all 0.3s ease;
    
    &:hover {
      background-color: #f0f4f8;
      color: #409eff;
    }
  }

  .custom-input {
    .word-count {
      font-size: 12px;
      color: #999;
      margin-right: 4px;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      transition: color 0.3s ease;
      user-select: none;
      
      &--warning {
        color: #E6A23C;
      }
      
      &:hover {
        color: #606266;
      }
    }

    .el-input__suffix {
      right: 5px;
    }

    // 暗色主题适配
    .dark-theme & {
      .word-count {
        color: #666;
        
        &--warning {
          color: #E6A23C;
        }
        
        &:hover {
          color: #8E8EA0;
        }
      }
    }
  }
}


</style>

