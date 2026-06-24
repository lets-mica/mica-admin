package net.dreamlu.mica.admin.project.im.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.core.result.IResultCode;

/**
 * IM 模块业务错误码。
 * <p>
 * 错误码段：21000-21999。Phase 0 / Phase 1 全部使用 21xxx 段。
 *
 * @author L.cm
 */
@Getter
@RequiredArgsConstructor
public enum ImApiCode implements IResultCode {

	/**
	 * 通用错误
	 */
	MQTT_INVALID_TOKEN(21001, "MQTT 连接 JWT 无效"),
	MQTT_NOT_AUTHENTICATED(21002, "MQTT 连接未鉴权"),

	/**
	 * 单聊 (Phase 1)
	 */
	CONVERSATION_NOT_FOUND(21010, "会话不存在"),
	CONVERSATION_NOT_MEMBER(21011, "用户不是会话成员"),
	MESSAGE_EMPTY(21012, "消息内容为空"),
	MESSAGE_TOO_LONG(21013, "消息过长"),

	/**
	 * 群聊 (Phase 1.1)
	 */
	GROUP_NOT_FOUND(21020, "群组不存在"),
	GROUP_NOT_MEMBER(21021, "用户不是群成员"),
	GROUP_ALREADY_MEMBER(21022, "用户已经是群成员"),
	GROUP_FULL(21023, "群成员已满"),

	/**
	 * 系统推送 (Phase 0 PR-0.3)
	 */
	PUSH_FAIL(21030, "消息推送失败"),
	;

	private final int code;
	private final String msg;
}