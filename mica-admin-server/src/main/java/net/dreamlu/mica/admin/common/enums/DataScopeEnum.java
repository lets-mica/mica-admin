package net.dreamlu.mica.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限枚举：1全部 2部门 3自定义
 *
 * @author L.cm
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum implements ValueLabelEnum {

	ALL(1, "全部"),
	DEPT(2, "部门"),
	CUSTOM(3, "自定义");

	private final int value;
	private final String label;

	/** EasyExcel 转换器 */
	public static class Converter extends EnumExcelConverter<DataScopeEnum> {
		public Converter() {
			super(DataScopeEnum.class);
		}
	}
}
