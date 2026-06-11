package net.dreamlu.mica.admin.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.core.result.IResultCode;

/**
 * api code
 *
 * @author L.cm
 */
@Getter
@RequiredArgsConstructor
public enum ApiCode implements IResultCode {

	/**
	 * 接口 code 码
	 */
	USER_NAME_ID_BLANK(20001, "用户名不能为空"),
	USER_ALREADY_EXISTS(20002, "用户已经存在"),
	DEMO_BLOCK(20003, "demo演示，禁用更新、删除"),

	/**
	 * 任务 code 码
	 */
	JOBS_START_FAIL(20010, "任务调度器启动失败"),
	JOBS_ADD_FAIL(20011, "任务添加失败"),
	JOBS_DELETE_FAIL(20012, "任务删除失败"),
	JOBS_PAUSE_FAIL(20013, "任务暂停失败"),
	JOBS_RESUME_FAIL(20014, "任务恢复失败"),
	JOBS_CRON_FAIL(20015, "任务 cron 修改失败"),
	;

	private final int code;
	private final String msg;

}
