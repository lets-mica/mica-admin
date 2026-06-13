package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.project.system.entity.SysPost;
import net.dreamlu.mica.admin.project.system.entity.SysUserPost;
import net.dreamlu.mica.admin.project.system.mapper.SysPostMapper;
import net.dreamlu.mica.admin.project.system.pojo.PostQuery;
import net.dreamlu.mica.admin.project.system.service.ISysPostService;
import net.dreamlu.mica.admin.project.system.service.ISysUserPostService;
import net.dreamlu.mica.core.result.R;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 岗位信息表 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Service
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements ISysPostService {
	@Autowired
	private ISysUserPostService userPostService;

	@Override
	public Wrapper<SysPost> getQueryWrapper(PostQuery query) {
		LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
		String name = query.getName();
		wrapper.like(StringUtil.isNotBlank(name), SysPost::getName, name);
		wrapper.eq(query.getEnabled() != null, SysPost::getEnabled, query.getEnabled());
		List<LocalDateTime> createTime = query.getCreateTime();
		if (createTime != null && createTime.size() > 1) {
			wrapper.between(SysPost::getCreatedAt, createTime.get(0), createTime.get(1));
		}
		return wrapper;
	}

	@Cacheable(value = "sys:post:user#10m", key = "#userId")
	@Override
	public List<SysPost> getListByUserId(Long userId) {
		// 获取关联列表
		List<SysUserPost> userPostList = userPostService.getListByUserId(userId);
		if (userPostList.isEmpty()) {
			return Collections.emptyList();
		}
		// 获取角色id列表
		Set<Long> roleIds = userPostList.stream()
			.map(SysUserPost::getPostId)
			.collect(Collectors.toSet());
		// 获取角色列表
		Wrapper<SysPost> wrapper = new LambdaQueryWrapper<SysPost>()
			.in(SysPost::getId, roleIds)
			.eq(SysPost::getEnabled, Boolean.TRUE);
		return super.list(wrapper);
	}

	@Override
	public boolean deleteIfUnusedByIds(Collection<Long> ids) {
		List<SysUserPost> userPostList = userPostService.getListByPostIds(ids);
		if (userPostList != null && !userPostList.isEmpty()) {
			R.throwFail("存在用户岗位关系");
		}
		return super.removeByIds(ids);
	}

}
