package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色查询
 *
 * @author L.cm
 */
@Data
public class RoleQuery {

	/**
	 * 模糊查询 name,description
	 */
	private String blurry;
	private List<LocalDateTime> createTime;

}
