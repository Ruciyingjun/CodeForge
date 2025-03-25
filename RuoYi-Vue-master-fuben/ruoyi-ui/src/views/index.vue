<template>
  <div class="app-container home">
    <!-- AI平台介绍部分 -->
    <el-row :gutter="20" class="platform-intro">
      <el-col :span="24">
        <div class="feature-header">
          <h1>CodeForge AI 驱动的代码辅助生成平台</h1>
          <p class="subtitle">让 AI 为您的开发工作提供智能支持</p>
        </div>
      </el-col>
    </el-row>

    <!-- 核心功能展示 -->
    <el-row :gutter="20" class="feature-cards">
      <el-col :sm="12" :md="6">
        <el-card shadow="hover" class="feature-card">
          <div class="icon-wrapper">
            <i class="el-icon-edit"></i>
          </div>
          <h3>一键配置</h3>
          <p>抽取API通用类，轻松修改配置，选择大模型一键完成调用</p>
        </el-card>
      </el-col>
      <el-col :sm="12" :md="6">
        <el-card shadow="hover" class="feature-card">
          <div class="icon-wrapper">
            <i class="el-icon-cpu"></i>
          </div>
          <h3>智能代码生成</h3>
          <p>基于AI的智能代码生成，提供完整的代码解决方案</p>
        </el-card>
      </el-col>
      <el-col :sm="12" :md="6">
        <el-card shadow="hover" class="feature-card">
          <div class="icon-wrapper">
            <i class="el-icon-monitor"></i>
          </div>
          <h3>工具集成</h3>
          <p>无缝集成多种开发工具，提升工作效率和协作能力</p>
        </el-card>
      </el-col>
      <el-col :sm="12" :md="6">
        <el-card shadow="hover" class="feature-card">
          <div class="icon-wrapper">
            <i class="el-icon-help"></i>
          </div>
          <h3>代码质量分析</h3>
          <p>生成深度代码分析报告，检测出潜在问题和安全隐患</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 使用说明部分 -->
    <el-row :gutter="20" class="usage-guide">
      <el-col :span="24">
        <h2 class="section-title">使用说明</h2>
      </el-col>
      <el-col :span="24">
        <el-card shadow="hover" class="guide-card">
          <div class="platform-description">
            <h3 class="fixed-title">配置api（后端AiConfig.java，前端constants/ai.js）-> 测试连接，成功后即可使用大模型能力</h3>
            <p class="description-text">
              <span class="char-by-char">CodeForge 是？一个集成了 AI 能力的代码辅助生成平台，一个优化原生代码生成器生成模糊问题且具备二次开发能力的 RuoYi 框架，或是好用工具的集合。</span>
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 技术栈展示 -->
    <el-row :gutter="20" class="tech-stack">
      <el-col :span="24">
        <h2 class="section-title">技术生态</h2>
      </el-col>
      <el-col :sm="24" :md="8">
        <el-card shadow="hover" class="tech-card">
          <h3><i class="el-icon-s-platform"></i> AI能力</h3>
          <ul>
            <li><el-link type="primary" href="https://www.tensorflow.org/" target="_blank">自然语言处理</el-link></li>
            <li><el-link type="primary" href="https://pytorch.org/" target="_blank">深度学习模型</el-link></li>
            <li><el-link type="primary" href="https://scikit-learn.org/" target="_blank">机器学习算法</el-link></li>
            <li><el-link type="primary" href="https://openai.com/" target="_blank">智能代码分析</el-link></li>
          </ul>
        </el-card>
      </el-col>
      <el-col :sm="24" :md="8">
        <el-card shadow="hover" class="tech-card">
          <h3><i class="el-icon-s-management"></i> 后端技术</h3>
          <ul>
            <li><el-link type="primary" href="https://spring.io/projects/spring-boot" target="_blank">SpringBoot</el-link></li>
            <li><el-link type="primary" href="https://spring.io/projects/spring-security" target="_blank">Spring Security</el-link></li>
            <li><el-link type="primary" href="https://mybatis.org/mybatis-3/" target="_blank">MyBatis</el-link></li>
            <li><el-link type="primary" href="https://redis.io/" target="_blank">Redis</el-link></li>
          </ul>
        </el-card>
      </el-col>
      <el-col :sm="24" :md="8">
        <el-card shadow="hover" class="tech-card">
          <h3><i class="el-icon-s-data"></i> 前端技术</h3>
          <ul>
            <li><el-link type="primary" href="https://vuejs.org/" target="_blank">Vue</el-link></li>
            <li><el-link type="primary" href="https://element.eleme.io/" target="_blank">Element UI</el-link></li>
            <li><el-link type="primary" href="https://axios-http.com/" target="_blank">Axios</el-link></li>
            <li><el-link type="primary" href="https://webpack.js.org/" target="_blank">Webpack</el-link></li>
          </ul>
        </el-card>
      </el-col>
    </el-row>

    <!-- 联系方式部分 -->
    <div class="contact-section">
      <h2>联系方式</h2>
      <div class="contact-list">
        <div class="contact-item">
          <i class="el-icon-message"></i>
          <span class="label">邮箱：</span>
          <span class="value">19949317733@163.com</span>
        </div>
        <div class="contact-item">
          <i class="el-icon-chat-line-round"></i>
          <span class="label">微信：</span>
          <span class="value">G2876580784</span>
        </div>
        <div class="contact-item">
          <i class="el-icon-notebook-2"></i>
          <span class="label">博客：</span>
          <span class="value">
            <a href="https://blog.csdn.net/m0_73657660?spm=1000.2115.3001.5343" target="_blank">技术博客</a>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.home {
  padding: 20px;

  .platform-intro {
    text-align: center;
    margin-bottom: 40px;

    .feature-header {
      h1 {
        font-size: 2.5em;
        color: #3a8ee6;
        margin-bottom: 15px;
        font-weight: 500;
      }

      .subtitle {
        font-size: 1.2em;
        color: #bdbdbd;
      }
    }
  }

  .feature-cards {
    margin-bottom: 30px;

    .feature-card {
      height: 200px;
      text-align: center;
      transition: all 0.3s;
      background: rgba(28, 28, 28, 0.6) !important;
      border: 1px solid #2c2c2c !important;
      display: flex !important;
      flex-direction: column !important;
      justify-content: center !important;
      padding: 20px !important;

      &:hover {
        transform: translateY(-5px);
        background: rgba(32, 32, 32, 0.8) !important;
        border-color: #3a8ee6 !important;
        box-shadow: 0 4px 12px rgba(58, 142, 230, 0.1) !important;
      }

      .icon-wrapper {
        margin-bottom: 16px !important;
        i {
          font-size: 36px !important;
          color: #3a8ee6;
        }
      }

      h3 {
        margin: 0 0 12px 0 !important;
        font-size: 18px;
        color: #e0e0e0;
        font-weight: 500;
      }

      p {
        color: #808080;
        font-size: 14px;
        line-height: 1.6;
        margin: 0 !important;
        padding: 0 24px !important;
      }
    }
  }

  .tech-stack {
    .section-title {
      text-align: center;
      margin-bottom: 20px;
      color: #e0e0e0;
      font-weight: 500;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: -8px;
        left: 50%;
        transform: translateX(-50%);
        width: 40px;
        height: 3px;
        background: linear-gradient(90deg, #409EFF, #66b1ff);
        border-radius: 2px;
      }
    }

    .tech-card {
      margin-bottom: 20px;
      background: rgba(28, 28, 28, 0.6) !important;
      border: 1px solid #2c2c2c !important;
      padding: 20px !important;

      &:hover {
        background: rgba(32, 32, 32, 0.8) !important;
        border-color: #3a8ee6 !important;
      }

      h3 {
        border-bottom: 1px solid #2c2c2c;
        padding-bottom: 8px !important;
        margin-bottom: 12px !important;
        color: #e0e0e0;
        font-weight: 500;
        display: flex !important;
        align-items: center !important;

        i {
          color: #3a8ee6;
          margin-right: 8px !important;
          font-size: 18px !important;
        }
      }

      ul {
        padding: 0 8px !important;

        li {
          line-height: 2.2 !important;
          color: #808080;
          display: flex !important;
          align-items: center !important;

          &:before {
            content: "•";
            color: #3a8ee6;
            margin-right: 8px;
          }

          .el-link {
            color: #3a8ee6 !important;
            font-weight: normal !important;

            &:hover {
              color: #66b1ff !important;
            }
          }
        }
      }
    }
  }

  .usage-guide {
    margin-bottom: 30px;

    .section-title {
      text-align: center;
      margin-bottom: 20px;
      color: #e0e0e0;
      font-weight: 500;
      position: relative;

      &::after {
        content: '';
        position: absolute;
        bottom: -8px;
        left: 50%;
        transform: translateX(-50%);
        width: 40px;
        height: 3px;
        background: linear-gradient(90deg, #409EFF, #66b1ff);
        border-radius: 2px;
      }
    }

    .guide-card {
      background: rgba(28, 28, 28, 0.6) !important;
      border: 1px solid #2c2c2c !important;

      .platform-description {
        padding: 24px 32px;
        text-align: center;

        .description-text {
          color: #e0e0e0;
          font-size: 16px;
          line-height: 1.8;
          margin: 0;
          letter-spacing: 0.5px;
          text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);

          .char-by-char {
            display: inline-block;
            animation: revealChars 4s steps(80) forwards;
            white-space: pre-wrap;
            clip-path: polygon(0 0, 0 0, 0 100%, 0 100%);
          }
        }
      }
    }
  }

  .contact-section {
    margin-bottom: 50px;
    padding: 30px !important;
    background: rgba(28, 28, 28, 0.6) !important;
    border-radius: 12px !important;
    border: 1px solid #2c2c2c !important;
    transition: all 0.3s ease !important;

    h2 {
      color: #e0e0e0 !important;
      font-size: 24px !important;
      margin-bottom: 24px !important;
      padding-bottom: 16px !important;
      border-bottom: 1px solid #2c2c2c !important;
      display: flex !important;
      align-items: center !important;

      &::before {
        content: "" !important;
        width: 4px !important;
        height: 20px !important;
        background: #409EFF !important;
        margin-right: 12px !important;
        border-radius: 2px !important;
      }
    }

    .contact-list {
      display: flex !important;
      flex-direction: column !important;
      gap: 16px !important;

      .contact-item {
        display: flex !important;
        align-items: center !important;
        gap: 12px !important;
        padding: 16px !important;
        border-radius: 8px !important;
        background: rgba(64, 158, 255, 0.05) !important;
        border: 1px solid rgba(64, 158, 255, 0.1) !important;
        transition: all 0.3s ease !important;

        i {
          font-size: 20px !important;
          color: #409EFF !important;
          width: 40px !important;
          height: 40px !important;
          display: flex !important;
          align-items: center !important;
          justify-content: center !important;
          background: rgba(64, 158, 255, 0.1) !important;
          border-radius: 8px !important;
          transition: all 0.3s ease !important;
        }

        .label {
          color: #8E8EA0 !important;
          font-size: 14px !important;
          min-width: 60px !important;
          font-weight: 500 !important;
        }

        .value {
          color: #E0E0E0 !important;
          font-size: 14px !important;

          a {
            color: #409EFF !important;
            text-decoration: none !important;
            transition: all 0.3s ease !important;
            padding: 4px 12px !important;
            background: rgba(64, 158, 255, 0.1) !important;
            border-radius: 4px !important;

            &:hover {
              color: #ffffff !important;
              background: #409EFF !important;
              text-decoration: none !important;
            }
          }
        }

        &:hover {
          background: rgba(64, 158, 255, 0.1) !important;
          transform: translateX(5px) !important;
          border-color: rgba(64, 158, 255, 0.3) !important;

          i {
            background: #409EFF !important;
            color: #ffffff !important;
            transform: scale(1.1) !important;
          }
        }
      }
    }
  }
}

@keyframes revealChars {
  to {
    clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%);
  }
}
</style>
