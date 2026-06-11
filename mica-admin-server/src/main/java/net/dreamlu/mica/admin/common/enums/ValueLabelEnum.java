package net.dreamlu.mica.admin.common.enums;

/**
 * 值-标签 枚举接口
 * <p>
 * 所有需要导出中文标签的枚举都应实现此接口，
 * 配合 {@link net.dreamlu.mica.admin.common.enums.EnumExcelConverter} 使用。
 * </p>
 *
 * @author L.cm
 */
public interface ValueLabelEnum {

	/**
	 * 枚举对应的数值
	 */
	int getValue();

	/**
	 * 枚举对应的中文标签
	 */
	String getLabel();
}
