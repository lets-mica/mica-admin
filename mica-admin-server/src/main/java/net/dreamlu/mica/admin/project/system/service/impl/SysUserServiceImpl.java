package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.core.result.R;
import net.dreamlu.mica.core.utils.*;
import net.dreamlu.mica.admin.common.code.ApiCode;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.framework.utils.PageUtil;
import net.dreamlu.mica.admin.project.system.entity.*;
import net.dreamlu.mica.admin.project.system.mapper.SysUserMapper;
import net.dreamlu.mica.admin.project.system.pojo.UserQuery;
import net.dreamlu.mica.admin.project.system.pojo.UserVo;
import net.dreamlu.mica.admin.project.system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
	@Autowired
	private ISysFileStorageService fileStorageService;
	@Autowired
	private ISysDeptService deptService;
	@Autowired
	private ISysUserPostService userPostService;
	@Autowired
	private ISysUserRoleService userRoleService;
	@Autowired
	private ISysPostService postService;
	@Autowired
	private ISysRoleService roleService;

	@Cacheable(value = "sys:user#10m", key = "#userName")
	@Override
	public SysUser getByUserName(String userName) {
		Wrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
			.eq(SysUser::getUserName, userName);
		return super.getOne(wrapper);
	}

	@Override
	public Wrapper<SysUser> getQueryWrapper(UserQuery query) {
		LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
		// 模糊查询字段 email,username,nickName
		String blurry = query.getBlurry();
		wrapper.and(StringUtil.isNotBlank(blurry), w -> w.or()
			.like(SysUser::getEmail, blurry)
			.or()
			.like(SysUser::getUserName, blurry)
			.or()
			.like(SysUser::getNickName, blurry)
		);
		Long deptId = query.getDeptId();
		if (deptId != null) {
			query.getDeptIds().addAll(getAllDeptIds(deptId));
		}
		wrapper.in(ObjectUtil.isNotEmpty(query.getDeptIds()), SysUser::getDeptId, query.getDeptIds());
		wrapper.eq(query.getEnabled() != null, SysUser::getEnabled, query.getEnabled());
		List<LocalDateTime> createTime = query.getCreateTime();
		if (createTime != null && createTime.size() > 1) {
			wrapper.between(SysUser::getCreatedAt, createTime.get(0), createTime.get(1));
		}
		return wrapper;
	}

	@CacheEvict(value = "sys:user#10m", key = "#authUser.username")
	@Override
	public Map<String, Object> updateAvatar(MultipartFile file, AuthUser authUser) {
		// 使用 x-file-storage 上传，保存到 avatar/ 子目录，文件名以 userId 命名便于覆盖更新
		SysFileStorage fileStorage = fileStorageService.upload(file, "avatar", true, null, authUser);
		// 头像地址
		String fileUrl = fileStorage.getUrl();
		// 更新用户信息
		SysUser entity = new SysUser();
		entity.setId(authUser.getUserId());
		entity.setAvatar(fileUrl);
		this.updateById(entity);
		HashMap<String, Object> data = new HashMap<>(1);
		data.put("avatar", fileUrl);
		return data;
	}

	@CacheEvict(value = "sys:user#10m", key = "#userName")
	@Override
	public boolean updateById(SysUser entity, String userName) {
		return updateById(entity);
	}

	@Override
	public Page<UserVo> getUserPage(Page<SysUser> page, Wrapper<SysUser> queryWrapper) {
		Page<SysUser> userPage = super.page(page, queryWrapper);
		List<SysUser> records = userPage.getRecords();
		if (ObjectUtil.isEmpty(records)) {
			return PageUtil.toPage(userPage, UserVo.class);
		}
		// 部门 id 集合
		Set<Long> deptIdSet = new HashSet<>();
		// 用户id 岗位集合
		MultiValueMap<Long, Long> userIdPostMap = new LinkedMultiValueMap<>();
		// 用户id 角色集合
		MultiValueMap<Long, Long> userIdRoleMap = new LinkedMultiValueMap<>();
		// 岗位集合
		Set<Long> postIdSet = new HashSet<>();
		// 角色集合
		Set<Long> roleIdSet = new HashSet<>();
		for (SysUser sysUser : records) {
			Long userId = sysUser.getId();
			deptIdSet.add(sysUser.getDeptId());
			List<SysUserPost> userPostList = userPostService.getListByUserId(userId);
			List<SysUserRole> userRoleList = userRoleService.getListByUserId(userId);
			// 数据转换
			List<Long> userPostIdSet = userPostList.stream()
				.map(SysUserPost::getPostId)
				.distinct()
				.collect(Collectors.toList());
			List<Long> userRoleIdSet = userRoleList.stream()
				.map(SysUserRole::getRoleId)
				.distinct()
				.collect(Collectors.toList());
			// 数据存储
			userIdPostMap.addAll(userId, userPostIdSet);
			userIdRoleMap.addAll(userId, userRoleIdSet);
			postIdSet.addAll(userPostIdSet);
			roleIdSet.addAll(userRoleIdSet);
		}
		// 变换数据结构，方便处理
		Map<Long, SysDept> deptMap = deptService.listByIds(deptIdSet).stream()
			.collect(Collectors.toMap(SysDept::getId, Function.identity()));
		Map<Long, SysPost> postMap = postService.listByIds(postIdSet).stream()
			.collect(Collectors.toMap(SysPost::getId, Function.identity()));
		Map<Long, SysRole> roleMap = roleService.listByIds(roleIdSet).stream()
			.collect(Collectors.toMap(SysRole::getId, Function.identity()));
		return PageUtil.toPage(userPage, (user) -> {
			UserVo userVo = BeanUtil.copy(user, UserVo.class);
			// 用户部门
			userVo.setDept(deptMap.get(user.getDeptId()));
			Long userId = user.getId();
			// 用户岗位
			List<Long> userPostList = userIdPostMap.get(userId);
			if (ObjectUtil.isNotEmpty(userPostList)) {
				userPostList.forEach(postId -> {
					userVo.getPosts().add(postMap.get(postId));
				});
			}
			// 用户角色
			List<Long> userRoleList = userIdRoleMap.get(userId);
			if (ObjectUtil.isNotEmpty(userRoleList)) {
				userRoleList.forEach(roleId -> {
					userVo.getRoles().add(roleMap.get(roleId));
				});
			}
			return userVo;
		});
	}

	@Transactional(rollbackFor = Throwable.class)
	@Override
	public boolean saveUserInfo(SysUser sysUser, List<Long> roleIds, List<Long> postIds) {
		String userName = sysUser.getUserName();
		SysUser existedUser = this.getByUserName(userName);
		if (existedUser != null) {
			R.throwFail(ApiCode.USER_ALREADY_EXISTS);
		}
		// 公共参数
		sysUser.setIsAdmin(Boolean.FALSE);
		sysUser.setLocked(Boolean.FALSE);
		sysUser.setDelFlag(Boolean.FALSE);
		// 1. 保存用户
		super.save(sysUser);
		Long userId = sysUser.getId();
		// 2. 保存用户角色
		userRoleService.saveByUserIdAndRoleIds(userId, roleIds);
		// 3. 保存用户岗位
		return userPostService.saveByUserIdAndPostIds(userId, postIds);
	}

	@Transactional(rollbackFor = Throwable.class)
	@Override
	public boolean updateUserInfo(SysUser sysUser, List<Long> roleIds, List<Long> postIds) {
		String userName = sysUser.getUserName();
		SysUser existedUser = this.getByUserName(userName);
		if (existedUser != null) {
			R.throwFail(ApiCode.USER_ALREADY_EXISTS);
		}
		// 1. 更新用户
		super.updateById(sysUser);
		Long userId = sysUser.getId();
		// 2. 清除用户角色
		userRoleService.deleteByUserId(userId);
		// 3. 清除用户岗位
		userPostService.deleteByUserId(userId);
		// 4. 保存用户角色
		userRoleService.saveByUserIdAndRoleIds(userId, roleIds);
		// 5. 保存用户岗位
		return userPostService.saveByUserIdAndPostIds(userId, postIds);
	}

	@Transactional(rollbackFor = Throwable.class)
	@Override
	public boolean deleteByIds(Set<Long> userIds) {
		// 1. 删除用户
		super.removeByIds(userIds);
		// 2. 清除用户角色
		userRoleService.deleteByUserIds(userIds);
		// 3. 清除用户岗位
		return userPostService.deleteByUserIds(userIds);
	}

	@Override
	public List<SysUser> findListByDeptIds(Collection<Long> deptIds) {
		LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
		wrapper.in(SysUser::getDeptId, deptIds);
		return super.list(wrapper);
	}

	private List<Long> getAllDeptIds(Long deptId) {
		// 获取父类所有的部门
		SysDept sysDept = deptService.getById(deptId);
		List<SysDept> deptList = new ArrayList<>();
		List<SysDept> superiorList = deptService.getChildren(Collections.singletonList(deptId), deptList);
		superiorList.add(sysDept);
		return superiorList.stream()
			.map(SysDept::getId)
			.distinct()
			.collect(Collectors.toList());
	}

}
