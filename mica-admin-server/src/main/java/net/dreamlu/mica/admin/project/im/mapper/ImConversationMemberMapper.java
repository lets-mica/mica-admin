package net.dreamlu.mica.admin.project.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.dreamlu.mica.admin.project.im.entity.ImConversationMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * IM 会话成员 Mapper。
 *
 * @author L.cm
 */
@Mapper
public interface ImConversationMemberMapper extends BaseMapper<ImConversationMember> {
}