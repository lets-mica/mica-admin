package net.dreamlu.mica.admin.project.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.project.im.entity.ImConversationMember;
import net.dreamlu.mica.admin.project.im.mapper.ImConversationMemberMapper;
import net.dreamlu.mica.admin.project.im.service.IImConversationMemberService;
import org.springframework.stereotype.Service;

/**
 * IM 会话成员服务实现。
 *
 * @author L.cm
 */
@Service
public class ImConversationMemberServiceImpl extends ServiceImpl<ImConversationMemberMapper, ImConversationMember>
	implements IImConversationMemberService {
}