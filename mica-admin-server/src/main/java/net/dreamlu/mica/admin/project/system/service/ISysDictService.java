package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysDict;
import net.dreamlu.mica.admin.project.system.pojo.DictQuery;

import java.util.Collection;

/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-07-19
 */
public interface ISysDictService extends IService<SysDict> {

	/**
	 * 根据 query 组装查询条件
	 *
	 * @param query DeptQuery
	 * @return Wrapper
	 */
	Wrapper<SysDict> getQueryWrapper(DictQuery query);

	/**
	 * 如果没有试用删除
	 *
	 * @param ids id集合
	 * @return 是否成功
	 */
	boolean deleteIfUnusedByIds(Collection<Long> ids);
}
