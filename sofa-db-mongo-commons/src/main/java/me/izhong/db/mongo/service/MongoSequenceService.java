package me.izhong.db.mongo.service;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.db.common.domain.SysSeqInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MongoSequenceService {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public long getNextId(String collectionName) {
        Query query = new Query(Criteria.where("collectionName").is(collectionName));
        Update update = new Update();
        update.inc("seqId", 1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);

        SysSeqInfo seq = mongoTemplate.findAndModify(query, update, options, SysSeqInfo.class);
        log.debug(collectionName + " generate id:" + seq.getSeqId());

        return seq.getSeqId();
    }

}
