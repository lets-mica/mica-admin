package net.dreamlu.mica.admin.project.im.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.admin.project.im.entity.ImGroupMember;
import net.dreamlu.mica.admin.project.im.mapper.ImGroupMemberMapper;
import net.dreamlu.mica.admin.project.im.service.IImGroupMemberService;
import org.springframework.stereotype.Service;

/**
 * IM 群成员服务实现。
 *
 * @author L.cm
 */
@Service
public class ImGroupMemberServiceImpl
	extends ServiceImpl<ImGroupMemberMapper, ImGroupMember>
	implements IImGroupMemberService {
}
