# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

mica-admin 是基于 [mica](https://gitee.com/596392912/mica) 工具集的低代码权限管理平台，使用 Maven、Spring Boot 2.7、Spring Security、MyBatis-Plus（后端）+ Vben Admin 5.x（前端）。

Maven multi-module 结构：

- `mica-admin-server/` — Spring Boot 后端（Java 8+）
- `mica-admin-web/` — 前端工程（Vite + Vue 3 + Naive UI）。这是 Vben Admin 5.x 的**本地化**版本，源码提取到 `mica-admin-web/_vben/`，由 pnpm workspace 协议引用。完整前端说明见 `mica-admin-web/CLAUDE.md`。

## 构建命令

### 后端 (`mica-admin-server`)

从仓库根目录执行：

```bash
# 默认 dev profile，跳过前端构建
mvn package
# 生产构建：触发 pnpm install + pnpm build:prod，把 dist 打入 META-INF/resources
mvn clean package -Pprod -U -Dmaven.test.skip=true

# 单测
mvn test -Dtest=ClassName#methodName -DfailIfNoTests=false

# 本地启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Maven profile 行为：
- `dev`（默认激活）：`skipNpmBuild=true`，不构建前端。
- `prod`：`skipNpmBuild=false`，通过 `exec-maven-plugin` 触发前端构建，并把 `mica-admin-web/dist` 复制到 `META-INF/resources`，与后端 jar 合并。

### 前端 (`mica-admin-web`)

```bash
cd mica-admin-web
pnpm install
pnpm dev               # Vite dev server，端口 5888，/api 代理到 http://localhost:8080
pnpm typecheck         # vue-tsc --noEmit --skipLibCheck
pnpm lint              # eslint --fix
pnpm build             # 类型检查 + 生产构建
pnpm build:prod        # 仅打包，跳过类型检查（Maven prod profile 调的就是这个）
pnpm build:analyze     # 构建并分析包体积
pnpm api               # 从 http://127.0.0.1:8080/v3/api-docs 生成 swagger 客户端到 src/api/
```

## 后端架构 (`mica-admin-server`)

主包：`net.dreamlu.mica.admin`，源码 `src/main/java/`：

- `net.dreamlu.mica.admin.common` — 公共常量（`ApiCode`、`MicaAdminConstants`）。
- `net.dreamlu.mica.admin.framework` — 框架核心：安全、MyBatis、AOP 日志、控制器基类、VO、工具类。
- `net.dreamlu.mica.admin.project` — 业务模块，目前主要是 `project.system`（用户/角色/菜单/部门/字典/文件存储/监控/日志/通知）。

### 关键框架细节

- **容器**：Undertow（非 Tomcat），日志 Log4j2（非 Logback）。
- **ORM**：MyBatis-Plus 3.5 + Druid。实体扫描 `net.dreamlu.mica.admin.**.entity`；Mapper XML 位于 `classpath:net/dreamlu/mica/admin/**/mapper/*Mapper.xml`。
- **存储**：MySQL（schema 见 `docs/database/mysql.sql`）+ Redis（localhost:6379）。
- **安全**：Spring Security + JWT（HMAC，密钥在 `mica.security.jwt-token.secret`）+ RSA 公钥加密登录密码 + 算术图形验证码。配置见 `framework.config.MicaAdminSecurityConfig`。
  - 放行白名单在 `application.yml` 的 `mica.security.permit-all` 中。
  - 表单登录端点：`POST /api/session`（form-urlencoded）。
  - 注销端点：`GET /api/logout`。
  - 认证相关组件：`framework.security.auth.*`（`SecAuthenticationProvider`、`SecAuthHandler`、`SecWebAuthDetailsSource`、`AuthUserArgumentResolver`）。
  - JWT：`framework.security.jwt.*`（`JwtTokenService`、`JwtTokenStore`、`JwtAuthenticationTokenFilter`、`JwtUser`）。
- **基础类**：`framework.base.BaseModel`（含 `id/created_by/created_at/updated_by/updated_at` 审计字段，由 `framework.mybatis.MybatisPlusMetaObjectHandler` 自动填充）和 `framework.base.BaseController`（统一响应封装）。
- **系统日志**：`@ApiLog` 注解 + AOP 切面（`framework.syslog.SysLogAspect`）→ 事件 → `SysLogListener` 异步落库。
- **API 文档**：`mica-openapi`（Swagger/Knife4j），访问 `/doc.html`。
- **文件存储**：`dromara.x-file-storage`（本地 + OSS 抽象），控制器 `SysFileStorageController`。
- **低代码**：Magic-API 通过 DB 存储（表 `magic_api_file`），web UI 在 `/magic/web`。

### 代码生成

`spring-boot-starter-test` scope 下含 `mybatis-plus-generator` + Freemarker 引擎。生成器入口：`mica-admin-server/src/test/java/net/dreamlu/mica/admin/generator/MysqlAutoGenerator.java`。

执行 `main()` 时：
- 从 `application-dev.yml` 读取 JDBC 配置。
- Entity 继承 `BaseModel`，Controller 继承 `BaseController`。
- 输出到 `src/gen_code/`（在 `.gitignore` 中）。

切换表名时只需修改 `strategyConfig.addInclude(...)`。

## 前端架构（`mica-admin-web/`）

完整说明见 `mica-admin-web/CLAUDE.md`。摘要要点：

- 框架：Vue 3.5 + Naive UI 2.44 + Vite 6 + TypeScript 5.7 + Tailwind CSS 4 + Pinia 3。
- **包管理**：pnpm workspace，Vben 源码本地化到 `_vben/packages/*`、`_vben/core/*`、`_vben/tailwind-config/`。`vite.config.ts` 中的 `vbenResolver()` 插件负责解析 `@vben/*`、`@vben-core/*`、`@vben/common-ui/es/*`、`@vben/plugins/*` 等别名到本地路径。
- **后端联调**：dev server 通过 `/api` 代理到 `http://localhost:8080`。
- **认证流程已针对 mica-admin 后端定制**：
  - `src/store/auth-mica-admin.ts` + `src/api/core/auth-mica-admin.ts`
  - 登录顺序：获取 RSA 公钥 (`GET /api/auth/public-key`) → 获取算术验证码 (`GET /api/auth/captcha`) → `POST /api/session`（密码 RSA 加密，form-urlencoded）→ `GET /api/auth/info` → `GET /api/auth/menus`。
  - **不要**修改 `src/api/core/auth.ts`（Vben 默认实现），mica-admin 适配一律放在 `auth-mica-admin.*`。
- **响应处理**：`src/api/request.ts` 中 `defaultResponseInterceptor({ successCode: 0, codeField: 'code', dataField: 'data' })` —— **注意：mica-admin 成功 code = 0**，与一般 Vben 模板不同。
- **模块路由**：`src/router/routes/modules/`（dashboard、system、monitor、tools、components）。动态路由由 `src/router/access.ts` 的 `generateAccessible()` 基于后端菜单生成，配合 `import.meta.glob('../views/**/*.vue')` 自动匹配页面组件。
- **i18n**：`src/locales/langs/{zh-CN,en-US}/`，通过 `vue-i18n` 引用。
- **API 自动生成**：`pnpm api` 读取后端 swagger (`http://127.0.0.1:8080/v3/api-docs`)，输出到 `src/api/Api.ts`。

## 配置文件

后端：
- `mica-admin-server/src/main/resources/application.yml` — 公共配置（端口、Redis、Mail、MyBatis-Plus、Security 白名单、JWT 密钥）。
- `mica-admin-server/src/main/resources/application-dev.yml` — 本地开发配置（MySQL、Redis、文件存储）。
- `mica-admin-server/src/main/resources/application-prod.yml` — 生产配置。
- `mica-admin-server/src/main/resources/messages/messages*.properties` — i18n 文案。
- `mica-admin-server/src/main/resources/templates/email.ftl` — Freemarker 邮件模板。

前端：
- `mica-admin-web/.env.development` / `.env.production` — `VITE_GLOB_API_URL`（默认 `/api`）、`VITE_PORT`（dev=5888）、`VITE_ROUTER_HISTORY`（dev=`web`、prod=`hash`）、`VITE_APP_NAMESPACE`、`VITE_APP_STORE_SECURE_KEY`。

## 部署

- **Linux systemd 脚本**：`script/start.sh`。服务目录约定 `/www/server/${SERVER_NAME}/${SERVER_NAME}.jar`，命令 `start.sh 服务名 {startd|restartd|stopd}`。脚本会自动写入 `/etc/systemd/system/${SERVER_NAME}.service` 并 `enable`。
- **一键发布**：仓库根 `deploy.sh`（`mvn clean package -Pprod -U -Dmaven.test.skip=true` → scp jar 与 `start.sh` 到远端 host `tx` → 远端执行 `restartd` → 清理本地 prod 包）。

## 前置依赖

- MySQL：导入 `docs/database/mysql.sql`。
- Redis：localhost:6379。
- Java 8+（后端）。
- Node.js 18+ + pnpm（前端）。
- Linux 部署需 jdk8（脚本按序探测 `/www/server/jdk8/`、`/usr/local/jdk`、`/data/jdk`）。

## 编辑器约定（`.editorconfig`）

| 文件类型 | 缩进 |
|---|---|
| `*.java` | Tab |
| `*.{vue,js,json,yml,yaml}` | 2 空格 |
| 其它 | 4 空格 |

行尾 `lf`，UTF-8，文件末尾保留换行（`*.{txt,md}` 除外）。

## 注意事项

- 前端工作**不要**直接改 `_vben/` 内的"包对包"导入路径（别名由 `vbenResolver()` 接管，改了反而破坏解析）。
- `_vben/` 内**不要**使用 Tailwind `@apply`（已通过 `scripts/fix-apply.mjs` 批量转纯 CSS）。
- 业务侧 RBAC：路由/菜单/按钮权限通过后端 `sys_menu` + `sys_role_menu` 控制，前端通过 `v-permission` 自定义指令与动态路由消费。
- README 中 TODO 列表（数据权限）尚未实现，新增需求前先确认是否落在该 TODO。
- 后端成功响应 `code = 0`（非 200），前端拦截器已适配；新写接口时保持一致。
