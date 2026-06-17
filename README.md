## mica-admin

基于开源版 [**mica**](https://gitee.com/596392912/mica) 工具集，采用 maven、spring boot、spring security、vue 的低代码管理后台。

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
https://admin.dreamlu.net

默认用户名：admin、test 密码：test

### 9. 开源推荐
- Spring boot 高效开发之 **Mica** 工具集：[https://gitee.com/596392912/mica](https://gitee.com/596392912/mica)
- 简单易用的 Java mqtt 之 **Mica MQTT**：[https://gitee.com/dromara/mica-mqtt](https://gitee.com/dromara/mica-mqtt)
