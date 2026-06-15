package net.dreamlu.mica.admin.common.enums;

import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.enums.CellDataTypeEnum;
import org.apache.fesod.sheet.metadata.GlobalConfiguration;
import org.apache.fesod.sheet.metadata.data.ReadCellData;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.metadata.property.ExcelContentProperty;

/**
 * 通用枚举 Excel 转换器
 * <p>
 * 将实现了 {@link ValueLabelEnum} 的枚举与 Excel 中文标签互相转换。
 * <br>
 * 用法：
 * <pre>{@code
 * @ExcelProperty(value = "性别", converter = GenderEnum.Converter.class)
 * private Integer gender;
 * }</pre>
 * </p>
 *
 * @param <E> 枚举类型
 * @author L.cm
 */
public abstract class EnumExcelConverter<E extends Enum<E> & ValueLabelEnum> implements Converter<Object> {

	private final Class<E> enumClass;

	protected EnumExcelConverter(Class<E> enumClass) {
		this.enumClass = enumClass;
	}

	@Override
	public Class<?> supportJavaTypeKey() {
		return Object.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}

	/**
	 * Excel 中文标签 → Java 数值
	 */
	@Override
	public Object convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
	                                GlobalConfiguration globalConfiguration) {
		String label = cellData.getStringValue();
		for (E e : enumClass.getEnumConstants()) {
			if (e.getLabel().equals(label)) {
				return e.getValue();
			}
		}
		return 0;
	}

	/**
	 * Java 数值 → Excel 中文标签
	 */
	@Override
	public WriteCellData<?> convertToExcelData(Object value, ExcelContentProperty contentProperty,
												GlobalConfiguration globalConfiguration) {
		if (value == null) {
			return new WriteCellData<>("");
		}
		String strValue = String.valueOf(value);
		// 对 Boolean 字段，true→1, false→0
		if (value instanceof Boolean) {
			strValue = (Boolean) value ? "1" : "0";
		}
		int intValue = Integer.parseInt(strValue);
		for (E e : enumClass.getEnumConstants()) {
			if (e.getValue() == intValue) {
				return new WriteCellData<>(e.getLabel());
			}
		}
		return new WriteCellData<>(strValue);
	}
}
