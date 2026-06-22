## mica-admin

基于开源版 [**mica**](https://gitee.com/596392912/mica) 工具集，采用 maven、spring boot、spring security、vue 的低代码管理后台。

### 0. 项目结构(mono-repo)

```
mica-admin/
├── mica-admin-server/        Spring Boot 后端(Java 8+)
│   └── src/main/java/net/dreamlu/mica/admin/
│       ├── common/           公共常量
│       ├── framework/        框架核心(安全/MyBatis/AOP)
│       ├── project/          业务模块(用户/角色/菜单/部门/字典...)
│       └── im/               IM 模块(基于 mica-mqtt,详见 docs/im/)
├── mica-admin-web/           Web 管理端(Vben Admin 5.x 本地化)
├── mica-admin-uniapp/        移动 App(uniapp x,Vue 3 + TS + Pinia)
├── docs/
│   ├── app/                  App 端设计文档
│   ├── im/                   IM 模块设计文档
│   └── database/             数据库迁移脚本
├── deploy.sh                 一键部署脚本
├── script/                   systemd 脚本
└── AGENTS.md                 AI 编码助手规则
```

> **`mica-admin-uniapp/` 不进 Maven**,是独立 npm 工程。三者放在同一 git 仓库,
> 便于 AI 编码时跨模块协作。

### 1. 文档
1. 导入 `docs/database/mysql.sql` 到 mysql。
2. 更改后端 `mica-admin-server/src/main/resources` 服务下的配置文件。
3. 前端更改 `mica-admin-web` 下的 `.env.production` 下的域名。
4. App 端:参考 `mica-admin-uniapp/` 工程配置。
5. **IM 模块**(可选):参考 [`docs/im/`](./docs/im/README.md) 实施,默认不启用。
6. Redis windows 服务端：https://github.com/tporadowski/redis/releases

### 2. 最佳实践
1. 先设计好数据库，注意表名，字段名描述填写清楚（不要直接更改开发或者生产库）。
2. 运行 `src/test/java/net/dreamlu/generator/MysqlGenerator.java` 中的main方法生成基础代码。
3. 代码生成`jdbc配置`和`模板`详见`test/resources`。
4. 部署默认采用的 `systemd` 详见 `script/start.sh` 脚本。

### 3. 效果演示
https://admin.dreamlu.net

默认用户名：admin、test 密码：test

### 9. 开源推荐
- Spring boot 高效开发之 **Mica** 工具集：[https://gitee.com/596392912/mica](https://gitee.com/596392912/mica)
- 简单易用的 Java mqtt 之 **Mica MQTT**：[https://gitee.com/dromara/mica-mqtt](https://gitee.com/dromara/mica-mqtt)
