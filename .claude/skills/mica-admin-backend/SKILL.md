---
name: mica-admin-backend
description: Use when adding, modifying or reviewing backend code in mica-admin-server (Spring Boot 4.1 + MyBatis-Plus + Spring Security). Covers the 6-file CRUD module recipe (entity/query/mapper/service/impl/controller), response conventions (success returns raw body, code=0 only on error), @ApiLog audit logging, @PreAuthorize permission codes, IPage pagination, Excel export, caching, and sys_menu seeding.
---

# mica-admin 后端开发

Spring Boot 4.1 + MyBatis-Plus 3.5 + Spring Security + JWT，主包 `net.dreamlu.mica.admin`。

## 分层与文件位置

业务代码一律落在 `mica-admin-server/src/main/java/net/dreamlu/mica/admin/project/<module>/`：

| 层 | 目录 | 命名 | 基类/父接口 |
|---|---|---|---|
| 实体 | `entity/` | `SysBanner` | `extends BaseModel` |
| 查询条件 | `pojo/` | `BannerQuery` | 纯 `@Data` POJO |
| Mapper | `mapper/` | `SysBannerMapper` + 同名 `.xml` | `BaseMapper<T>` |
| 服务接口 | `service/` | `ISysBannerService` | `IService<T>` |
| 服务实现 | `service/impl/` | `SysBannerServiceImpl` | `ServiceImpl<M, T>` |
| 控制器 | `controller/` | `SysBannerController` | `extends BaseController` |

框架层（`framework/`）只在需要扩展安全、AOP、序列化等横切能力时才改。

完整可复制模板见 `references/crud-module.md`。

## 硬性约定

**响应格式** — 成功直接返回裸数据体（对象 / `List` / `IPage`），**不要**手工包 `R.success()`；写操作返回 `void`。失败由全局异常处理器包成 `{ code, msg }`，且**成功 code = 0**（不是 200），前端拦截器按此适配，不要改。

**审计字段** — `BaseModel` 已含 `id/createdBy/createdAt/updatedBy/updatedAt`，由 `MybatisPlusMetaObjectHandler` 自动填充。实体里**不要**重复声明，也不要手动 set。

**分页** — 控制器直接把 `Page<T> page` 作为入参（Spring 自动绑定 `?current=&size=`），返回 `IPage<T>`：
```java
public IPage<SysBanner> list(Page<SysBanner> page, BannerQuery query) {
    return bannerService.page(page, bannerService.getQueryWrapper(query));
}
```

**查询条件构造** — 统一放在 service 的 `getQueryWrapper(query)`，用 `LambdaQueryWrapper` + 条件生效标志位，**不要**在控制器里拼 wrapper：
```java
wrapper.like(StringUtil.isNotBlank(name), SysBanner::getName, name);
wrapper.eq(query.getEnabled() != null, SysBanner::getEnabled, query.getEnabled());
```
时间范围用 `List<LocalDateTime> createTime`，`size() > 1` 时 `between`。

**业务异常** — 用 `R.throwFail("存在用户岗位关系")`，不要自己 `throw new RuntimeException`。

**校验** — 控制器方法加 `@Validated`，类上加 `@Validated` 才能校验 `@NotEmpty` 等方法级参数。新增/修改共用实体时用 `CreateGroup` / `UpdateGroup` 分组（`BaseModel.id` 已按分组标注 `@Null` / `@NotNull`）。

**删除** — 批量删除签名固定为 `@NotEmpty @RequestBody Set<Long> ids`，`DELETE` 方法。有关联关系的先校验再删（`deleteIfUnusedByIds`）。

**缓存** — `@Cacheable(value = "sys:post:user#10m", key = "#userId")`，value 用 `冒号分隔业务名#TTL` 格式，TTL 由 mica 的 Redis cache 解析。

**Excel 导出** — 控制器 `@GetMapping("download")` + `@ResponseExcel(name = "岗位数据")` 返回 `List<T>`；实体字段加 `@ExcelProperty(value, index)` + `@ColumnWidth`，枚举列用 `converter = EnabledEnum.Converter.class`，不导出的字段 `@ExcelIgnore`。

**Mapper XML** — 放在 `src/main/resources/net/dreamlu/mica/admin/**/mapper/*Mapper.xml`（与 Java 包路径镜像）。简单 CRUD 靠 MyBatis-Plus，无需写 XML。

## 权限码

格式 `<模块>:<资源>:<动作>`，动作用 `list / query / add / edit / del / export`。

```java
@PreAuthorize("@sec.hasPermission('system:banner:list')")
```

⚠️ **用 `:del`，不要用 `:remove`**。历史遗留的 `SysPostController` / `SysNoticeController` / `SysMessageController` 里写的是 `:remove`，但数据库 `sys_menu` 与前端按钮用的都是 `:del`——这几处是既存 bug，新代码一律对齐 `:del`，改动老模块时顺手核对 `docs/database/mysql.sql`。

放行接口写进 `application.yml` 的 `mica.security.permit-all`，不要在代码里关安全。

## 审计日志

写操作加 `@ApiLog("新增轮播图")`，AOP（`framework.syslog.SysLogAspect`）发事件 → `SysLogListener` 异步落库。查询接口不加。`@Operation(summary = ...)` 用于 Swagger，两者都要写。

## 新增模块 checklist

1. 建表 SQL 写进 `docs/database/mysql.sql`。
2. 可用 `src/test/java/.../generator/MysqlAutoGenerator.java` 生成骨架（改 `strategyConfig.addInclude(...)`，产物在 `src/gen_code/`，需手工搬进 `project/`）。
3. 按上表补全 6 个文件。
4. 在 `docs/database/mysql.sql` 追加 `sys_menu` 菜单行 + 5 条按钮权限行（模板见 references）。
5. `mvn test -Dtest=XxxTest -DfailIfNoTests=false` / `mvn spring-boot:run -Dspring-boot.run.profiles=dev` 验证。
6. 前端联调：前端接口类型是**手写**的（无 swagger 代码生成），改动实体字段后需同步 `mica-admin-web/src/api/` 与 uniapp 的类型定义；`/doc.html` 可用于人工核对。

## 命令

```bash
mvn package                                          # dev profile，跳过前端构建
mvn clean package -Pprod -U -Dmaven.test.skip=true   # 生产包（含前端 dist）
mvn test -Dtest=ClassName#methodName -DfailIfNoTests=false
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Java 用 **Tab** 缩进（`.editorconfig`），LF 行尾，文件末尾留换行。日志用 Log4j2，容器是 Tomcat（4.1 已移除 Undertow）。
