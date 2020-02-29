package me.izhong.dashboard.web.service;

import me.izhong.dashboard.manage.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("config")
public class ConfigService {

    @Autowired
    private SysConfigService sysConfigService;

    public String getKey(String key) {
        return  sysConfigService.selectNormalConfigByKey(key);
    }
}
