package me.izhong.dashboard.manage.entity;

import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.izhong.common.annotation.AutoId;
import me.izhong.common.annotation.PrimaryId;
import me.izhong.common.annotation.Search;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_menu")
@Data
public class SysMenu extends TimedBasedEntity {

    @PrimaryId
    @AutoId
    @Indexed(unique = true)
    private Long menuId;

    @Indexed(unique = false)
    @Search(op = Search.Op.REGEX)
    @NotBlank(message = "菜单名称不能为空")
    @Size(min = 0, max = 50, message = "菜单名称长度不能超过50个字符")
    private String menuName;

    @Search
    @Size(min = 0, max = 200, message = "请求地址不能超过200个字符")
    private String url;

    @Search
    private Long parentId;
    private String parentName;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    private Integer orderNum;

    /**
     * 打开方式：menuItem页签 menuBlank新窗口
     */
    private String target;

    /**
     * 类型:0目录,1菜单,2按钮
     */
    @Search
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    /**
     * 菜单状态:0显示,1隐藏
     */
    @Search
    private String visible;

    /**
     * 权限字符串
     */
    @Size(min = 0, max = 100, message = "权限标识长度不能超过100个字符")
    private String perms;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 子菜单
     */
    @Transient
    private List<SysMenu> children = new ArrayList<SysMenu>();

}
