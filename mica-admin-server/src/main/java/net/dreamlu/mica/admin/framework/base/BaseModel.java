package net.dreamlu.mica.admin.framework.base;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import net.dreamlu.mica.core.utils.DatePattern;
import net.dreamlu.mica.core.validation.CreateGroup;
import net.dreamlu.mica.core.validation.UpdateGroup;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 抽象模型
 *
 * @author L.cm
 */
@Getter
@Setter
public class BaseModel implements Serializable {
	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	@Null(groups = CreateGroup.class)
	@NotNull(groups = UpdateGroup.class)
	@ExcelIgnore
	private Long id;
	/**
	 * 创建者
	 */
	@TableField(value = "created_by", fill = FieldFill.INSERT)
	@ExcelIgnore
	private String createdBy;
	/**
	 * 创建时间
	 */
	@TableField(value = "created_at", fill = FieldFill.INSERT)
	@ExcelIgnore
	@JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
	@DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
	private LocalDateTime createdAt;
	/**
	 * 更新者
	 */
	@TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
	@ExcelIgnore
	private String updatedBy;
	/**
	 * 更新时间
	 */
	@TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
	@ExcelIgnore
	@JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
	@DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
	private LocalDateTime updatedAt;
}
