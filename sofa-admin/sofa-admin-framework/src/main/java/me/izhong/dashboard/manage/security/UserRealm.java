package me.izhong.dashboard.manage.security;

import me.izhong.dashboard.manage.service.SysUserService;
import me.izhong.dashboard.manage.util.SpringUtil;
import me.izhong.common.exception.BusinessException;
import me.izhong.common.model.UserInfo;
import me.izhong.dashboard.manage.constants.Global;
import me.izhong.dashboard.manage.entity.SysDept;
import me.izhong.dashboard.manage.entity.SysRole;
import me.izhong.dashboard.manage.entity.SysUser;
import me.izhong.dashboard.manage.expection.*;
import me.izhong.dashboard.manage.expection.user.UserBlockedException;
import me.izhong.dashboard.manage.expection.user.UserNotFoundException;
import me.izhong.dashboard.manage.expection.user.UserPasswordNotMatchException;
import me.izhong.dashboard.manage.expection.user.UserPasswordRetryLimitExceedException;
import me.izhong.dashboard.manage.security.service.LoginService;
import me.izhong.dashboard.manage.service.SysDeptService;
import me.izhong.dashboard.manage.service.SysMenuService;
import me.izhong.dashboard.manage.service.SysRoleService;
import me.izhong.dashboard.manage.util.UserConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    private SysUserService sysUserService;

    @Autowired
    private SysDeptService sysDeptService;

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
        UserInfo user = UserInfoContextHelper.getLoginUser();
        // 角色列表
        Set<String> roles = new HashSet<String>();
        // 菜单，按钮权限列表
        Set<String> menus = new HashSet<String>();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        roles = sysRoleService.selectRoleKeys(user.getUserId());
        menus = sysMenuService.selectPermsByUserId(user.getUserId());
        // 角色加入AuthorizationInfo认证对象
        info.setRoles(roles);
        // 权限加入AuthorizationInfo认证对象
        info.setStringPermissions(menus);

        log.info("用户[{}]从数据库加载权限成功", UserInfoContextHelper.getCurrentLoginName());
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

        SysUser user = null;
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

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(loginUser, password, getName());
        setUserScope(loginUser);
        return info;
    }
    public static void refreshUserScope() {
        UserInfo u = (UserInfo) SecurityUtils.getSubject().getPrincipal();

        SpringUtil.getBean(UserRealm.class).setUserScope(u);

        Subject subject = SecurityUtils.getSubject();
        String realmName = subject.getPrincipals().getRealmNames().iterator().next();
        SimplePrincipalCollection principals = new SimplePrincipalCollection(u, realmName);
        subject.runAs(principals);

        //清理缓存
        DefaultWebSecurityManager rsm = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
        UserRealm myShiroRealm = (UserRealm) rsm.getRealms().iterator().next();
        if(myShiroRealm.isAuthenticationCachingEnabled()) {
            myShiroRealm.getAuthenticationCache().remove(principals);
        }
        if(myShiroRealm.isAuthorizationCachingEnabled()) {
            // 删除指定用户shiro权限
            myShiroRealm.getAuthorizationCache().remove(principals);
        }
        // 刷新权限
        subject.releaseRunAs();
    }
    private void setUserScope(UserInfo loginUser){
        //根据roles 计算数据权限
        if(loginUser == null) {
            log.info("用户没有登录");
            return;
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
    }

    /**
     * 清理缓存权限
     */
    public void clearCachedAuthorizationInfo() {
        this.clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
    }
}
