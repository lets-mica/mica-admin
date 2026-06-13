package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.project.system.entity.SysUserPost;
import net.dreamlu.mica.admin.project.system.mapper.SysUserPostMapper;
import net.dreamlu.mica.admin.project.system.service.ISysUserPostService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户与岗位关联表 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Service
public class SysUserPostServiceImpl extends ServiceImpl<SysUserPostMapper, SysUserPost> implements ISysUserPostService {

	@Override
	public List<SysUserPost> getListByUserId(Long userId) {
		Wrapper<SysUserPost> wrapper = new LambdaQueryWrapper<SysUserPost>()
			.eq(SysUserPost::getUserId, userId);
		return super.list(wrapper);
	}

	@Override
	public boolean saveByUserIdAndPostIds(Long userId, List<Long> postIds) {
		List<SysUserPost> userRoleList = postIds.stream()
			.map(postId -> {
				SysUserPost userPost = new SysUserPost();
				userPost.setUserId(userId);
				userPost.setPostId(postId);
				return userPost;
			})
			.distinct()
			.collect(Collectors.toList());
		return super.saveBatch(userRoleList);
	}

	@Override
	public boolean deleteByUserId(Long userId) {
		Wrapper<SysUserPost> wrapper = new LambdaQueryWrapper<SysUserPost>()
			.eq(SysUserPost::getUserId, userId);
		return super.remove(wrapper);
	}

	@Override
	public boolean deleteByUserIds(Set<Long> userIds) {
		Wrapper<SysUserPost> wrapper = new LambdaQueryWrapper<SysUserPost>()
			.in(SysUserPost::getUserId, userIds);
		return super.remove(wrapper);
	}

	@Override
	public List<SysUserPost> getListByPostIds(Collection<Long> postIds) {
		Wrapper<SysUserPost> wrapper = new LambdaQueryWrapper<SysUserPost>()
			.eq(SysUserPost::getPostId, postIds);
		return super.list(wrapper);
	}

}
