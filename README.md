## mica-admin

基于开源版 [**mica**](https://gitee.com/596392912/mica) 工具集，采用 maven、spring boot、spring security、vue 的低代码管理后台。

[✨✨✨推广：**BladeX 物联网平台**✨✨✨iot.bladex.cn](https://iot.bladex.cn?from=mica-mqtt)

### 技术栈

#### 后端 (`mica-admin-server`)
- **基础**：Java 17 + Spring Boot 4.1 + Maven
- **Web 容器**：Undertow
- **安全**：Spring Security + JWT（HMAC）+ RSA 公钥加密 + 算术图形验证码
- **ORM**：MyBatis-Plus 3.5 + Druid 连接池
- **存储**：MySQL + Redis
- **日志**：Log4j2 + `@ApiLog` AOP 切面异步落库
- **API 文档**：mica-openapi（Swagger / Knife4j，访问 `/doc.html`）
- **工具集**：[mica](https://gitee.com/596392912/mica)（mica-lite / mica-captcha / mica-redis / mica-xss / mica-ip2region / mica-logging / mica-openapi）
- **文件存储**：dromara x-file-storage（本地 + OSS 抽象）
- **Excel**：excel-spring-boot-starter
- **其他**：oshi（系统信息）、yauaa（User-Agent 解析）

#### 前端 (`mica-admin-web`)
- **框架**：Vue 3.5 + Vite 6 + TypeScript 5.7
- **UI 库**：Naive UI 2.44 + vxe-table
- **样式**：Tailwind CSS 4
- **状态管理**：Pinia 3 + pinia-plugin-persistedstate
- **路由**：Vue Router（基于后端菜单的动态路由）
- **国际化**：vue-i18n（zh-CN / en-US）
- **包管理**：pnpm workspace，Vben Admin 5.x 源码本地化到 `vben/` 目录
- **图标**：lucide-vue-next

### 分支说明

| 分支 | 用途 | 说明 |
|------|------|------|
| `main` | 主分支（默认） | **Java 17 + Spring Boot 4.1** 版本， |
| `java8` | 兼容分支 | **Java 8 + Spring Boot 2.7** 版本。 |

### 1. 文档
1. 导入 `docs/database/mysql.sql` 到 mysql。
2. 更改后端 `mica-admin-server/src/main/resources` 服务下的配置文件。
3. 前端更改 `mica-admin-web` 下的 `.env.production` 下的域名。
4. Redis windows 服务端：https://github.com/tporadowski/redis/releases

### 2. 最佳实践
1. 先设计好数据库，注意表名，字段名描述填写清楚（不要直接更改开发或者生产库）。
2. 运行 `src/test/java/net/dreamlu/generator/MysqlGenerator.java` 中的main方法生成基础代码。
3. 代码生成`jdbc配置`和`模板`详见`test/resources`。
4. 部署默认采用的 `systemd` 详见 `script/start.sh` 脚本。

### 3. 效果演示
http://admin.dreamlu.net

默认用户名：admin、test 密码：test

### 9. 开源推荐
- Spring boot 高效开发之 **Mica** 工具集：[https://gitee.com/596392912/mica](https://gitee.com/596392912/mica)
- 简单易用的 Java mqtt 之 **Mica MQTT**：[https://gitee.com/dromara/mica-mqtt](https://gitee.com/dromara/mica-mqtt)
