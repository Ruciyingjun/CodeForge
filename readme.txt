CodeForge代码辅助开发平台

test:
	redis-cli shutdown
	代码生成用：请帮我生成一段代码来计算1+1
	模型连接用：管理目录下的AI功能测试

开发流程：
	1，下载若以框架跑通
	2，将后端管理平台直接作为前端服务平台（替换了logo，配色，登录页，名字等确定准备开发的功能，生成对应的菜单）
	3，通过aliyun拿到api，在项目中接入大模型，编写对应的层。包括：
		ai对话功能的实体类，包含请求和响应。ruoyi-system\src\main\java\com\ruoyi\system\domain\AiChat.java
		ai对话功能的服务接口，定义了对话方式。ruoyi-system\src\main\java\com\ruoyi\system\service\IAiChatService.java
2.18日-------------------------------------------------------------------------------------------------------
		ai对话功能的服务实现类，实现与api的连接。ruoyi-system\src\main\java\com\ruoyi\system\service\impl\AiChatServiceImpl.java
		ai对话功能的控制器，处理前端请求并调用服务。ruoyi-admin\src\main\java\com\ruoyi\web\controller\system\AiChatController.java
	4，开始第一个代码生成功能的实现，在ruoyi-ui中创建简单的对话界面在route.js里绑定路由。ruoyi-ui\src\views\codegen\CodeGeneration.vue。
	  4-1，实现大模型后端逻辑与前端的接入时候遇到报错“AiCodeService.java中java: 类 CodeGenerationResult 是公共的, 应在名为 CodeGenerationResult.java 的文件中声明”，于是创建单独的CodeGenerationResult.java文件器，并从从AiCodeService.java中移除。来解决该问题。
	  4-2，显示接入成功没有报错也没有异常，尝试发现问题，怀疑大模型没有接通，尝试写个测试类。测试成功。
2.19日--------------------------------------------------------------------------------------------------------
	  4-3，补充完AiController中的生成逻辑，并且提取json中生成的代码，返回给前端（毕竟不能让用户看到全部的生成内容，那还不如直接用大模型），并且展示代码正确缩进以美观
	  4-4，让大模型修改代码生成的前端界面，调试。
	5，把关于大模型的调用方式抽取出来，放到ruoyi-system\src\main\java\com\ruoyi\system\ai\config\AiConfig.java，保证测试和实际调用模型和令牌都一致。
	6，开始实现excel一键转化为sql功能
	  6-1，创建后端逻辑ruoyi-common\src\main\java\com\ruoyi\common\utils\ExcelToSqlUtil.java，生成对应的前端代码ruoyi-ui\src\views\tool\excel2sql\index.vue，实现controller，ruoyi-admin\src\main\java\com\ruoyi\web\controller\tool\Excel2SqlController.java。
2.20日--------------------------------------------------------------------------------------------------------
	  6-2，完成ExcelToSql的基础功能，发现若以要求配置后端权限，选择加上配置注解。失败了，直接使用注解配置匿名访问。
	  6-2，不断调前端，终于通了，一直显示转换中，打印处理进度到输出，发现不是后端的问题，而是前端处理逻辑有问题，下一步优化代码生成和sql转换的生成界面。
2.21日--------------------------------------------------------------------------------------------------------
2.22日--------------------------------------------------------------------------------------------------------
待办：单词，湖工匠，完善项目，学一下easyExcel。
2.23日--------------------------------------------------------------------------------------------------------
	  6-3解决lexcel转sql下载模板报401的bug（需要在后端方法中添加权限配置），解决了通过不正常逻辑创建的菜单无法正确通过ruoyi的菜单管理进行管理问题，整理菜单。
	7， 开始实现代码质量分析功能
	  7-1，定义接口src/main/java/com/ruoyi/system/service/ICodeAnalyzerService.java，接口实现src/main/java/com/ruoyi/system/service/impl/CodeAnalyzerServiceImpl.java，控制器src/main/java/com/ruoyi/web/controller/tool/CodeAnalyzerController.java，
	  7-2，请求数据和分析报告src/main/java/com/ruoyi/system/domain/CodeAnalysisReport.java，src/main/java/com/ruoyi/system/domain/CodeAnalyzeRequest.java
	  7-3，大模型生成前端，调试ruoyi-ui/src/views/tool/codeanalyzer/index.vue，配置路由，数据库中生成菜单。
	  7-4，完善分析逻辑。该部分的目录结构：
ruoyi-system/src/main/java/com/ruoyi/system/
├── analyzer/
│   ├── CodeAnalyzer.java
│   ├── impl/
│   │   ├── JavaCodeAnalyzer.java
│   │   ├── PythonCodeAnalyzer.java
│   │   └── JavaScriptCodeAnalyzer.java
│   └── utils/
│       └── CodeAnalyzerUtils.java
├── domain/
│   ├── CodeAnalyzeRequest.java
│   └── CodeAnalysisReport.java
└── service/
    ├── ICodeAnalyzerService.java
    └── impl/
        └── CodeAnalyzerServiceImpl.java
	  7-5，解决菜单重复的问题，总结下ruoyi路由的管理方法，还需要①测试下不同语言下代码分析情况，②总结ruoyi路由管理方法，③优化前端，④解决大模型响应问题，⑤实现最后一个ai平台助手功能。
2.24日--------------------------------------------------------------------------------------------------------
	  7-6，①完成，②完成，代码质量分析基本完成，但实际效果不怎么样，后续考虑让ai介入。
	  7-7，优化前端，基本完成。
2.25日--------------------------------------------------------------------------------------------------------
待办：①csdn演示文档，②优化代码质量分析前端界面，③考虑是否继续做多轮对话。


	
		



	
	
	
主要开发功能
