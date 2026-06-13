package net.dreamlu.mica.admin.project.system.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dreamlu.mica.admin.common.enums.EnabledEnum;
import net.dreamlu.mica.admin.framework.base.BaseModel;

/**
 * <p>
 * 岗位信息表
 * </p>
 *
 * @author L.cm
 * @since 2020-07-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPost extends BaseModel {
    private static final long serialVersionUID=1L;

    /**
     * 岗位编码
     */
    @ExcelProperty(value = "岗位编码", index = 0)
    @ColumnWidth(16)
    private String code;
    /**
     * 岗位名称
     */
    @ExcelProperty(value = "岗位名称", index = 1)
    @ColumnWidth(16)
    private String name;
    /**
     * 显示顺序
     */
    @ExcelProperty(value = "显示顺序", index = 2)
    @ColumnWidth(12)
    private Integer seq;
    /**
     * 状态（0停用,1正常）
     */
    @ExcelProperty(value = "状态", index = 3, converter = EnabledEnum.Converter.class)
    @ColumnWidth(10)
    private Integer enabled;
    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 4)
    @ColumnWidth(24)
    private String remark;

}
