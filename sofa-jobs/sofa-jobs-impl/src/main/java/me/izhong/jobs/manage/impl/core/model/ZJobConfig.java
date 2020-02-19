package me.izhong.jobs.manage.impl.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.izhong.common.annotation.AutoId;
import me.izhong.common.annotation.Excel;
import me.izhong.common.annotation.PrimaryId;
import me.izhong.common.annotation.Search;
import me.izhong.db.common.domain.TimedBasedEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_config")
@Data
public class ZJobConfig extends TimedBasedEntity {

    /**
     * 参数主键
     */
    @PrimaryId
    @Excel(name = "参数主键", cellType = Excel.ColumnType.NUMERIC)
    @Search
    @AutoId
    @Indexed(unique = true)
    private Long configId;

    /**
     * 参数名称
     */
    @Excel(name = "参数名称")
    @Search
    @NotBlank(message = "参数名称不能为空")
    @Size(min = 0, max = 100, message = "参数名称不能超过100个字符")
    private String configName;

    @Indexed(unique = true)
    /** 参数键名 */
    @Excel(name = "参数键名")
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "参数键名长度不能为空")
    @Size(min = 0, max = 100, message = "参数键名长度不能超过100个字符")
    private String configKey;

    /**
     * 参数键值
     */
    @Excel(name = "参数键值")
    @Search
    @NotBlank(message = "参数键值不能为空")
    @Size(min = 0, max = 500, message = "参数键值长度不能超过500个字符")
    private String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    @Excel(name = "系统内置", readConverterExp = "Y=是,N=否")
    @Search
    @Size(min = 0, max = 1, message = "类型不能超过1个字符")
    private String configType;
}
