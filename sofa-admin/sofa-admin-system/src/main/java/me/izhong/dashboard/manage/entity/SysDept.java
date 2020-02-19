package me.izhong.dashboard.manage.entity;

import me.izhong.common.annotation.PrimaryId;
import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.izhong.common.annotation.AutoId;
import me.izhong.common.annotation.Search;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门表 sys_dept
 */
@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_dept")
@Data
public class SysDept extends TimedBasedEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    @AutoId
    @PrimaryId
    @Indexed(unique = true)
    private Long deptId;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 祖级列表
     */
    private List<Long> ancestors = new ArrayList<>();

    /**
     * 子孙列表
     */
    private List<Long> children = new ArrayList<>();

    private List<Long> descendents = new ArrayList<>();

    /**
     * 部门名称
     */
    @Search(op=Search.Op.REGEX)
    @NotBlank(message = "部门名称不能为空")
    @Size(min = 0, max = 30, message = "部门名称长度不能超过30个字符")
    private String deptName;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    private Integer orderNum;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    @Size(min = 0, max = 11, message = "联系电话长度不能超过11个字符")
    private String phone;
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    /**
     * 部门状态:0正常,1停用
     */
    @Search
    private String status;

    /**
     * 父部门名称
     */
    private String parentName;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("deptId", getDeptId())
                .append("parentId", getParentId())
                .append("ancestors", getAncestors())
                .append("deptName", getDeptName())
                .append("orderNum", getOrderNum())
                .append("leader", getLeader())
                .append("phone", getPhone())
                .append("email", getEmail())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }

//    @Override
//    public boolean equals(Object o) {
//        if(!(o instanceof SysDept))
//            return false;
//        SysDept dept = (SysDept) o;
//        return deptId != null && deptId.equals(dept.getDeptId());
//    }
}
