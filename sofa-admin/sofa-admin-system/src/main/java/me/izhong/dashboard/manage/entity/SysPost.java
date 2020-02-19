package me.izhong.dashboard.manage.entity;

import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.izhong.common.annotation.AutoId;
import me.izhong.common.annotation.Excel;
import me.izhong.common.annotation.PrimaryId;
import me.izhong.common.annotation.Search;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_post")
@Data
public class SysPost extends TimedBasedEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 岗位序号
     */
    @Excel(name = "岗位序号", cellType = Excel.ColumnType.NUMERIC)
    @AutoId
    @PrimaryId
    @Indexed(unique = true)
    private Long postId;

    /**
     * 岗位编码
     */
    @Search
    @Excel(name = "岗位编码")
    private String postCode;

    /**
     * 岗位名称
     */
    @Search(op = Search.Op.REGEX)
    @Excel(name = "岗位名称")
    private String postName;

    /**
     * 岗位排序
     */
    @Excel(name = "岗位排序")
    private String postSort;

    /**
     * 状态（0正常 1停用）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 用户是否存在此岗位标识 默认不存在
     */
    @Transient
    private boolean flag = false;

}
