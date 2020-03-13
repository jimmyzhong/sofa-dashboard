package me.izhong.dashboard.manage.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_role_dept")
@Data
public class SysRoleDept implements Serializable {
    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 部门ID
     */
    private Long deptId;
}
