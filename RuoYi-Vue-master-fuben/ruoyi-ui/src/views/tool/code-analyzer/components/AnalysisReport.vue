<template>
  <div class="analysis-report">
    <div class="report-section">
      <div class="report-score">
        <div class="score-ring">
          <el-progress 
            type="circle" 
            :percentage="report.score"
            :stroke-width="8"
            :width="120"
            :color="[
              {color: '#F56C6C', percentage: 60},
              {color: '#E6A23C', percentage: 80},
              {color: '#67C23A', percentage: 100}
            ]">
            <span class="score-number">{{ report.score }}</span>
          </el-progress>
        </div>
        <div class="score-metrics">
          <div class="metric-item" v-for="(metric, key) in report.metrics" :key="key">
            <span class="metric-name">{{ getMetricName(key) }}</span>
            <span class="metric-score" :class="getScoreClass(metric.score)">{{ metric.score }}分</span>
          </div>
        </div>
      </div>
    </div>

    <div class="report-details">
      <div class="detail-item" v-for="(metric, key) in report.metrics" :key="key">
        <div class="detail-header">
          <i :class="getMetricIcon(key)"></i>
          <span>{{ getMetricName(key) }}</span>
          <span class="detail-score" :class="getScoreClass(metric.score)">{{ metric.score }}分</span>
        </div>
        <div class="detail-content">
          <template v-if="metric.issues && metric.issues.length">
            <div v-for="(issue, index) in metric.issues" 
                 :key="index"
                 class="issue-box"
                 :class="issue.severity">
              <div class="issue-title">
                <i :class="getSeverityIcon(issue.severity)"></i>
                {{ issue.message }}
              </div>
              <pre v-if="issue.code" class="code-block"><code>{{ issue.code }}</code></pre>
              <div class="issue-suggestion">
                <i class="el-icon-info"></i>
                {{ issue.suggestion }}
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AnalysisReport',
  props: {
    report: {
      type: Object,
      required: true
    }
  },
  methods: {
    getMetricName(key) {
      const names = {
        complexity: '复杂度分析',
        documentation: '文档完整性',
        naming: '命名规范',
        bestPractices: '最佳实践'
      }
      return names[key] || key
    },
    getMetricIcon(key) {
      const icons = {
        complexity: 'el-icon-share',
        documentation: 'el-icon-document',
        naming: 'el-icon-edit',
        bestPractices: 'el-icon-s-check'
      }
      return icons[key] || 'el-icon-info'
    },
    getScoreClass(score) {
      if (score >= 90) return 'score-excellent'
      if (score >= 80) return 'score-good'
      if (score >= 60) return 'score-fair'
      return 'score-poor'
    },
    getSeverityIcon(severity) {
      return {
        error: 'el-icon-error',
        warning: 'el-icon-warning',
        info: 'el-icon-info'
      }[severity] || 'el-icon-info'
    }
  }
}
</script>

<style lang="scss" scoped>
.analysis-report {
  color: #d4d4d4;

  .report-section {
    margin-bottom: 24px;
  }

  .report-score {
    display: flex;
    align-items: center;
    gap: 40px;
    padding: 24px;
    background: #2d2d2d;
    border-radius: 6px;

    .score-ring {
      .score-number {
        font-size: 32px;
        font-weight: 600;
        color: #fff;
      }
    }

    .score-metrics {
      flex: 1;
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 16px;

      .metric-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 12px;
        background: #363636;
        border-radius: 4px;

        .metric-name {
          color: #909399;
        }

        .metric-score {
          font-weight: 500;
        }
      }
    }
  }

  .report-details {
    .detail-item {
      margin-bottom: 20px;

      .detail-header {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px 16px;
        background: #2d2d2d;
        border-radius: 4px;
        color: #fff;

        i {
          color: #409EFF;
        }

        .detail-score {
          margin-left: auto;
          padding: 2px 8px;
          border-radius: 3px;
          font-size: 13px;
        }
      }

      .detail-content {
        padding: 16px;
      }
    }
  }

  .issue-box {
    margin-bottom: 16px;
    border-radius: 4px;
    background: #363636;
    overflow: hidden;

    &.error { border-left: 3px solid #F56C6C; }
    &.warning { border-left: 3px solid #E6A23C; }
    &.info { border-left: 3px solid #409EFF; }

    .issue-title {
      padding: 12px 16px;
      background: #2d2d2d;
      color: #fff;
      display: flex;
      align-items: center;
      gap: 8px;

      i {
        &.el-icon-error { color: #F56C6C; }
        &.el-icon-warning { color: #E6A23C; }
        &.el-icon-info { color: #409EFF; }
      }
    }

    .code-block {
      margin: 12px 16px;
      padding: 12px;
      background: #1e1e1e;
      border-radius: 4px;
      font-family: 'Fira Code', monospace;
      overflow-x: auto;
    }

    .issue-suggestion {
      padding: 12px 16px;
      color: #909399;
      display: flex;
      align-items: flex-start;
      gap: 8px;
      font-size: 13px;
      
      i {
        margin-top: 3px;
      }
    }
  }

  .score-excellent { color: #67C23A; }
  .score-good { color: #409EFF; }
  .score-fair { color: #E6A23C; }
  .score-poor { color: #F56C6C; }
}
</style> 