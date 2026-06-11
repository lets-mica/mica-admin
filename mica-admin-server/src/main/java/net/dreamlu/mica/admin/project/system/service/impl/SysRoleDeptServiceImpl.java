package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.project.system.mapper.SysRoleDeptMapper;
import net.dreamlu.mica.admin.project.system.entity.SysRoleDept;
import net.dreamlu.mica.admin.project.system.service.ISysRoleDeptService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色和部门关联表 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Service
public class SysRoleDeptServiceImpl extends ServiceImpl<SysRoleDeptMapper, SysRoleDept> implements ISysRoleDeptService {

	@Override
	public boolean saveList(Long roleId, List<Long> deptIdList) {
		List<SysRoleDept> entityList = new ArrayList<>();
		for (Long deptId : deptIdList) {
			SysRoleDept entity = new SysRoleDept();
			entity.setRoleId(roleId);
			entity.setDeptId(deptId);
			entityList.add(entity);
		}
		return super.saveBatch(entityList);
	}

	@Override
	public boolean removeByRoleId(Long roleId) {
		LambdaUpdateWrapper<SysRoleDept> wrapper = Wrappers.lambdaUpdate();
		wrapper.eq(SysRoleDept::getRoleId, roleId);
		return super.remove(wrapper);
	}

	@Override
	public List<Long> findDeptIdListByRoleId(Long roleId) {
		LambdaQueryWrapper<SysRoleDept> wrapper = Wrappers.lambdaQuery();
		wrapper.select(SysRoleDept::getDeptId);
		wrapper.eq(SysRoleDept::getRoleId, roleId);
		return super.list(wrapper).stream()
			.map(SysRoleDept::getDeptId)
			.collect(Collectors.toList());
	}

}
