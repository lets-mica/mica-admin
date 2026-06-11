package net.dreamlu.mica.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 启用状态枚举：0停用 1正常
 *
 * @author L.cm
 */
@Getter
@AllArgsConstructor
public enum EnabledEnum implements ValueLabelEnum {

	DISABLED(0, "停用"),
	ENABLED(1, "正常");

	private final int value;
	private final String label;

	/** EasyExcel 转换器 */
	public static class Converter extends EnumExcelConverter<EnabledEnum> {
		public Converter() {
			super(EnabledEnum.class);
		}
	}
}
