package me.izhong.dashboard.manage.entity;

import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.izhong.common.annotation.AutoId;
import me.izhong.common.annotation.Excel;
import me.izhong.common.annotation.PrimaryId;
import me.izhong.common.annotation.Search;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Size;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_role")
@Data
public class SysRole extends TimedBasedEntity {

    /**
     * 角色ID
     */
    @PrimaryId
    @AutoId
    @Indexed(unique = true)
    @Excel(name = "角色序号", cellType = Excel.ColumnType.NUMERIC)
    private Long roleId;

    /**
     * 角色名称
     */
    @Excel(name = "角色名称")
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 0, max = 30, message = "角色名称长度不能超过30个字符")
    private String roleName;

    /**
     * 角色权限
     */
    @Excel(name = "角色权限")
    @Search
    @NotBlank(message = "权限字符不能为空")
    @Size(min = 0, max = 100, message = "权限字符长度不能超过100个字符")
    private String roleKey;

    /**
     * 角色排序
     */
    @Indexed
    @Excel(name = "角色排序")
    @NotBlank(message = "显示顺序不能为空")
    private String roleSort;

    /**
     * 数据范围
     */
    @NotBlank
    @Excel(name = "数据范围", readConverterExp = "1=所有数据权限,2=自定义数据权限,3=本部门数据权限,4=本部门及以下数据权限")
    private String dataScope;

    /**
     * 角色状态（0正常 1停用）
     */
    @NotBlank
    @Excel(name = "角色状态", readConverterExp = "0=正常,1=停用")
    @Search
    private String status;

    /**
     * 用户是否存在此角色标识 默认不存在
     */
    @Transient
    private boolean flag = false;

    /**
     * 菜单组
     */
    private Long[] menuIds;

    /**
     * 部门组（数据权限）
     */
    private Long[] deptIds;


    public boolean isAdmin()
    {
        return isAdmin(this.roleId);
    }

    public static boolean isAdmin(Long roleId)
    {
        return roleId != null && 1L == roleId;
    }
}
