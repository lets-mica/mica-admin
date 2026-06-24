package net.dreamlu.mica.admin.project.im.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创建/获取单聊会话请求。
 *
 * @author L.cm
 */
@Data
@Schema(description = "创建/获取单聊会话请求")
public class P2pConversationForm {

	/**
	 * 对端 userId
	 */
	@NotNull
	@Schema(description = "对端 userId")
	private Long peerUserId;
}