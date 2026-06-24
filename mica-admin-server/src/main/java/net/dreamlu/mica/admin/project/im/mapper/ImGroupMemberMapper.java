package net.dreamlu.mica.admin.project.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.dreamlu.mica.admin.project.im.entity.ImGroupMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * IM 群成员 Mapper。
 *
 * @author L.cm
 */
@Mapper
public interface ImGroupMemberMapper extends BaseMapper<ImGroupMember> {

	/**
	 * 列出某群的全部成员（含 owner/admin/member）。
	 *
	 * @param groupId 群 id
	 * @return 群成员列表
	 */
	List<ImGroupMember> selectByGroupId(@Param("groupId") Long groupId);

	/**
	 * 列出某用户加入的全部群成员记录（用于"我的群"列表关联）。
	 *
	 * @param userId 用户 id
	 * @return 群成员列表
	 */
	List<ImGroupMember> selectByUserId(@Param("userId") Long userId);

	/**
	 * 统计某群当前成员数。
	 *
	 * @param groupId 群 id
	 * @return 成员数
	 */
	long countByGroupId(@Param("groupId") Long groupId);
}