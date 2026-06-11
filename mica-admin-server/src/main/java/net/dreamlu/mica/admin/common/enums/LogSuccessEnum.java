package net.dreamlu.mica.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志成功状态枚举：0失败 1成功
 *
 * @author L.cm
 */
@Getter
@AllArgsConstructor
public enum LogSuccessEnum implements ValueLabelEnum {

	FAIL(0, "失败"),
	SUCCESS(1, "成功");

	private final int value;
	private final String label;

	/** EasyExcel 转换器 */
	public static class Converter extends EnumExcelConverter<LogSuccessEnum> {
		public Converter() {
			super(LogSuccessEnum.class);
		}
	}
}
