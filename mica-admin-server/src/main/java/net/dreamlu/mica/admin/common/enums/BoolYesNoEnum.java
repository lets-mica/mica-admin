package net.dreamlu.mica.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 布尔 是/否 枚举：0否 1是
 *
 * @author L.cm
 */
@Getter
@AllArgsConstructor
public enum BoolYesNoEnum implements ValueLabelEnum {

	NO(0, "否"),
	YES(1, "是");

	private final int value;
	private final String label;

	/** EasyExcel 转换器 */
	public static class Converter extends EnumExcelConverter<BoolYesNoEnum> {
		public Converter() {
			super(BoolYesNoEnum.class);
		}
	}
}
