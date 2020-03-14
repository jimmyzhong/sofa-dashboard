package me.izhong.dashboard.manage.service;

import me.izhong.db.common.service.CrudBaseService;
import me.izhong.dashboard.manage.entity.SysConfig;

public interface SysConfigService extends CrudBaseService<Long,SysConfig> {

    /**
     * 只查询正常的config
     * @param key
     * @return
     */
    String selectNormalConfigByKey(String key);

    /**
     * 查询包含删除的config
     * @param configKey
     * @return
     */
    String selectConfigByKey(String configKey);

    boolean checkConfigKeyUnique(SysConfig sysConfig);
}
