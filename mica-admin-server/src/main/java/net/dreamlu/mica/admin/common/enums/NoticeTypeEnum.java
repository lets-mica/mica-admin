package net.dreamlu.mica.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知公告类型枚举：1通知 2公告
 *
 * @author L.cm
 */
@Getter
@AllArgsConstructor
public enum NoticeTypeEnum implements ValueLabelEnum {

	NOTICE(1, "通知"),
	ANNOUNCEMENT(2, "公告");

	private final int value;
	private final String label;

	/** EasyExcel 转换器 */
	public static class Converter extends EnumExcelConverter<NoticeTypeEnum> {
		public Converter() {
			super(NoticeTypeEnum.class);
		}
	}
}
