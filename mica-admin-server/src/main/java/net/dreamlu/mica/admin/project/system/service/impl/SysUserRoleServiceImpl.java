package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.project.system.mapper.SysUserRoleMapper;
import net.dreamlu.mica.admin.project.system.entity.SysUserRole;
import net.dreamlu.mica.admin.project.system.service.ISysUserRoleService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户和角色关联表 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {

	@Override
	public List<SysUserRole> getListByUserId(Long userId) {
		Wrapper<SysUserRole> wrapper = new LambdaQueryWrapper<SysUserRole>()
			.eq(SysUserRole::getUserId, userId);
		return super.list(wrapper);
	}

	@Override
	public boolean saveByUserIdAndRoleIds(Long userId, List<Long> roleIds) {
		List<SysUserRole> userRoleList = roleIds.stream()
			.map(roleId -> {
				SysUserRole userRole = new SysUserRole();
				userRole.setUserId(userId);
				userRole.setRoleId(roleId);
				return userRole;
			})
			.distinct()
			.collect(Collectors.toList());
		return super.saveBatch(userRoleList);
	}

	@Override
	public boolean deleteByUserId(Long userId) {
		Wrapper<SysUserRole> wrapper = new LambdaQueryWrapper<SysUserRole>()
			.eq(SysUserRole::getUserId, userId);
		return super.remove(wrapper);
	}

	@Override
	public boolean deleteByUserIds(Set<Long> userIds) {
		Wrapper<SysUserRole> wrapper = new LambdaQueryWrapper<SysUserRole>()
			.in(SysUserRole::getUserId, userIds);
		return super.remove(wrapper);
	}

	@Override
	public List<SysUserRole> getListByRoleIds(Collection<Long> ids) {
		Wrapper<SysUserRole> wrapper = new LambdaQueryWrapper<SysUserRole>()
			.in(SysUserRole::getRoleId, ids);
		return super.list(wrapper);
	}

}
