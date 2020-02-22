package me.izhong.jobs.admin.controller;

import me.izhong.dashboard.manage.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JobHomeController {


    @Autowired
    private SysMenuService sysMenuService;

    // 系统介绍
    @GetMapping("/system/main_job")
    public String main(ModelMap mmap) {
        mmap.put("version", "1.0");
        return "ext/djob/main_v1";
    }


}