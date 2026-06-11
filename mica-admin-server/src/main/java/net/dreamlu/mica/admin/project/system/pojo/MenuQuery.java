package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单查询
 *
 * @author L.cm
 */
@Data
public class MenuQuery {

	private Long pid;
	/**
	 * 模糊查询 title,component,permission
	 */
	private String blurry;
	private List<LocalDateTime> createTime;

}
