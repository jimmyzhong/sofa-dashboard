package me.izhong.jobs.manage.impl.service.impl;

import com.mongodb.client.result.DeleteResult;
import me.izhong.db.mongo.service.CrudBaseServiceImpl;
import me.izhong.jobs.manage.impl.core.model.ZJobStats;
import me.izhong.jobs.manage.impl.service.ZJobStatsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

@Service
public class ZJobStatsServiceImpl extends CrudBaseServiceImpl<Long, ZJobStats>
        implements ZJobStatsService {

    @Override
    public ZJobStats insertOrUpdate(ZJobStats stats) {
        Query query = new Query();
        query.addCriteria(new Criteria("key").is(stats.getKey()));

        //更新
        Update update = new Update();
        if (stats.getStatsId() == null)
            update.set("statsId", getNextId("JobStats"));
        if (StringUtils.isNotBlank(stats.getType()))
            update.set("type", stats.getType());
        if (StringUtils.isNotBlank(stats.getValue1()))
            update.set("value1", stats.getValue1());
        if (StringUtils.isNotBlank(stats.getValue2()))
            update.set("value2", stats.getValue2());
        if (StringUtils.isNotBlank(stats.getValue3()))
            update.set("value3", stats.getValue3());
        if (StringUtils.isNotBlank(stats.getValue4()))
            update.set("value4", stats.getValue4());
        if (StringUtils.isNotBlank(stats.getValue5()))
            update.set("value5", stats.getValue5());
        update.set("expireTime", new Date());

        FindAndModifyOptions options = FindAndModifyOptions.options();
        //没有则新增
        options.upsert(true);
        options.returnNew(true);
        ZJobStats ml = mongoTemplate.findAndModify(query, update, options, ZJobStats.class);
        return ml;
    }

    @Override
    public List<ZJobStats> findByType(String type) {
        Query query = new Query();
        query.addCriteria(Criteria.where("type").is(type));
        return mongoTemplate.find(query, ZJobStats.class);
    }

    @Override
    public ZJobStats findByKey(String key) {
        Query query = new Query();
        query.addCriteria(Criteria.where("key").is(key));
        return mongoTemplate.findOne(query, ZJobStats.class);
    }

    @Override
    public boolean removeStats(String key) {
        Assert.notNull(key, "");
        Query query = new Query();
        query.addCriteria(Criteria.where("key").is(key));
        DeleteResult rt = mongoTemplate.remove(query, ZJobStats.class);
        return rt.getDeletedCount() > 0;
    }
}
