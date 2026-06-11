package net.dreamlu.mica.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举：0正常 1停用
 *
 * @author L.cm
 */
@Getter
@AllArgsConstructor
public enum StatusEnum implements ValueLabelEnum {

	NORMAL(0, "正常"),
	DISABLED(1, "停用");

	private final int value;
	private final String label;

	/** EasyExcel 转换器 */
	public static class Converter extends EnumExcelConverter<StatusEnum> {
		public Converter() {
			super(StatusEnum.class);
		}
	}
}
