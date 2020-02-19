package me.izhong.dashboard.manage.entity;

import me.izhong.db.common.domain.TimedBasedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.izhong.common.annotation.PrimaryId;
import me.izhong.common.annotation.Search;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Document(collection = "sys_user_online")
@Data
public class SysUserOnline extends TimedBasedEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 用户会话id
     */
    @PrimaryId
    @Indexed(unique = true)
    private String sessionId;

    /**
     * 部门名称
     */
    @Search
    private String deptName;

    /**
     * 登录名称
     */
    @Indexed
    @Search(op = Search.Op.REGEX)
    private String loginName;

    /**
     * 登录IP地址
     */
    @Search(op = Search.Op.REGEX)
    private String ipAddr;

    /**
     * 登录地址
     */
    @Search(op = Search.Op.REGEX)
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * session创建时间
     */
    private Date startTimestamp;

    /**
     * session最后访问时间
     */
    private Date lastAccessTime;

    /**
     * 超时时间，单位为分钟
     */
    private Long expireTime;

    /**
     * 在线状态
     */
    private OnlineStatus status = OnlineStatus.on_line;

    /**
     * 备份的当前用户会话
     */
    //private OnlineSession2 session;
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("sessionId", getSessionId())
                .append("loginName", getLoginName())
                .append("deptName", getDeptName())
                .append("ipAddr", getIpAddr())
                .append("loginLocation", getLoginLocation())
                .append("browser", getBrowser())
                .append("os", getOs())
                .append("status", getStatus())
                .append("startTimestamp", getStartTimestamp())
                .append("lastAccessTime", getLastAccessTime())
                .append("expireTime", getExpireTime())
                .toString();
    }

    public enum OnlineStatus {
        /**
         * 用户状态
         */
        on_line("在线"), off_line("离线");
        private final String info;

        private OnlineStatus(String info) {
            this.info = info;
        }

        public String getInfo() {
            return info;
        }
    }
}
