package me.izhong.db.common.listener;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AutoId;
import me.izhong.common.annotation.CreateTimeAdvise;
import me.izhong.common.annotation.UpdateTimeAdvise;
import me.izhong.db.common.domain.SysSeqInfo;
import me.izhong.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Date;

//@Component
@Slf4j
public class EntitySaveEventListener extends AbstractMongoEventListener<Object> {

    @Autowired(required = false)
    private MongoTemplate mongo;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        if(mongo == null)
            return;
        final Object source = event.getSource();
        if (source != null) {
            ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {
                @Override
                public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                    ReflectionUtils.makeAccessible(field);
                    boolean isNew = false;
                    if (field.isAnnotationPresent(AutoId.class)) {
                        if(!field.getType().equals(Long.class)){
                            throw BusinessException.build("AutoId field must be Long type");
                        }
                        if (field.get(source) == null || ((Long) field.get(source)).longValue() == 0) {
                            field.set(source, getNextId(source.getClass().getSimpleName() + "|" + field.getName()));
                            isNew = true;
                        }
                    }

                    if (field.isAnnotationPresent(CreateTimeAdvise.class)) {
                        if (field.get(source) == null) {
                            field.set(source, new Date());
                        }
                    }
                    if (field.isAnnotationPresent(UpdateTimeAdvise.class)) {
                        field.set(source, new Date());
                    }
                }
            });
        }
    }


    private Long getNextId(String collectionName) {
        Query query = new Query(Criteria.where("collectionName").is(collectionName));
        Update update = new Update();
        update.inc("seqId", 1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);

        SysSeqInfo seq = mongo.findAndModify(query, update, options, SysSeqInfo.class);
        log.debug(collectionName + "generate id:" + seq.getSeqId());
        return seq.getSeqId();
    }

}
