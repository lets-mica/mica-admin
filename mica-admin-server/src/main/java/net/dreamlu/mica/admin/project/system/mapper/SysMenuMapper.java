package net.dreamlu.mica.admin.project.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.dreamlu.mica.admin.project.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 菜单权限表 Mapper 接口
 * </p>
 *
 * @author L.cm
 * @since 2020-07-10
 */
@Mapper
@InterceptorIgnore
public interface SysMenuMapper extends BaseMapper<SysMenu> {

}
