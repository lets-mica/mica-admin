package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;
import net.dreamlu.mica.core.validation.UpdateGroup;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色创建更新实体
 *
 * @author L.cm
 */
@Data
public class RoleReq {

	/**
	 * 主键 id
	 */
	@NotNull(groups = UpdateGroup.class)
	private Long id;
	/**
	 * 角色名称
	 */
	@NotBlank
	private String name;
	/**
	 * 角色权限字符串
	 */
	@NotBlank
	private String title;
	/**
	 * 显示顺序
	 */
	@NotNull
	private Integer seq;
	/**
	 * 数据范围（1：全部 2：部门 3：自定义）
	 */
	@NotNull
	@Range(min = 1, max = 3)
	private Integer dataScope;
	/**
	 * 自定义数据权限部门 id 集合
	 */
	private List<Long> depts;
	/**
	 * 备注
	 */
	private String remark;

}
