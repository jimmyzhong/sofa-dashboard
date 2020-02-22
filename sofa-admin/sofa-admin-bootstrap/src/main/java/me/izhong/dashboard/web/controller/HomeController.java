package me.izhong.dashboard.web.controller;

import me.izhong.common.model.UserInfo;
import me.izhong.dashboard.manage.entity.SysMenu;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import me.izhong.dashboard.manage.service.SysMenuService;
import me.izhong.dashboard.manage.domain.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class HomeController {


    @Autowired
    private SysMenuService sysMenuService;

    @RequestMapping("/")
    public String index(Model model) {
        Message msg = new Message("测试标题", "测试内容", "额外信息，只对管理员显示");
        model.addAttribute("msg", msg);
        return "redirect:/index";
    }

    @RequestMapping("/index")
    public String index2(Model model) {
        UserInfo user = UserInfoContextHelper.getLoginUser();
        model.addAttribute("user", user);

//        List<SysMenu> menus = new ArrayList<>();
//
//        SysMenu m1 = new SysMenu();
//        m1.setMenuName("系统管理");
//        m1.setMenuId(1L);
//        m1.setOrderNum("1");
//        m1.setVisible("0");
//        menus.add(m1);
//
//        SysMenu m2 = new SysMenu();
//        m2.setMenuName("用户");
//        m2.setUrl("/system/user");
//        m2.setMenuId(2L);
//        m2.setOrderNum("2");
//        m2.setVisible("0");
//        m1.getChildren().add(m2);

        model.addAttribute("demoEnabled", StringUtils.equals(user.getLoginName(),"admin"));

        List<SysMenu> menus2 = sysMenuService.selectVisibleMenusByUser(user.getUserId());
        if(menus2 !=null && menus2.size() > 0) {
            //去掉隐藏的菜单
        }
        model.addAttribute("menus", menus2);

        return "index";
    }

    // 切换主题
    @GetMapping("/system/switchSkin")
    public String switchSkin(ModelMap mmap)
    {
        return "skin";
    }

    // 系统介绍
    @GetMapping("/system/main")
    public String main(ModelMap mmap) {
        mmap.put("version", "1.0");
        return "main_v1";
    }

//    @RequestMapping("/error")
//    public String error(Model model){
//        return "/error/unauth";
//    }

}