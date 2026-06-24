package net.dreamlu.mica.admin.project.im.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.admin.framework.base.BaseController;
import net.dreamlu.mica.admin.framework.security.auth.AuthUser;
import net.dreamlu.mica.admin.project.im.entity.ImGroup;
import net.dreamlu.mica.admin.project.im.entity.ImGroupMember;
import net.dreamlu.mica.admin.project.im.mapper.ImGroupMemberMapper;
import net.dreamlu.mica.admin.project.im.service.IImGroupService;
import net.dreamlu.mica.admin.project.im.vo.GroupCreateForm;
import net.dreamlu.mica.admin.project.im.vo.GroupMemberForm;
import net.dreamlu.mica.admin.project.im.vo.GroupMemberVO;
import net.dreamlu.mica.admin.project.im.vo.GroupVO;
import net.dreamlu.mica.admin.project.system.entity.SysUser;
import net.dreamlu.mica.admin.project.system.service.ISysUserService;
import net.dreamlu.mica.core.result.R;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.*;
import java.util.stream.Collectors;

/**
 * IM 群基础接口（PR-1.1.1 读 + PR-1.1.2 写）。
 *
 * @author L.cm
 */
@Tag(name = "IM：群组")
@RestController
@RequestMapping("/api/im/groups")
@RequiredArgsConstructor
public class ImGroupController extends BaseController {

	private final IImGroupService groupService;
	private final ImGroupMemberMapper groupMemberMapper;
	private final ISysUserService userService;

	/**
	 * 列出我加入的所有群（含部门群）。
	 */
	@Operation(summary = "我加入的群列表")
	@GetMapping("/my")
	public List<GroupVO> myGroups(AuthUser authUser) {
		Long me = authUser.getUserId();
		List<ImGroup> groups = groupService.listMyGroups(me);
		if (groups.isEmpty()) {
			return Collections.emptyList();
		}
		// 批量查当前用户在各群的群成员记录（拿 role / nickname）
		List<ImGroupMember> myMemberships = groupMemberMapper.selectByUserId(me);
		Map<Long, ImGroupMember> memberMap = myMemberships.stream()
			.collect(Collectors.toMap(ImGroupMember::getGroupId, m -> m, (a, b) -> a));
		// 批量查群主昵称
		Set<Long> ownerIds = groups.stream().map(ImGroup::getOwnerId).collect(Collectors.toSet());
		Map<Long, String> ownerNameMap = lookupUserNameMap(ownerIds);

		List<GroupVO> result = new ArrayList<>(groups.size());
		for (ImGroup g : groups) {
			GroupVO vo = new GroupVO();
			vo.setId(g.getId());
			vo.setName(g.getName());
			vo.setAvatar(g.getAvatar());
			vo.setType(g.getType());
			vo.setOwnerId(g.getOwnerId());
			vo.setOwnerName(ownerNameMap.get(g.getOwnerId()));
			vo.setDeptId(g.getDeptId());
			vo.setAnnouncement(g.getAnnouncement());
			vo.setMemberCount(g.getMemberCount());
			vo.setMaxMembers(g.getMaxMembers());
			ImGroupMember mine = memberMap.get(g.getId());
			if (mine != null) {
				vo.setRole(mine.getRole());
				vo.setNickname(mine.getNickname());
			}
			result.add(vo);
		}
		return result;
	}

	/**
	 * 群详情。
	 */
	@Operation(summary = "群详情")
	@GetMapping("/{groupId}")
	public GroupVO detail(@PathVariable Long groupId, AuthUser authUser) {
		Long me = authUser.getUserId();
		if (!groupService.isMember(groupId, me)) {
			R.throwFail(net.dreamlu.mica.admin.project.im.common.ImApiCode.GROUP_NOT_MEMBER);
		}
		ImGroup g = groupService.getDetail(groupId);
		ImGroupMember mine = groupMemberMapper.selectByGroupId(groupId).stream()
			.filter(m -> m.getUserId().equals(me))
			.findFirst().orElse(null);
		GroupVO vo = new GroupVO();
		vo.setId(g.getId());
		vo.setName(g.getName());
		vo.setAvatar(g.getAvatar());
		vo.setType(g.getType());
		vo.setOwnerId(g.getOwnerId());
		vo.setOwnerName(lookupUserNameMap(Collections.singleton(g.getOwnerId())).get(g.getOwnerId()));
		vo.setDeptId(g.getDeptId());
		vo.setAnnouncement(g.getAnnouncement());
		vo.setMemberCount(g.getMemberCount());
		vo.setMaxMembers(g.getMaxMembers());
		if (mine != null) {
			vo.setRole(mine.getRole());
			vo.setNickname(mine.getNickname());
		}
		return vo;
	}

	/**
	 * 群成员列表（按加入时间正序）。
	 */
	@Operation(summary = "群成员列表")
	@GetMapping("/{groupId}/members")
	public Map<String, Object> listMembers(@PathVariable Long groupId, AuthUser authUser) {
		if (!groupService.isMember(groupId, authUser.getUserId())) {
			R.throwFail(net.dreamlu.mica.admin.project.im.common.ImApiCode.GROUP_NOT_MEMBER);
		}
		List<ImGroupMember> members = groupService.listMembers(groupId);
		if (members.isEmpty()) {
			Map<String, Object> data = new HashMap<>(2);
			data.put("total", 0);
			data.put("list", Collections.emptyList());
			return data;
		}
		Set<Long> userIds = members.stream().map(ImGroupMember::getUserId).collect(Collectors.toSet());
		Map<Long, SysUser> userMap = userService.listByIds(userIds).stream()
			.collect(Collectors.toMap(SysUser::getId, u -> u));
		List<GroupMemberVO> voList = new ArrayList<>(members.size());
		for (ImGroupMember m : members) {
			GroupMemberVO vo = new GroupMemberVO();
			vo.setGroupId(m.getGroupId());
			vo.setUserId(m.getUserId());
			vo.setRole(m.getRole());
			vo.setNickname(m.getNickname());
			vo.setJoinedAt(m.getJoinedAt());
			SysUser u = userMap.get(m.getUserId());
			if (u != null) {
				vo.setUserName(u.getUserName());
				vo.setUserNickName(u.getNickName());
				vo.setAvatar(u.getAvatar());
			}
			voList.add(vo);
		}
		Map<String, Object> data = new HashMap<>(2);
		data.put("total", voList.size());
		data.put("list", voList);
		return data;
	}

	/**
	 * 批量查询 sys_user 的展示名（昵称 > 用户名）。
	 */
	private Map<Long, String> lookupUserNameMap(java.util.Collection<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) {
			return Collections.emptyMap();
		}
		return userService.listByIds(userIds).stream()
			.collect(Collectors.toMap(
				SysUser::getId,
				u -> u.getNickName() != null && !u.getNickName().isEmpty() ? u.getNickName() : u.getUserName(),
				(a, b) -> a));
	}

	// ==================== 写接口（PR-1.1.2）====================

	/**
	 * 创建群组（普通群 / 部门群由系统同步创建）。
	 */
	@Operation(summary = "创建群组")
	@PostMapping
	public Map<String, Object> createGroup(@Valid @RequestBody GroupCreateForm form, AuthUser authUser) {
		ImGroup group = groupService.createGroup(
			authUser.getUserId(),
			form.getName(),
			form.getAvatar(),
			form.getType(),
			form.getAnnouncement(),
			form.getMemberIds());
		Map<String, Object> data = new HashMap<>(2);
		data.put("groupId", group.getId());
		data.put("name", group.getName());
		data.put("memberCount", group.getMemberCount());
		return data;
	}

	/**
	 * 邀请成员入群。
	 */
	@Operation(summary = "邀请成员入群")
	@PostMapping("/{groupId}/members")
	public Map<String, Object> addMembers(@PathVariable Long groupId,
										  @Valid @RequestBody GroupMemberForm form,
										  AuthUser authUser) {
		int added = groupService.addMembers(groupId, authUser.getUserId(), form.getUserIds());
		Map<String, Object> data = new HashMap<>(2);
		data.put("groupId", groupId);
		data.put("added", added);
		return data;
	}

	/**
	 * 移除指定成员（群主 / 管理员可操作）。
	 */
	@Operation(summary = "移除成员")
	@DeleteMapping("/{groupId}/members/{userId}")
	public Map<String, Object> removeMember(@PathVariable Long groupId,
											@PathVariable Long userId,
											AuthUser authUser) {
		groupService.removeMember(groupId, authUser.getUserId(), userId);
		Map<String, Object> data = new HashMap<>(2);
		data.put("groupId", groupId);
		data.put("removedUserId", userId);
		return data;
	}

	/**
	 * 主动退群。
	 */
	@Operation(summary = "主动退群")
	@DeleteMapping("/{groupId}/members")
	public Map<String, Object> quitGroup(@PathVariable Long groupId, AuthUser authUser) {
		groupService.quitGroup(groupId, authUser.getUserId());
		Map<String, Object> data = new HashMap<>(2);
		data.put("groupId", groupId);
		data.put("userId", authUser.getUserId());
		return data;
	}

	/**
	 * 解散群（仅群主）。
	 */
	@Operation(summary = "解散群")
	@DeleteMapping("/{groupId}")
	public Map<String, Object> dismissGroup(@PathVariable Long groupId, AuthUser authUser) {
		groupService.dismissGroup(groupId, authUser.getUserId());
		Map<String, Object> data = new HashMap<>(2);
		data.put("groupId", groupId);
		return data;
	}
}
