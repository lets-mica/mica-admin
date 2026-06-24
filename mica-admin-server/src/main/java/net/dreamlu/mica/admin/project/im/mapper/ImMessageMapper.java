package net.dreamlu.mica.admin.project.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.dreamlu.mica.admin.project.im.entity.ImMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * IM 消息 Mapper。
 *
 * @author L.cm
 */
@Mapper
public interface ImMessageMapper extends BaseMapper<ImMessage> {
}