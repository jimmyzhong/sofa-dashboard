package me.izhong.jobs.manage.impl.service.impl;

import me.izhong.db.mongo.service.CrudBaseServiceImpl;
import me.izhong.jobs.manage.impl.core.model.ZJobGroup;
import me.izhong.jobs.manage.impl.service.ZJobGroupService;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZJobGroupServiceImpl extends CrudBaseServiceImpl<Long,ZJobGroup> implements ZJobGroupService {
    @Override
    public List<ZJobGroup> findByAddressType(int addressType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("addressType").is(addressType));
        return super.selectList(query, null, null);
    }
}
