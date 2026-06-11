package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息查询
 *
 * @author L.cm
 */
@Data
public class MessageQuery {

	private String title;
	private String category;
	private Boolean enabled;
	private List<LocalDateTime> createTime;

}
