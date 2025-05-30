本项目是[AssistsX](https://www.pgyer.com/SqGaCx8C)自动化插件平台的示例项目，使用vite开发，通过调用[AssistsX](https://www.pgyer.com/SqGaCx8C) API实现在Android平台的自动化脚本

# 示例安装运行

## 本地安装运行

### 1. 安装[AssistsX](https://www.pgyer.com/SqGaCx8C)

安装前请先在手机安装[AssistsX](https://www.pgyer.com/SqGaCx8C)

### 2. 插件包传至手机

下载本示例源码后，本地运行命令`npm run build`编译项目，编译完成后将`dist`文件夹压缩为`.zip`文件，然后将`.zip`文件传至手机。手机打开[AssistsX](https://www.pgyer.com/SqGaCx8C)，点击立即安装，选择本地添加，然后选择传至手机的插件包即可完成安装

> 或者直接将编译打包好的`assistsx-simple.zip`传至手机也可以


<img src="https://github.com/user-attachments/assets/7dc27910-be61-473b-8900-f09c16ca5f46" width="250">

## 局域网加载运行

### 1. 安装[AssistsX](https://www.pgyer.com/SqGaCx8C)

加载运行前请先在手机安装[AssistsX](https://www.pgyer.com/SqGaCx8C)

### 2. 启动项目

下载本示例源码后，本地运行命令`npm run dev`启动项目

### 3. 加载插件

打开[AssistsX](https://www.pgyer.com/SqGaCx8C)，点击立即安装，选择**扫描局域网**，点击对应扫描到插件的 **+** 号即可完成插件添加

<img src="https://github.com/user-attachments/assets/d0f24763-266e-4e3c-bd64-a63be9e6c68c" width="250"/>

# 快速开始
## 1. 创建项目
- 创建vite模版项目`npm create vite@latest assistsx-helloword -- --template vue`
- 安装assistsx依赖`npm install assistsx@latest`
## 2. 创建插件配置
在目录`public`下创建文件`assistsx_plugin_config.json`文件，将以下`json`复制粘贴到文件中
```
{
"name": "AssistsX示例",
"version": "1.0.0",
"description": "AssistsX示例",
"isShowOverlay": true,
"needScreenCapture": true,
"packageName": "com.assistsx.example",
"main": "index.html",
"icon": "vite.svg",
"overlayTitle": "AssistsX示例"
}
```
## 3. 编写脚本插件
写一个最简单的，点击微信搜索进入搜索页面
```agsl
const handleClick = () => {
  AssistsX.findById("com.tencent.mm:id/jha")[0].click()
}
```

增加一个测试按钮调用这个方法
```agsl
<button type="button" @click="handleClick">测试按钮</button>
```

## 4. 加载插件
1. 我们使用局域网加载插件，加载插件前需要配置项目允许局域网访问，在文件`vite.config.js`添加以下配置
```
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0', // 允许局域网访问
    port: 5173
  },
})
```
运行项目`npm run dev`以便AssistsX直接加载

2. 打开AssistsX，扫描局域网插件添加

<img src="https://github.com/user-attachments/assets/d0f24763-266e-4e3c-bd64-a63be9e6c68c" width="250"/>

3. 测试插件：点击开始，打开微信消息列表，点击测试按钮

<img src="https://github.com/user-attachments/assets/e6e59149-ed78-42de-81a7-c3476b5472e6" width="250"/>
