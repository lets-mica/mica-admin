package net.dreamlu.mica.admin.project.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.dreamlu.mica.admin.project.im.entity.ImGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * IM 群 Mapper。
 *
 * @author L.cm
 */
@Mapper
public interface ImGroupMapper extends BaseMapper<ImGroup> {

	/**
	 * 列出某用户参与的所有群（含部门群），按最近消息时间倒序。
	 * <p>
	 * 实现：{@code im_group_member JOIN im_group}，按 {@code im_group.updated_at} 排序
	 * （Phase 1.1 暂未在群表中冗余最后消息时间，使用更新时间作为粗略排序；PR-1.1.3 群消息收发时
	 * 可在 {@code im_group} 加 {@code last_msg_time} 列）。
	 *
	 * @param userId 用户 id
	 * @return 群列表（最多 200 条）
	 */
	List<ImGroup> selectMyGroups(@Param("userId") Long userId);

	/**
	 * 按部门 id 查找关联的部门群（每个部门最多对应一个部门群）。
	 *
	 * @param deptId 部门 id
	 * @return 群实体，无则 null
	 */
	ImGroup selectByDeptId(@Param("deptId") Long deptId);

	/**
	 * 原子自增成员数（{@code member_count = member_count + delta}）。
	 *
	 * @param groupId 群 id
	 * @param delta   增量（可为负数）
	 * @return 影响行数
	 */
	int incrMemberCount(@Param("groupId") Long groupId, @Param("delta") int delta);
}