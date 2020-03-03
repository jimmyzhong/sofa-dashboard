package me.izhong.dashboard.manage.entity;

import com.alibaba.fastjson.annotation.JSONField;
import me.izhong.common.annotation.*;
import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_user")
@Data
public class SysUser extends TimedBasedEntity {

    @PrimaryId
    @AutoId
    @Indexed(unique = true)
    @Excel(name = "用户序号", prompt = "用户编号", cellType = Excel.ColumnType.NUMERIC)
    private Long userId;

    @Indexed(unique = true)
    @Excel(name = "登录名称")
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "登录账号不能为空")
    @Size(min = 0, max = 30, message = "登录账号长度不能超过30个字符")
    private String loginName;

    @Excel(name = "用户名称")
    @Search(op = Search.Op.REGEX)
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
    private String userName;
    @Indexed
    private String openId;

    @Excel(name = "手机号码")
    @Search
    @Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
    private String phoneNumber;

    private Boolean phoneNumberLoginEnable;

    @Excel(name = "用户邮箱")
    @Search
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    private Boolean emailLoginEnable;

    /**
     * 用户性别
     */
    @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
    @Search
    private String sex;

    private String avatar;

    @JSONField(serialize = false)
    private String password;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date passwordUpdateTime;
    /**
     * 盐加密
     */
    private String salt;

    /**
     * 帐号状态（0正常 1停用）
     */
    @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用")
    @Search
    private String status;

    /**
     * 最后登陆IP
     */
    @Excel(name = "最后登陆IP", type = Excel.Type.EXPORT)
    private String loginIp;

    /**
     * 最后登陆时间
     */
    @Excel(name = "最后登陆时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
    private Date loginDate;

    /**
     * 部门ID
     */
    @Excel(name = "部门编号", type = Excel.Type.ALL)
    //@Search
    private Long deptId;

    /**
     * 部门父ID
     */
    @Transient
    private Long parentId;

    private String deptName;

    /**
     * 角色集合
     */
    @Transient
    private List<SysRole> sysRoles;

    /**
     * 角色组
     */
    @Transient
    private Long[] roleIds;

    /**
     * 岗位组
     */
    @Transient
    private Long[] postIds;

    public SysUser(){
    }


    public SysUser(Long userId){
        this.userId = userId;
    }

    public boolean isAdmin() {
        return isAdmin(this.userId);
    }

    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }
}
