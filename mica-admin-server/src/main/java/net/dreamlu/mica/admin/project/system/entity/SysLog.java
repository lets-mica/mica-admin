package net.dreamlu.mica.admin.project.system.entity;

import org.apache.fesod.sheet.annotation.ExcelIgnore;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import net.dreamlu.mica.admin.common.enums.LogSuccessEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 系统日志
 * </p>
 *
 * @author L.cm
 * @since 2020-07-09
 */
@Data
public class SysLog implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	@ExcelIgnore
	private Long id;
	/**
	 * 用户id
	 */
	@ExcelIgnore
	private Long userId;
	/**
	 * 登陆名
	 */
	@ExcelProperty(value = "用户名", index = 0)
	@ColumnWidth(16)
	private String userName;
	/**
	 * 日志类别
	 */
	@ExcelProperty(value = "日志类别", index = 1)
	@ColumnWidth(14)
	private String logType;
	/**
	 * 描述
	 */
	@ExcelProperty(value = "描述", index = 2)
	@ColumnWidth(24)
	private String description;
	/**
	 * url 请求参数
	 */
	@ExcelIgnore
	private String params;
	/**
	 * post data
	 */
	@ExcelIgnore
	private String data;
	/**
	 * 是否成功[0失败,1成功]
	 */
	@ExcelProperty(value = "是否成功", index = 3, converter = LogSuccessEnum.Converter.class)
	@ColumnWidth(12)
	private Boolean successful;
	/**
	 * 类-方法
	 */
	@ExcelProperty(value = "类-方法", index = 4)
	@ColumnWidth(30)
	private String classMethod;
	/**
	 * 异常信息
	 */
	@ExcelProperty(value = "异常信息", index = 5)
	@ColumnWidth(30)
	private String exceptionDetail;
	/**
	 * 请求ip
	 */
	@ExcelProperty(value = "请求IP", index = 6)
	@ColumnWidth(16)
	private String requestIp;
	/**
	 * 请求耗时
	 */
	@ExcelProperty(value = "请求耗时(ms)", index = 7)
	@ColumnWidth(14)
	private Long requestTime;
	/**
	 * 系统
	 */
	@ExcelProperty(value = "操作系统", index = 8)
	@ColumnWidth(16)
	private String os;
	/**
	 * 浏览器
	 */
	@ExcelProperty(value = "浏览器", index = 9)
	@ColumnWidth(16)
	private String browser;
	/**
	 * 请求者地址
	 */
	@ExcelProperty(value = "请求地址", index = 10)
	@ColumnWidth(16)
	private String address;
	/**
	 * 创建时间
	 */
	@ExcelProperty(value = "创建时间", index = 11)
	@ColumnWidth(20)
	@TableField(value = "created_at", fill = FieldFill.INSERT)
	private LocalDateTime createdAt;

}
