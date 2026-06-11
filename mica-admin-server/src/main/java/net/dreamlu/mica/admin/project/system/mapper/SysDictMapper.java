package net.dreamlu.mica.admin.project.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.dreamlu.mica.admin.project.system.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 字典表 Mapper 接口
 * </p>
 *
 * @author L.cm
 * @since 2020-07-19
 */
@Mapper
@InterceptorIgnore
public interface SysDictMapper extends BaseMapper<SysDict> {

}
