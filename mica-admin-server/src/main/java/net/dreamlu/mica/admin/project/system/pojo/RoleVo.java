package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色创建更新实体
 *
 * @author L.cm
 */
@Data
public class RoleVo {

	/**
	 * 主键 id
	 */
	private Long id;
	/**
	 * 角色名称
	 */
	private String name;
	/**
	 * 角色权限字符串
	 */
	private String title;
	/**
	 * 显示顺序
	 */
	private Integer seq;
	/**
	 * 数据范围（1：全部 2：部门 3：自定义）
	 */
	private Integer dataScope;
	/**
	 * 自定义数据权限部门 id 集合
	 */
	private List<Long> depts;
	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 创建者
	 */
	private String createdBy;
	/**
	 * 创建时间
	 */
	private LocalDateTime createdAt;
	/**
	 * 更新者
	 */
	private String updatedBy;
	/**
	 * 更新时间
	 */
	private LocalDateTime updatedAt;

}
