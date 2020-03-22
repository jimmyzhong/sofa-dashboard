package me.izhong.db.mongo.service;

import lombok.extern.slf4j.Slf4j;
import me.izhong.db.common.domain.SysRuntimeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
public class MongoRuntimeConfigService {

    @Autowired
    protected MongoTemplate mongoTemplate;

    public void updateRuntimeConfig(String key, Object value) {
        Query query = new Query(Criteria.where("_id").is("runtime"));
        Update update = new Update();
        update.set(key, value);

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);

        SysRuntimeConfig config = mongoTemplate.findAndModify(query, update, options, SysRuntimeConfig.class);
    }

    public void updateRealmUpdateTime() {
        updateRuntimeConfig("realmUpdateTime",new Date());
    }

    public boolean isNeedUpdateRealm(Date sessionUpdateTime) {
        Query query = new Query(Criteria.where("_id").is("runtime"));
        SysRuntimeConfig config = mongoTemplate.findOne(query, SysRuntimeConfig.class);
        if(config != null && config.getRealmUpdateTime() !=null && config.getRealmUpdateTime().after(sessionUpdateTime)) {
            return true;
        }
        return false;
    }

}
