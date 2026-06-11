package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.dreamlu.mica.admin.project.system.entity.SysRoleMenu;
import net.dreamlu.mica.admin.project.system.mapper.SysRoleMenuMapper;
import net.dreamlu.mica.admin.project.system.service.ISysRoleMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 角色和菜单关联表 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements ISysRoleMenuService {

	@Override
	public List<SysRoleMenu> getListByRoleIds(Collection<Long> roleIds) {
		Wrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<SysRoleMenu>()
			.in(SysRoleMenu::getRoleId, roleIds);
		return super.list(wrapper);
	}

	@Override
	public boolean deleteByRoleId(Long roleId) {
		Wrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<SysRoleMenu>()
			.eq(SysRoleMenu::getRoleId, roleId);
		return super.remove(wrapper);
	}

	@Override
	public List<SysRoleMenu> getListByMenuIds(Collection<Long> menuIds) {
		Wrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<SysRoleMenu>()
			.in(SysRoleMenu::getMenuId, menuIds);
		return super.list(wrapper);
	}

}
