package me.izhong.jobs.manage.impl.service.impl;

import me.izhong.db.mongo.service.CrudBaseServiceImpl;
import me.izhong.db.mongo.util.CriteriaUtil;
import me.izhong.jobs.manage.impl.core.model.ZJobConfig;
import me.izhong.jobs.manage.impl.service.ZJobConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ZJobConfigServiceImpl extends CrudBaseServiceImpl<Long,ZJobConfig> implements ZJobConfigService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数名称
     * @return 参数键值
     */
    @Override
    public String selectNormalConfigByKey(String configKey) {
        Query query = new Query();
        query.addCriteria(Criteria.where("configKey").is(configKey));
        query.addCriteria(CriteriaUtil.notDeleteCriteria());
        ZJobConfig sysConfig = mongoTemplate.findOne(query, ZJobConfig.class);
        if (sysConfig == null) {
            return null;
        }
        return sysConfig.getConfigValue();
    }
}
