package me.izhong.dashboard.web.controller.monitor;

import me.izhong.dashboard.manage.security.service.PasswordService;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.db.common.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.dashboard.manage.entity.SysLoginInfo;
import me.izhong.dashboard.manage.security.config.PermissionConstants;
import me.izhong.dashboard.manage.service.SysLoginInfoService;
import me.izhong.dashboard.manage.util.ExcelUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/monitor/logininfor")
public class SysLoginInfoController {
    private String prefix = "monitor/logininfor";

    @Autowired
    private SysLoginInfoService sysLoginInfoService;

    @Autowired
    private PasswordService passwordService;

    @RequiresPermissions(PermissionConstants.LoginInfo.VIEW)
    @GetMapping()
    public String logininfor() {
        return prefix + "/logininfor";
    }

    @RequiresPermissions(PermissionConstants.LoginInfo.VIEW)
    @PostMapping("/list")
    @AjaxWrapper
    public PageModel<SysLoginInfo> list(HttpServletRequest request, SysLoginInfo sysLoginInfo) {
        return sysLoginInfoService.selectPage(PageRequestUtil.fromRequest(request), sysLoginInfo);
    }

    @Log(title = "登陆日志", businessType = BusinessType.EXPORT)
    @RequiresPermissions(PermissionConstants.LoginInfo.EXPORT)
    @PostMapping("/export")
    @AjaxWrapper
    public String export(HttpServletRequest request, SysLoginInfo sysLoginInfo) {
        List<SysLoginInfo> list = sysLoginInfoService.selectList(PageRequestUtil.fromRequestIgnorePageSize(request), sysLoginInfo);
        ExcelUtil<SysLoginInfo> util = new ExcelUtil<SysLoginInfo>(SysLoginInfo.class);
        return util.exportExcel(list, "登陆日志");
    }

    @RequiresPermissions(PermissionConstants.LoginInfo.REMOVE)
    @Log(title = "登陆日志", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @AjaxWrapper
    public long remove(String ids) {
        return sysLoginInfoService.removeByPIds(ids);
    }

    @RequiresPermissions(PermissionConstants.LoginInfo.REMOVE)
    @Log(title = "登陆日志", businessType = BusinessType.CLEAN)
    @PostMapping("/clean")
    @AjaxWrapper
    public long clean() {
        return sysLoginInfoService.clearAll();
    }

    @RequiresPermissions(PermissionConstants.LoginInfo.UNLOCK)
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @PostMapping("/unlock")
    @AjaxWrapper
    public void unlock(String loginName)
    {
        passwordService.unlock(loginName);
    }
}
