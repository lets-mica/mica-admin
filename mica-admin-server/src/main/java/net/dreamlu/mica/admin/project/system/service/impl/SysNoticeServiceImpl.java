package net.dreamlu.mica.admin.project.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.admin.project.system.mapper.SysNoticeMapper;
import net.dreamlu.mica.admin.project.system.entity.SysNotice;
import net.dreamlu.mica.admin.project.system.pojo.NoticeQuery;
import net.dreamlu.mica.admin.project.system.service.ISysNoticeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 通知公告表 服务实现类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

	@Override
	public Wrapper<SysNotice> getQueryWrapper(NoticeQuery query) {
		LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
		wrapper.like(StringUtil.isNotBlank(query.getTitle()), SysNotice::getTitle, query.getTitle());
		wrapper.like(StringUtil.isNotBlank(query.getCreateBy()), SysNotice::getCreatedBy, query.getCreateBy());
		wrapper.eq(query.getType() != null, SysNotice::getType, query.getType());
		List<LocalDateTime> createTime = query.getCreateTime();
		if (createTime != null && createTime.size() > 1) {
			wrapper.between(SysNotice::getCreatedAt, createTime.get(0), createTime.get(1));
		}
		return wrapper;
	}

}
