package net.dreamlu.mica.admin.project.system.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知查询
 *
 * @author L.cm
 */
@Data
public class NoticeQuery {

	private String title;
	private String createBy;
	private Integer type;
	private List<LocalDateTime> createTime;

}
