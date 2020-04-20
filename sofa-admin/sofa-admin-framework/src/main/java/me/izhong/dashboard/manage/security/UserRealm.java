package me.izhong.dashboard.manage.security;

import eu.bitwalker.useragentutils.UserAgent;
import me.izhong.dashboard.manage.security.service.SysShiroService;
import me.izhong.dashboard.manage.security.session.OnlineSession;
import me.izhong.dashboard.manage.service.*;
import me.izhong.dashboard.common.util.IpUtil;
import me.izhong.dashboard.common.util.ServletUtil;
import me.izhong.dashboard.common.util.SpringUtil;
import me.izhong.common.exception.BusinessException;
import me.izhong.common.model.UserInfo;
import me.izhong.dashboard.common.constants.Global;
import me.izhong.dashboard.manage.entity.SysDept;
import me.izhong.dashboard.manage.entity.SysRole;
import me.izhong.dashboard.manage.entity.SysUser;
import me.izhong.dashboard.common.expection.*;
import me.izhong.dashboard.common.expection.user.UserBlockedException;
import me.izhong.dashboard.common.expection.user.UserNotFoundException;
import me.izhong.dashboard.common.expection.user.UserPasswordNotMatchException;
import me.izhong.dashboard.common.expection.user.UserPasswordRetryLimitExceedException;
import me.izhong.dashboard.manage.security.service.LoginService;
import me.izhong.dashboard.common.util.UserConvertUtil;
import me.izhong.db.mongo.service.MongoRuntimeConfigService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自定义Realm 处理登录 权限
 */
public class UserRealm extends AuthorizingRealm {
    private static final Logger log = LoggerFactory.getLogger(UserRealm.class);

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private MongoRuntimeConfigService mongoRuntimeConfigService;

    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private SysShiroService shiroService;
    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
        UserInfo user = UserInfoContextHelper.getLoginUser();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        // 角色列表
        Set<String> roles;
        // 菜单，按钮权限列表
        Set<String> menus;

        if(Global.isDebugMode()) {
            roles = sysRoleService.selectRoleKeys(user.getUserId());
            menus = sysMenuService.selectPermsAll();
            user.setRoles(roles);
            user.setPerms(menus);
            //UserInfoContextHelper.setUser(user);
            log.debug("DebugMode用户[{}]从数据库加载权限成功", user.getLoginName());
        } else if(user.getPerms() !=null) {
            roles = user.getRoles();
            menus = user.getPerms();
            log.debug("用户[{}]从session加载权限成功", user.getLoginName());
        } else {
            roles = sysRoleService.selectRoleKeys(user.getUserId());
            menus = sysMenuService.selectPermsByUserId(user.getUserId());
            user.setRoles(roles);
            user.setPerms(menus);
            //UserInfoContextHelper.setUser(user);
            log.debug("用户[{}]从数据库加载权限成功", user.getLoginName());
        }

        // 角色加入AuthorizationInfo认证对象
        info.setRoles(roles);
        // 权限加入AuthorizationInfo认证对象
        info.setStringPermissions(menus);

        return info;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        String password = "";
        if (upToken.getPassword() != null) {
            password = new String(upToken.getPassword());
        }

        SysUser user;
        try {
            user = loginService.login(username, password);
        } catch (CaptchaException e) {
            throw new AuthenticationException(e.getMessage(), e);
        } catch (UserNotFoundException e) {
            throw new UnknownAccountException(e.getMessage(), e);
        } catch (UserPasswordNotMatchException e) {
            throw new IncorrectCredentialsException(e.getMessage(), e);
        } catch (UserPasswordRetryLimitExceedException e) {
            throw new ExcessiveAttemptsException(e.getMessage(), e);
        } catch (UserBlockedException e) {
            throw new LockedAccountException(e.getMessage(), e);
        } catch (RoleBlockedException e) {
            throw new LockedAccountException(e.getMessage(), e);
        } catch (Exception e) {
            log.info("对用户[" + username + "]进行登录验证..验证未通过{}", e.getMessage());
            throw new AuthenticationException(e.getMessage(), e);
        }
        UserInfo loginUser = UserConvertUtil.convert(user);

        String avatarUrl = Global.getAvatarMapping();
        if(StringUtils.isNotBlank(loginUser.getAvatar()) && !loginUser.getAvatar().startsWith("http")) {
            loginUser.setAvatar(avatarUrl + user.getAvatar());
        }

        log.info("用户[{}]登陆成功", user.getLoginName());

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(loginUser, password, getName());
        setUserScope(loginUser);
        return info;
    }

    public static void refreshUserScope() {

        UserInfo u = UserInfoContextHelper.getLoginUser();
        if(u == null) {
            log.info("用户没有登陆refreshUserScope失败");
        }
        SysUser sysUser = SpringUtil.getBean(SysUserService.class).findUser(u.getUserId());
        UserInfo loginUser = UserConvertUtil.convert(sysUser);
        SpringUtil.getBean(UserRealm.class).setUserScope(loginUser);

        Subject subject = SecurityUtils.getSubject();
        String realmName = subject.getPrincipals().getRealmNames().iterator().next();
        SimplePrincipalCollection principals = new SimplePrincipalCollection(loginUser, realmName);
        subject.runAs(principals);

/*        //清理缓存
        DefaultWebSecurityManager rsm = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
        UserRealm myShiroRealm = (UserRealm) rsm.getRealms().iterator().next();
        if(myShiroRealm.isAuthenticationCachingEnabled() && myShiroRealm.getAuthenticationCache() != null) {
            myShiroRealm.getAuthenticationCache().remove(principals);
        }
        if(myShiroRealm.isAuthorizationCachingEnabled() && myShiroRealm.getAuthorizationCache() != null) {
            // 删除指定用户shiro权限
            myShiroRealm.getAuthorizationCache().remove(principals);
        }
        // 刷新权限
        subject.releaseRunAs();*/
    }
    private void setUserScope(UserInfo loginUser){
        //根据roles 计算数据权限
        if(loginUser == null) {
            log.info("用户没有登录");
            return;
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = shiroService.getSession(subject.getSession().getId());
        OnlineSession onlineSession = (OnlineSession)session;

        HttpServletRequest request = ServletUtil.getRequest();
        if (request != null) {
            UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtil.getRequest().getHeader("User-Agent"));
            // 获取客户端操作系统
            String os = userAgent.getOperatingSystem().getName();
            // 获取客户端浏览器
            String browser = userAgent.getBrowser().getName();
            onlineSession.setHost(IpUtil.getIpAddr(request));
            onlineSession.setBrowser(browser);
            onlineSession.setOs(os);

            String ip = IpUtil.getIpAddr(request);
            loginUser.setLoginIp(ip);
        }

        List<SysRole> rs = sysRoleService.selectRolesByUserId(loginUser.getUserId());
        // 实体名称 -> deptIds
        Long deptId = loginUser.getDeptId();
        if(deptId == null)
            throw BusinessException.build("用户["+loginUser.getLoginName()+"]缺少所属部门记录");

        SysDept sysDept = sysDeptService.selectDeptByDeptId(deptId);
        List<Long> desces = sysDept.getDescendents();
        List<SysDept> allDepts = sysDeptService.selectAll();
        List<Long> allDeptIds = allDepts.stream().map(e->e.getDeptId()).collect(Collectors.toList());

        allDepts.forEach(e->{
            UserInfo.getDeptIdNames().put(e.getDeptId(),e.getDeptName());
        });

        loginUser.setHasAllDeptPerm(false);
        loginUser.setScopes(new HashMap<>());
        //遍历用户角色
        rs.forEach(e -> {
            Long roleId = e.getRoleId();
            String scope = e.getDataScope();

            List<String> rolePerms = sysMenuService.selectPermsByRoleId(roleId);
            List<Long> roleDeptIds = sysRoleService.selectDeptIdsByRoleId(roleId);
            if (rolePerms != null && rolePerms.size() > 0) {
                rolePerms.forEach(rpk -> {
                    if (StringUtils.equals("1", scope)) {
                        loginUser.setHasAllDeptPerm(true);
                        loginUser.addScopeData(rpk, allDeptIds);
                    } else if (StringUtils.equals("2", scope)) {
                        loginUser.addScopeData(rpk, roleDeptIds);
                    } else if (StringUtils.equals("3", scope)) {
                        loginUser.addScopeData(rpk, deptId);
                    } else if (StringUtils.equals("4", scope)) {
                        desces.add(deptId);
                        loginUser.addScopeData(rpk, desces);
                    } else {
                        throw BusinessException.build(e.getRoleName() + "的datascope不复合规则");
                    }
                });
            }
        });

        onlineSession.setRealmUpdateTime(new Date());
        shiroService.saveSession(onlineSession);

        log.info("用户{}刷新部门权限完成",loginUser.getLoginName());
    }

    /**
     * 清理缓存权限
     */
    public void clearCachedAuthorizationInfo() {
        this.clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
    }
}
