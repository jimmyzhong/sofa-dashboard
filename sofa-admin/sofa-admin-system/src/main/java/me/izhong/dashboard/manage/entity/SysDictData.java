package me.izhong.dashboard.manage.entity;

import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.izhong.common.annotation.AutoId;
import me.izhong.common.annotation.Excel;
import me.izhong.common.annotation.PrimaryId;
import me.izhong.common.annotation.Search;
import me.izhong.dashboard.manage.constants.UserConstants;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_dict_data")
@Data
public class SysDictData extends TimedBasedEntity {

    @PrimaryId
    @Indexed(unique = true)
    @AutoId
    @Excel(name = "字典编码", cellType = Excel.ColumnType.NUMERIC)
    private Long dictCode;

    @Excel(name = "字典排序")
    private Long dictSort;

    @Excel(name = "字典标签")
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "字典标签不能为空")
    @Size(min = 0, max = 100, message = "字典标签长度不能超过100个字符")
    private String dictLabel;

    @Excel(name = "字典键值")
    @Search
    @NotBlank(message = "字典键值不能为空")
    @Size(min = 0, max = 100, message = "字典键值长度不能超过100个字符")
    private String dictValue;

    @Excel(name = "字典类型")
    @Search
    @NotBlank(message = "字典类型不能为空")
    @Size(min = 0, max = 100, message = "字典类型长度不能超过100个字符")
    private String dictType;

    @Excel(name = "字典样式")
    @Size(min = 0, max = 100, message = "样式属性长度不能超过100个字符")
    private String cssClass;

    /**
     * 表格字典样式
     */
    private String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    @Excel(name = "是否默认", readConverterExp = "Y=是,N=否")
    private String isDefault;

    /**
     * 状态（0正常 1停用）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    @Search
    private String status;

    public boolean getDefault() {
        return UserConstants.YES.equals(this.isDefault) ? true : false;
    }
}
