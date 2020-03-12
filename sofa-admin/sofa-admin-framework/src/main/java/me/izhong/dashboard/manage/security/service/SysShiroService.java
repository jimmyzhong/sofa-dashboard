package me.izhong.dashboard.manage.security.service;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.model.UserInfo;
import me.izhong.common.util.DateUtil;
import me.izhong.dashboard.manage.entity.SysUserOnline;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import me.izhong.dashboard.manage.security.session.OnlineSession;
import me.izhong.dashboard.manage.service.SysUserOnlineService;
import me.izhong.dashboard.manage.util.IpUtil;
import me.izhong.dashboard.manage.util.SerializeUtil;
import me.izhong.dashboard.manage.util.ServletUtil;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 会话db操作处理
 */
@Slf4j
@Component
public class SysShiroService {
    @Autowired
    private SysUserOnlineService onlineService;

    private static final String CACHE_SESSION = SysShiroService.class.getName() + "CACHE_SESSION";
    /**
     * 删除会话
     *
     * @param onlineSession 会话信息
     */
    public void deleteSession(OnlineSession onlineSession) {
        onlineService.deleteById(String.valueOf(onlineSession.getId()));
    }

    /**
     * 获取会话信息
     *
     * @param sessionId
     * @return
     */
    public Session getSession(Serializable sessionId) {
        HttpServletRequest request = ServletUtil.getRequest();
        if(request != null && request.getAttribute(CACHE_SESSION + sessionId) != null) {
            //log.info("request session hit");
            return (Session)request.getAttribute(CACHE_SESSION + sessionId);
        }

        SysUserOnline sysUserOnline = onlineService.selectByPId(String.valueOf(sessionId));
        Session session = sysUserOnline == null ? null : createSession(sysUserOnline);
        if (session == null) {
            //log.info("查询session {} 失败",sessionId);
        } else {
            log.debug("从数据库查询session {} 成功", sessionId);
            if(request !=null)
                request.setAttribute(CACHE_SESSION+sessionId,session);
        }
        return session;
    }

    private Session createSession(SysUserOnline sysUserOnline) {
        if (sysUserOnline != null && sysUserOnline.getSession() != null) {
            try {
                return (Session) SerializeUtil.unserialize(sysUserOnline.getSession());
            } catch (Exception e) {
                log.info("序列化session异常",e);
                onlineService.deleteById(sysUserOnline.getSessionId());
            }
        }
        return null;
    }

    public Collection<Session> getActiveSessions() {
        List<SysUserOnline> onlines = onlineService.findAll();
        Collection<Session> rts = new ArrayList<>();
        if(onlines != null)
            onlines.forEach( e ->
                    rts.add(createSession(e))
            );
        return rts;
    }

    public void saveSession(Session s) {
        OnlineSession session = (OnlineSession) s;

        UserInfo user = null;
        Long userId = null;
        String loginName = null;
        if(s.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) != null) {
            Object obj =  ((SimplePrincipalCollection)s.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY)).getPrimaryPrincipal();
            if(obj instanceof UserInfo) {
                user = (UserInfo)obj;
            } else {
                user = new UserInfo();
                BeanUtils.copyProperties(obj, user);
            }
        }

        SysUserOnline online = new SysUserOnline();
        online.setSessionId(String.valueOf(session.getId()));
        if(user != null) {
            userId = user.getUserId();
            loginName = user.getLoginName();
            online.setDeptName(user.getDeptName());
            online.setLoginName(loginName);
        }
        online.setStartTimestamp(session.getStartTimestamp());
        online.setLastAccessTime(session.getLastAccessTime());
        online.setExpireTime(session.getTimeout());
        online.setIpAddr(session.getHost());
        online.setLoginLocation(IpUtil.getRealAddressByIP(session.getHost()));
        online.setBrowser(session.getBrowser());
        online.setOs(session.getOs());
        online.setStatus(session.getStatus());
        online.setSession(SerializeUtil.serialize(s));
        log.debug("修改session {} {} {} {}",session.getId(),userId,loginName, DateUtil.dateTime(session.getLastAccessTime()));
        onlineService.saveOnline(online);
    }
}
