package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.project.system.entity.SysDept;
import net.dreamlu.mica.admin.project.system.mapper.SysDeptMapper;
import net.dreamlu.mica.admin.project.system.pojo.DeptQuery;
import net.dreamlu.mica.admin.project.system.service.ISysDeptService;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门表 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

	@Override
	public Wrapper<SysDept> getQueryWrapper(DeptQuery query) {
		String queryName = query.getName();
		LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
		wrapper.like(StringUtil.isNotBlank(queryName), SysDept::getName, queryName);
		wrapper.eq(query.getEnabled() != null, SysDept::getEnabled, query.getEnabled());
		wrapper.eq(query.getPid() != null, SysDept::getParentId, query.getPid());
		List<LocalDateTime> createTime = query.getCreateTime();
		if (createTime != null && createTime.size() > 1) {
			wrapper.between(SysDept::getCreatedAt, createTime.get(0), createTime.get(1));
		}
		return wrapper;
	}

	@Override
	public List<SysDept> getSuperior(List<SysDept> deptList, List<SysDept> superiorList) {
		Set<Long> parentIds = deptList.stream()
			.map(SysDept::getParentId)
			.collect(Collectors.toSet());
		List<SysDept> result = listByIds(parentIds);
		if (result.isEmpty()) {
			return superiorList;
		}
		superiorList.addAll(result);
		return getSuperior(result, superiorList);
	}

	@Override
	public List<SysDept> getChildren(List<Long> deptIdList, List<SysDept> childrenList) {
		List<SysDept> result = getByParentId(deptIdList);
		if (result.isEmpty()) {
			return childrenList;
		}
		childrenList.addAll(result);
		List<Long> deptIdResult = result.stream()
			.map(SysDept::getId)
			.collect(Collectors.toList());
		return getChildren(deptIdResult, childrenList);
	}

	@Override
	public boolean deleteIfUnusedByIds(Collection<Long> ids) {
		return super.removeByIds(ids);
	}

	private List<SysDept> getByParentId(List<Long> deptIdList) {
		if (deptIdList.isEmpty()) {
			return Collections.emptyList();
		}
		LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(SysDept::getParentId, deptIdList);
		wrapper.eq(SysDept::getEnabled, Boolean.TRUE);
		return list(wrapper);
	}

}
