package net.dreamlu.mica.admin.project.system.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.dreamlu.mica.admin.project.system.job.entity.SysJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 数据库驱动定时任务 Mapper 接口
 * </p>
 *
 * @author L.cm
 */
@Mapper
public interface SysJobMapper extends BaseMapper<SysJob> {

}
