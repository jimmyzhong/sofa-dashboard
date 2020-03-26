package me.izhong.dashboard.web.controller.admin;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.entity.SysUser;
import me.izhong.dashboard.manage.security.service.SysRegisterService;
import me.izhong.dashboard.manage.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Slf4j
public class RegisterAdminController {

    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private SysConfigService configService;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    @AjaxWrapper
    public void ajaxRegister(SysUser user) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            throw BusinessException.build("当前系统没有开启注册功能！");
        }
        registerService.register(user);
    }
}
