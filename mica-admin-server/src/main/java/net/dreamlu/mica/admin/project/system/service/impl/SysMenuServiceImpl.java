package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.common.enums.StatusEnum;
import net.dreamlu.mica.admin.project.system.entity.SysMenu;
import net.dreamlu.mica.admin.project.system.entity.SysRoleMenu;
import net.dreamlu.mica.admin.project.system.mapper.SysMenuMapper;
import net.dreamlu.mica.admin.project.system.pojo.MenuQuery;
import net.dreamlu.mica.admin.project.system.service.ISysMenuService;
import net.dreamlu.mica.admin.project.system.service.ISysRoleMenuService;
import net.dreamlu.mica.core.result.R;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单权限表 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {
	@Autowired
	private ISysRoleMenuService roleMenuService;

	@Override
	public Wrapper<SysMenu> getQueryWrapper(MenuQuery query) {
		LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
		String blurry = query.getBlurry();
		// 模糊查询字段 菜单标题
		wrapper.and(StringUtil.isNotBlank(blurry), w -> w
			.like(SysMenu::getTitle, blurry)
			.or().like(SysMenu::getComponent, blurry)
			.or().like(SysMenu::getPermission, blurry));
		List<LocalDateTime> createTime = query.getCreateTime();
		if (createTime != null && createTime.size() > 1) {
			wrapper.between(SysMenu::getCreatedAt, createTime.get(0), createTime.get(1));
		}
		// 统一排序：先父级，再 seq，最后 id 兜底
		wrapper.orderByAsc(SysMenu::getParentId)
			.orderByAsc(SysMenu::getSeq)
			.orderByAsc(SysMenu::getId);
		return wrapper;
	}

	@Override
	public List<SysMenu> getAllMenu() {
		LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
		// 菜单类型（0目录 1菜单 2按钮）
		wrapper.in(SysMenu::getType, 0, 1);
		wrapper.eq(SysMenu::getHidden, Boolean.FALSE);
		wrapper.eq(SysMenu::getStatus, StatusEnum.NORMAL.getValue());
		// 按 parentId、seq 升序，seq 相同时按 id 兜底
		wrapper.orderByAsc(SysMenu::getParentId)
			.orderByAsc(SysMenu::getSeq)
			.orderByAsc(SysMenu::getId);
		return super.list(wrapper);
	}

	@Override
	public List<SysMenu> getListByRoleIds(Collection<Long> roleIds) {
		List<Long> roleMenuIdList = roleMenuService.getIdListByRoleIds(roleIds);
		if (roleMenuIdList.isEmpty()) {
			return Collections.emptyList();
		}
		List<SysMenu> list = super.listByIds(roleMenuIdList);
		// listByIds 顺序不可控，重新按 parentId/seq 排序
		return list.stream()
			.sorted((a, b) -> {
				int c = Long.compare(
					a.getParentId() == null ? 0L : a.getParentId(),
					b.getParentId() == null ? 0L : b.getParentId());
				if (c != 0) return c;
				c = Integer.compare(a.getSeq() == null ? 0 : a.getSeq(),
					b.getSeq() == null ? 0 : b.getSeq());
				if (c != 0) return c;
				return Long.compare(a.getId(), b.getId());
			})
			.collect(Collectors.toList());
	}

	@Override
	public List<SysMenu> getNavByRoleIds(Collection<Long> roleIds) {
		List<Long> roleMenuIdList = roleMenuService.getIdListByRoleIds(roleIds);
		if (roleMenuIdList.isEmpty()) {
			return Collections.emptyList();
		}
		LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(SysMenu::getId, roleMenuIdList);
		// 菜单类型（0目录 1菜单 2按钮）
		wrapper.in(SysMenu::getType, 0, 1);
		wrapper.eq(SysMenu::getHidden, Boolean.FALSE);
		wrapper.eq(SysMenu::getStatus, StatusEnum.NORMAL.getValue());
		// 按 parentId、seq 升序，seq 相同时按 id 兜底
		wrapper.orderByAsc(SysMenu::getParentId)
			.orderByAsc(SysMenu::getSeq)
			.orderByAsc(SysMenu::getId);
		return super.list(wrapper);
	}

	@Override
	public List<SysMenu> getSuperior(List<SysMenu> menuList, List<SysMenu> superiorList) {
		Set<Long> parentIds = menuList.stream()
			.map(SysMenu::getParentId)
			.collect(Collectors.toSet());
		List<SysMenu> result = listByIds(parentIds);
		if (result.isEmpty()) {
			return superiorList;
		}
		superiorList.addAll(result);
		return getSuperior(result, superiorList);
	}

	@Override
	public boolean deleteIfUnusedByIds(Collection<Long> ids) {
		List<SysRoleMenu> roleMenuList = roleMenuService.getListByMenuIds(ids);
		if (roleMenuList != null && !roleMenuList.isEmpty()) {
			R.throwFail("存在角色菜单关系");
		}
		return super.removeByIds(ids);
	}

}
