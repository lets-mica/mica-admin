package net.dreamlu.mica.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户性别枚举：0男 1女 2未知
 *
 * @author L.cm
 */
@Getter
@AllArgsConstructor
public enum GenderEnum implements ValueLabelEnum {

	MALE(0, "男"),
	FEMALE(1, "女"),
	UNKNOWN(2, "未知");

	private final int value;
	private final String label;

	/** EasyExcel 转换器 */
	public static class Converter extends EnumExcelConverter<GenderEnum> {
		public Converter() {
			super(GenderEnum.class);
		}
	}
}
