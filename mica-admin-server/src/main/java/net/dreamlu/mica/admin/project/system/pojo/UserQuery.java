package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户查询
 *
 * @author L.cm
 */
@Data
public class UserQuery implements Serializable {

	/**
	 * 模糊搜索，email,username,nickName
	 */
	private String blurry;
	private Boolean enabled;
	private Long deptId;
	private Set<Long> deptIds = new HashSet<>();
	private List<LocalDateTime> createTime;

}
