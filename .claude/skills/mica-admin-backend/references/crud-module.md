# CRUD 模块完整模板

以「轮播图 `sys_banner`」为例。复制后全局替换 `Banner` → 你的实体名、`banner` → 小写资源名、`轮播图` → 中文名。

## 1. 实体 `project/system/entity/SysBanner.java`

```java
package net.dreamlu.mica.admin.project.system.entity;

import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.common.enums.EnabledEnum;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * <p>
 * 轮播图
 * </p>
 *
 * @author L.cm
 * @since 2026-07-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysBanner extends BaseModel {
	private static final long serialVersionUID = 1L;

	/**
	 * 标题
	 */
	@ExcelProperty(value = "标题", index = 0)
	@ColumnWidth(16)
	private String title;
	/**
	 * 图片地址
	 */
	@ExcelProperty(value = "图片地址", index = 1)
	@ColumnWidth(32)
	private String imageUrl;
	/**
	 * 显示顺序
	 */
	@ExcelProperty(value = "显示顺序", index = 2)
	@ColumnWidth(12)
	private Integer seq;
	/**
	 * 状态（0停用,1正常）
	 */
	@ExcelProperty(value = "状态", index = 3, converter = EnabledEnum.Converter.class)
	@ColumnWidth(10)
	private Integer enabled;
	/**
	 * 备注
	 */
	@ExcelProperty(value = "备注", index = 4)
	@ColumnWidth(24)
	private String remark;
}
```

## 2. 查询条件 `project/system/pojo/BannerQuery.java`

```java
package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图查询
 *
 * @author L.cm
 */
@Data
public class BannerQuery {

	/**
	 * like
	 */
	private String title;
	private Integer enabled;
	private List<LocalDateTime> createTime;
}
```

## 3. Mapper `project/system/mapper/SysBannerMapper.java`

```java
package net.dreamlu.mica.admin.project.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.dreamlu.mica.admin.project.system.entity.SysBanner;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 轮播图 Mapper 接口
 * </p>
 *
 * @author L.cm
 */
@Mapper
public interface SysBannerMapper extends BaseMapper<SysBanner> {

}
```

自定义 SQL 才需要 `src/main/resources/net/dreamlu/mica/admin/project/system/mapper/SysBannerMapper.xml`。

## 4. 服务接口 `project/system/service/ISysBannerService.java`

```java
package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysBanner;
import net.dreamlu.mica.admin.project.system.pojo.BannerQuery;

/**
 * <p>
 * 轮播图 服务类
 * </p>
 *
 * @author L.cm
 */
public interface ISysBannerService extends IService<SysBanner> {

	/**
	 * 根据 query 组装查询条件
	 *
	 * @param query BannerQuery
	 * @return Wrapper
	 */
	Wrapper<SysBanner> getQueryWrapper(BannerQuery query);
}
```

## 5. 服务实现 `project/system/service/impl/SysBannerServiceImpl.java`

```java
package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.project.system.entity.SysBanner;
import net.dreamlu.mica.admin.project.system.mapper.SysBannerMapper;
import net.dreamlu.mica.admin.project.system.pojo.BannerQuery;
import net.dreamlu.mica.admin.project.system.service.ISysBannerService;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 轮播图 服务实现类
 * </p>
 *
 * @author L.cm
 */
@Service
public class SysBannerServiceImpl extends ServiceImpl<SysBannerMapper, SysBanner> implements ISysBannerService {

	@Override
	public Wrapper<SysBanner> getQueryWrapper(BannerQuery query) {
		LambdaQueryWrapper<SysBanner> wrapper = new LambdaQueryWrapper<>();
		String title = query.getTitle();
		wrapper.like(StringUtil.isNotBlank(title), SysBanner::getTitle, title);
		wrapper.eq(query.getEnabled() != null, SysBanner::getEnabled, query.getEnabled());
		List<LocalDateTime> createTime = query.getCreateTime();
		if (createTime != null && createTime.size() > 1) {
			wrapper.between(SysBanner::getCreatedAt, createTime.get(0), createTime.get(1));
		}
		wrapper.orderByAsc(SysBanner::getSeq);
		return wrapper;
	}
}
```

依赖其它 service 时既存风格是 `@Autowired` 字段注入；无循环依赖的新模块优先 `@RequiredArgsConstructor` 构造注入。
有关联表的删除写成 `deleteIfUnusedByIds(Collection<Long> ids)`，先查关联再 `R.throwFail("存在xxx关系")`。

## 6. 控制器 `project/system/controller/SysBannerController.java`

```java
package net.dreamlu.mica.admin.project.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.annotation.ApiLog;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.project.system.entity.SysBanner;
import net.dreamlu.mica.admin.project.system.pojo.BannerQuery;
import net.dreamlu.mica.admin.project.system.service.ISysBannerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 轮播图 前端控制器
 * </p>
 *
 * @author L.cm
 */
@Validated
@RestController
@RequestMapping("/api/system/banner")
@Tag(name = "系统-轮播图管理")
@RequiredArgsConstructor
public class SysBannerController extends BaseController {
	private final ISysBannerService bannerService;

	@Operation(summary = "轮播图列表")
	@GetMapping
	@PreAuthorize("@sec.hasPermission('system:banner:list')")
	public IPage<SysBanner> list(Page<SysBanner> page, BannerQuery query) {
		return bannerService.page(page, bannerService.getQueryWrapper(query));
	}

	@Operation(summary = "轮播图详情")
	@GetMapping("{id}")
	@PreAuthorize("@sec.hasPermission('system:banner:query')")
	public SysBanner getInfo(@PathVariable Long id) {
		return bannerService.getById(id);
	}

	@Operation(summary = "新增轮播图")
	@ApiLog("新增轮播图")
	@PostMapping
	@PreAuthorize("@sec.hasPermission('system:banner:add')")
	public void add(@Validated @RequestBody SysBanner entity) {
		bannerService.save(entity);
	}

	@Operation(summary = "修改轮播图")
	@ApiLog("修改轮播图")
	@PutMapping
	@PreAuthorize("@sec.hasPermission('system:banner:edit')")
	public void edit(@Validated @RequestBody SysBanner entity) {
		bannerService.updateById(entity);
	}

	@Operation(summary = "删除轮播图")
	@ApiLog("删除轮播图")
	@DeleteMapping
	@PreAuthorize("@sec.hasPermission('system:banner:del')")
	public void remove(@NotEmpty @RequestBody Set<Long> ids) {
		bannerService.removeByIds(ids);
	}

	@ApiLog("导出轮播图")
	@GetMapping("download")
	@ResponseExcel(name = "轮播图数据")
	@PreAuthorize("@sec.hasPermission('system:banner:export')")
	public List<SysBanner> export(BannerQuery query) {
		return bannerService.list(bannerService.getQueryWrapper(query));
	}
}
```

## 7. 菜单与权限 SQL（追加到 `docs/database/mysql.sql`）

`menu_type`：`1` = 菜单，`2` = 按钮。`component` 填前端 `src/views/` 下相对路径；按钮行 `path`/`component` 留空、`icon` 用 `'#'`。

```sql
-- 菜单（parent_id = 1 即挂在「系统管理」下）
INSERT INTO `sys_menu` VALUES (120, 1, '轮播图管理', 'Banner', 10, 'banner', 'system:banner:list', 'system/banner/index', 'lucide:image', 0, 1, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '轮播图管理菜单');
-- 按钮权限
INSERT INTO `sys_menu` VALUES (1201, 120, '轮播图查询', 'BannerQuery',  1, '', 'system:banner:query',  '', '#', 0, 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1202, 120, '轮播图新增', 'BannerAdd',    2, '', 'system:banner:add',    '', '#', 0, 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1203, 120, '轮播图修改', 'BannerEdit',   3, '', 'system:banner:edit',   '', '#', 0, 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1204, 120, '轮播图删除', 'BannerDelete', 4, '', 'system:banner:del',    '', '#', 0, 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (1205, 120, '轮播图导出', 'BannerExport', 5, '', 'system:banner:export', '', '#', 0, 2, 0, 0, 0, 'admin', NOW(), 'admin', NOW(), '');
```

写之前先 `grep -n "sys_menu" docs/database/mysql.sql` 确认现有列序与 id 占用情况。`name` 必须全局唯一（前端路由名）；顶级菜单 `path` 的前导 `/` 由后端 `MenuVoUtil` 自动补。插入后需给角色授权（`sys_role_menu`）并重新登录才能看到。

## 定时任务（可选）

任务类放 `project/<module>/job/`，参考 `project/system/job/DemoSysJob.java` 与 `framework/job/`（annotation + core + loader）机制，任务元数据落 `sys_job` 表，由 `SysJobController` 管理。
