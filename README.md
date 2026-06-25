<div align="center">

# ✨ mica-admin

### 一套代码，三个端，开箱即用的低代码权限管理平台

**后端 · Web 管理端 · 移动 App**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vue.js&logoColor=white)](https://vuejs.org)
[![uniapp](https://img.shields.io/badge/uniapp-x-2B9939?logo=wechat&logoColor=white)](https://uniapp.dcloud.net.cn)
[![Mica](https://img.shields.io/badge/Mica-2.7.18.7-blue)](https://gitee.com/596392912/mica)
[![License](https://img.shields.io/badge/license-Apache%202.0-green)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen)](https://github.com)

[在线演示](https://admin.dreamlu.net) · [更新日志](#-更新日志)

</div>

---

## 🎯 这是什么？

**mica-admin** 是一个面向中小团队的**全栈低代码权限管理平台**,把企业日常需要的**用户/角色/菜单/部门/字典/文件/监控/通知**全部集成在一起,定位为通用后台系统。

- 🏢 **管理后台** —— 用户、角色、菜单、部门、字典、文件、监控、操作日志一应俱全
- 📱 **移动 App** —— uniapp x 跨端,iOS / Android / H5 / 微信小程序一套代码
- 🚀 **一键部署** —— `deploy.sh` 跑完,jar 包就到生产了

> 自 v1.0 起,mica-admin **不再内置即时通讯(IM)**。
> 系统定位回归通用后台,如需 IM 走第三方 SDK 或二次开发扩展。

---

[✨✨✨推广：**BladeX 物联网平台**✨✨✨iot.bladex.cn](https://iot.bladex.cn?from=mica-mqtt)

---

## 🖼️ 在线演示

👉 **<https://admin.dreamlu.net>**

| 账号 | 密码 | 角色 |
| :--: | :--: | :--: |
| `admin` | `test` | 超级管理员 |
| `test` | `test` | 普通用户 |

---

## 🏗️ 项目结构

```
mica-admin/                                  # mono-repo (单 git 仓库)
├── mica-admin-server/                       # Spring Boot 后端 (Java 8+)
│   └── net.dreamlu.mica.admin
│       ├── common/      公共常量 (ApiCode 等)
│       ├── framework/   框架核心 (Security / MyBatis / AOP / JWT)
│       └── project/     业务模块 (用户 / 角色 / 菜单 / 部门 / 字典 ...)
│
├── mica-admin-web/                          # Web 管理端 (Vben Admin 5.x 本地化)
│   ├── src/             业务代码
│   └── vben/            本地化的 Vben 框架源码
│
├── mica-admin-uniapp/                       # 移动 App (uniapp x)
│   └── src
│       ├── modules/auth        登录 (通用)
│       └── modules/extension   二次开发只新增
│
├── docs/                  设计文档 (App / 数据库)
├── deploy.sh              一键部署脚本
├── script/                systemd 服务脚本
└── AGENTS.md              AI 编码助手规则
```

> **三端同仓**是为了让 AI 编码时"看见完整上下文",便于跨模块协作。
> `mica-admin-uniapp/` **不进 Maven**,是独立 npm 工程。

---

## 🚀 技术栈

<table>
  <tr>
    <th align="center" width="180">端</th>
    <th align="center">技术栈</th>
  </tr>
  <tr>
    <td align="center"><b>后端</b></td>
    <td>
      Spring Boot 2.7 · Spring Security · MyBatis-Plus 3.5 · Druid · Redis · JWT · RSA
      <br/>Undertow · Log4j2 · mica-captcha · mica-openapi · dromara x-file-storage
    </td>
  </tr>
  <tr>
    <td align="center"><b>Web</b></td>
    <td>
      Vue 3.5 + TypeScript 5.7 + Vite 6 + Pinia 3 + Naive UI 2.44 + Tailwind CSS 4
      <br/>Vben Admin 5.x (本地化到 <code>vben/</code>) · Vue I18n · ECharts · VxeTable
    </td>
  </tr>
  <tr>
    <td align="center"><b>App</b></td>
    <td>
      uniapp x · Vue 3.4 + TypeScript 5 + Pinia 2 + Vite 5
      <br/>uni-ui · dayjs · pinia-plugin-persistedstate
    </td>
  </tr>
  <tr>
    <td align="center"><b>存储</b></td>
    <td>MySQL 5.7+ · Redis 6+ · 本地 / OSS 文件存储 (可插拔)</td>
  </tr>
</table>

---

## ⭐ 核心亮点

### 1. 三端一套代码,权限完全打通

- Web 端 / App 端 / 后端**共用 RBAC 模型** (`sys_user` / `sys_role` / `sys_menu`)
- 后台给用户配什么菜单/按钮,App 端就显示什么
- JWT 鉴权贯通三端

### 2. 🛠️ 代码生成器

跑一次 `main()` 就生成完整的 Controller / Service / Mapper / Vue 页面:

```java
// mica-admin-server/src/test/java/.../generator/MysqlAutoGenerator.java
strategyConfig.addInclude("sys_user", "sys_role");
```

### 3. 🚀 一键部署

```bash
./deploy.sh
# 自动: mvn package → scp 到远端 host=tx → 远端 restartd → 清理本地包
```

服务用 **systemd** 托管(`script/start.sh`),开箱即用。

### 4. 🔌 二次开发友好

**通用模块不修改,二次开发只新增**。App 端把扩展点统一放在 `modules/extension/`,
后端按业务新建包即可,完全不碰 `framework/`。

---

## 📦 快速开始

### 0. 前置依赖

| 依赖 | 版本 | 备注 |
| :-- | :--: | :-- |
| JDK | 8+ | 后端编译/运行 |
| Maven | 3.6+ | 后端构建 |
| Node.js | 18+ | 前端 + App |
| pnpm | 9+ | 前端 + App |
| MySQL | 5.7+ | 导入 `docs/database/mysql.sql` |
| Redis | 6+ | localhost:6379 |

### 1. 初始化数据库

```bash
mysql -u root -p < docs/database/mysql.sql
```

### 2. 启动后端

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# 监听 8080,API 文档: http://localhost:8080/doc.html
```

### 3. 启动 Web 端

```bash
cd mica-admin-web
pnpm install
pnpm dev          # http://localhost:5888
```

### 4. 启动 App 端

```bash
cd mica-admin-uniapp
pnpm install
pnpm dev:h5       # http://localhost:5889
# 或: pnpm dev:app / pnpm dev:mp-weixin
```

### 5. 生产构建

```bash
# 后端 + 前端 一起打成 fat jar
mvn clean package -Pprod -U -Dmaven.test.skip=true
```

---

## 📚 文档导航

| 文档 | 适合谁 | 内容 |
| :-- | :-- | :-- |
| [docs/database/mysql.sql](docs/database/mysql.sql) | 运维 | 数据库结构 |
| [mica-admin-uniapp/AGENTS.md](mica-admin-uniapp/AGENTS.md) | App 二次开发 | 通用模块不修改原则 |
| [AGENTS.md](AGENTS.md) | AI 编码助手 | 仓库级编码规范 |

---

## 🤝 贡献

PR 永远欢迎 🙏

1. Fork → 2. Feature Branch → 3. Commit → 4. PR
2. 后端改动请补单测 (`mvn test -Dtest=ClassName#methodName`)
3. 前端改动请跑 `pnpm typecheck` + `pnpm lint`

## 📜 开源协议

[Apache 2.0](LICENSE)

## 🌟 致谢

- [mica](https://gitee.com/596392912/mica) —— 让 Java 开发更高效的瑞士军刀
- [vue-vben-admin](https://github.com/vbenjs/vue-vben-admin) —— Vue 3 中后台模板的天花板
- [uniapp](https://uniapp.dcloud.net.cn) —— 一套代码,8 端运行

---

<div align="center">

**如果这个项目对你有帮助,给个 ⭐ 鼓励一下!**

<sub>Built with ❤️ by dreamlu & contributors</sub>

</div>