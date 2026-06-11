package net.dreamlu.mica.admin.project.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import net.dreamlu.mica.admin.project.system.entity.SysDept;
import net.dreamlu.mica.admin.project.system.pojo.DeptQuery;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 部门表 服务类
 * </p>
 *
 * @author L.cm
 * @since 2020-05-05
 */
public interface ISysDeptService extends IService<SysDept> {

	/**
	 * 根据 query 组装查询条件
	 *
	 * @param query DeptQuery
	 * @return Wrapper
	 */
	Wrapper<SysDept> getQueryWrapper(DeptQuery query);

	/**
	 * 获取所有的父类
	 *
	 * @param deptList     deptList
	 * @param superiorList 父类
	 * @return deptList
	 */
	List<SysDept> getSuperior(List<SysDept> deptList, List<SysDept> superiorList);

	/**
	 * 获取所有的子部门
	 *
	 * @param deptIdList   部门id列表
	 * @param childrenList 子部门
	 * @return deptList
	 */
	List<SysDept> getChildren(List<Long> deptIdList, List<SysDept> childrenList);

	/**
	 * 如果没用使用时删除
	 *
	 * @param ids id集合
	 * @return 是否成功
	 */
	boolean deleteIfUnusedByIds(Collection<Long> ids);
}
