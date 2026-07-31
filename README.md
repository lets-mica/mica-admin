<div align="center">

# ✨ mica-admin

### 一套代码，三个端，开箱即用的模块化单体权限管理平台

**后端 · Web 管理端 · 移动 App**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vue.js&logoColor=white)](https://vuejs.org)
[![uniapp](https://img.shields.io/badge/uniapp-x-2B9939?logo=wechat&logoColor=white)](https://uniapp.dcloud.net.cn)
[![Mica](https://img.shields.io/badge/Mica-4.1.0-blue)](https://gitee.com/596392912/mica)
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

---

## 🌿 分支说明

项目针对不同 Java 与 Spring Boot 版本提供两条长期维护分支，请根据运行环境选择：

| 分支 | Java 版本 |   Spring Boot 版本   | 说明 |
| :-- | :--: |:------------------:| :-- |
| [`main`](../../tree/main) | Java 17 |  Spring Boot 4.x   | 主分支，推荐新项目使用 |
| [`java8`](../../tree/java8) | Java 8 | Spring Boot 2.7.18 | 兼容分支，适合仍需运行在 Java 8 环境的项目 |

```bash
# 推荐：Java 17 + Spring Boot 4.x
git switch main

# 兼容：Java 8 + Spring Boot 2.7.x
git switch java8
```

> 两个分支的构建与启动方式一致；开始开发前，请确认本地 JDK 版本与所选分支匹配。

---

[✨✨✨推广：**BladeX 物联网平台**✨✨✨iot.bladex.cn](https://iot.bladex.cn?from=mica-mqtt)

---

## 🖼️ 在线演示

👉 **<https://admin.dreamlu.net>**

|   账号    |    密码    | 角色 |
|:-------:|:--------:| :--: |
| `admin` | `123456` | 超级管理员 |
| `mica`  | `123456` | 普通用户 |

---

## 🏗️ 项目结构

```
mica-admin/                                  # mono-repo (单 git 仓库)
├── mica-admin-server/                       # Spring Boot 后端 (Java 版本见分支说明)
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
├── .claude/skills/                          # AI 编码 Skills (三端各一份)
│   ├── mica-admin-backend/    后端 CRUD 模块套路
│   ├── mica-admin-web/        Web 端页面套路
│   └── mica-admin-uniapp/     App 端页面套路
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
      Spring Boot 4.x（main）/ 2.7.x（java8）· Spring Security · MyBatis-Plus · Druid · Redis · JWT · RSA
      <br/>Tomcat · Log4j2 · mica-captcha · mica-openapi · dromara x-file-storage
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
      uniapp x · Vue 3.5 + TypeScript 5.4 + Pinia 2 + Vite 6
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

### 5. 🤖 内置 AI 编码 Skills

`.claude/skills/` 下沉淀了**三端各一份**的编码套路,AI 会按任务自动加载对应 skill,
写出来的代码直接符合本项目约定(分层、权限码、分页、菜单挂载),不用每次重复交代。
详见 [AI 辅助开发 Skills](#-ai-辅助开发-skills)。

---

## 📦 快速开始

### 0. 前置依赖

| 依赖 | 版本 | 备注 |
| :-- | :--: | :-- |
| JDK | 17 / 8 | `main` 使用 Java 17；`java8` 使用 Java 8 |
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

## 🤖 AI 辅助开发 Skills

项目把三端的开发套路沉淀成了 [Claude Code Skills](https://docs.claude.com/en/docs/claude-code/skills),
放在 `.claude/skills/`,**随仓库一起提交**,团队共享。

### 有哪些

| Skill | 覆盖范围 | 什么时候会自动触发 |
| :-- | :-- | :-- |
| `mica-admin-backend` | `mica-admin-server/` | 加/改后端接口、实体、Service、权限码、菜单 SQL |
| `mica-admin-web` | `mica-admin-web/` | 加/改 Web 页面、API 层、路由、权限按钮 |
| `mica-admin-uniapp` | `mica-admin-uniapp/` | 加/改 App 页面、模块、`pages.json`、工作台卡片 |

每个 skill 都是「**约定 + 模板**」两层:

```
mica-admin-backend/
├── SKILL.md                    # 硬性约定、禁区、checklist、命令
└── references/crud-module.md   # 可直接复制的完整代码模板
```

### 怎么用

**不需要手动指定**。Skill 的 `description` 里写明了适用场景,AI 会根据你的任务自动加载:

```
> 帮我加一个轮播图管理功能，后端 + Web 端都要
```

AI 会自动读取 `mica-admin-backend` + `mica-admin-web` 两个 skill,然后按项目既有套路产出:

- 后端 6 个文件(entity / query / mapper / service / impl / controller)+ `sys_menu` 菜单与按钮权限 SQL
- Web 端 `src/api/system/banner.ts` + `src/views/system/banner/index.vue`
- 权限码统一 `system:banner:list|query|add|edit|del|export`

也可以显式点名,让它先讲思路:

```
> 用 mica-admin-uniapp skill 说明下 App 端新增一个模块要改哪些文件
```

### Skill 里固化了哪些"坑"

这些都是从现有代码里逆推出来的、AI 不看约定就一定会写错的地方:

- 后端**成功响应 `code = 0`**(不是 200),写操作返回 `void`,不要手工包 `R.success()`
- Web 端分页要用 `parsePage()` 把 `IPage` 的 `records` 归一成 `list`;App 端**直接消费 `records`**(没有 `parsePage`)
- Web 端别名是 `#/`,App 端是 `@/`
- 表格多选用 `:checked-row-keys`,**不是** `:selection`
- **前端不写静态路由**,新页面靠后端 `sys_menu` 下发
- 全局拦截器已弹错误提示,业务 `catch` 里不要重复 toast
- 不改 `vben/` 内的导入路径、不在 `vben/` 内用 `@apply`、不改 `src/api/core/auth.ts`

### 扩展自己的 Skill

新建 `.claude/skills/<你的skill名>/SKILL.md`,frontmatter 写清 `name` 和 `description`
(description 要说明**什么时候该用**,这决定了能否被自动召回):

```markdown
---
name: my-module
description: Use when ... 覆盖 xxx 场景
---

# 标题
约定、禁区、checklist...
```

内容多的话拆到 `references/` 下,`SKILL.md` 里链接过去,避免一次性灌太多上下文。

### 用别的 AI 工具？

Skill 是 Claude Code 的机制。其他工具请读这两份等价规则:

- [AGENTS.md](AGENTS.md) —— 仓库级编码规范(Codex / Cursor 等通用格式)
- [CLAUDE.md](CLAUDE.md) —— 架构说明与全局约定

---

## 📚 文档导航

| 文档 | 适合谁 | 内容 |
| :-- | :-- | :-- |
| [docs/database/mysql.sql](docs/database/mysql.sql) | 运维 | 数据库结构 |
| [mica-admin-uniapp/AGENTS.md](mica-admin-uniapp/AGENTS.md) | App 二次开发 | 通用模块不修改原则 |
| [AGENTS.md](AGENTS.md) | AI 编码助手 | 仓库级编码规范 |
| [CLAUDE.md](CLAUDE.md) | AI 编码助手 | 架构说明与全局约定 |
| [.claude/skills/](.claude/skills/) | AI 编码助手 | 三端编码套路 + 可复制模板 |

---

## 🤝 贡献

PR 永远欢迎 🙏

1. Fork → 2. Feature Branch → 3. Commit → 4. PR
2. 后端改动请补单测 (`mvn test -Dtest=ClassName#methodName`)
3. 前端改动请跑 `pnpm typecheck` + `pnpm lint`
4. 若引入了新的编码套路或改了既有约定,请同步更新 `.claude/skills/` 下对应的 skill

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