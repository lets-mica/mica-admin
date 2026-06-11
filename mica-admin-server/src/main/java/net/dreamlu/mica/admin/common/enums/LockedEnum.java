package net.dreamlu.mica.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 锁定状态枚举：0正常 1锁定
 *
 * @author L.cm
 */
@Getter
@AllArgsConstructor
public enum LockedEnum implements ValueLabelEnum {

	NORMAL(0, "正常"),
	LOCKED(1, "锁定");

	private final int value;
	private final String label;

	/** EasyExcel 转换器 */
	public static class Converter extends EnumExcelConverter<LockedEnum> {
		public Converter() {
			super(LockedEnum.class);
		}
	}
}
