package net.dreamlu.mica.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单类型枚举：0目录 1菜单 2按钮
 *
 * @author L.cm
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum implements ValueLabelEnum {

	DIRECTORY(0, "目录"),
	MENU(1, "菜单"),
	BUTTON(2, "按钮");

	private final int value;
	private final String label;

	/** EasyExcel 转换器 */
	public static class Converter extends EnumExcelConverter<MenuTypeEnum> {
		public Converter() {
			super(MenuTypeEnum.class);
		}
	}
}
