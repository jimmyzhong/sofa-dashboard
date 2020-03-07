package me.izhong.dashboard.manage.security.session;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.izhong.dashboard.manage.entity.SysUserOnline;
import org.apache.shiro.session.mgt.SimpleSession;

/**
 * 在线用户会话属性
 */
@Getter
@Setter
@ToString
public class OnlineSession extends SimpleSession {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String loginName;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 登录IP地址
     */
    private String host;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 在线状态
     */
    private SysUserOnline.OnlineStatus status = SysUserOnline.OnlineStatus.on_line;

    /**
     * 属性是否改变 优化session数据同步
     */
    private transient boolean attributeChanged = false;

    public void resetAttributeChanged() {
        this.attributeChanged = false;
    }

    public void markAttributeChanged() {
        this.attributeChanged = true;
    }

    public boolean isAttributeChanged() {
        return attributeChanged;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public void setAttribute(Object key, Object value) {
        super.setAttribute(key, value);
    }

    @Override
    public Object removeAttribute(Object key) {
        return super.removeAttribute(key);
    }
}
