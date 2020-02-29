package me.izhong.jobs.manage.impl.service.impl;

import me.izhong.db.mongo.service.CrudBaseServiceImpl;
import me.izhong.jobs.manage.impl.core.model.ZJobScript;
import me.izhong.jobs.manage.impl.service.ZJobScriptService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZJobScriptServiceImpl extends CrudBaseServiceImpl<Long,ZJobScript> implements ZJobScriptService {

    @Override
    public List<ZJobScript> findByJobId(long jobId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("jobId").is(jobId));
        return super.selectList(query, null, null);
    }

    @Override
    public void removeOld(Long jobId, int keepCount) {
        Query query = new Query();
        query.addCriteria(Criteria.where("jobId").is(jobId));

        query.skip(keepCount);
        query.with(new Sort(Sort.Direction.DESC, "createTime"));
        mongoTemplate.remove(query, ZJobScript.class);
    }
}
