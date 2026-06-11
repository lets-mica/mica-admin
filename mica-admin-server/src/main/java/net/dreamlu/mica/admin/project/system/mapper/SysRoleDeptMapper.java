package net.dreamlu.mica.admin.project.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.dreamlu.mica.admin.project.system.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 角色和部门关联表 Mapper 接口
 * </p>
 *
 * @author L.cm
 * @since 2020-07-10
 */
@Mapper
@InterceptorIgnore
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {

}
