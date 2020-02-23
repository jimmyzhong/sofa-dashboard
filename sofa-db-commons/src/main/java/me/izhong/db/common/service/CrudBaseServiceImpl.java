package me.izhong.db.common.service;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.util.Convert;
import me.izhong.common.annotation.*;
import me.izhong.db.common.domain.SysSeqInfo;
import me.izhong.common.exception.BusinessException;
import me.izhong.db.common.util.CriteriaUtil;
import me.izhong.db.common.util.PageRequestUtil;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

@Slf4j
public class CrudBaseServiceImpl<K,T> implements CrudBaseService<K,T> {

    Type kClass;
    Type tClass;

    public CrudBaseServiceImpl() {
        ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
        kClass = pt.getActualTypeArguments()[0];
        tClass = pt.getActualTypeArguments()[1];
    }

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Override
    public PageModel<T> selectPage(PageRequest request, T target) {
        return selectPage(null, request, target);
    }

    @Override
    public PageModel<T> selectPage(Query query, PageRequest request, T target) {
        if(query == null)
            query = new Query();
        List<T> results = selectList(query,request,target);
        long count = doCount(query);
        return PageModel.instance(count, results);
    }

    @Override
    public List<T> selectList(Query query, PageRequest request, T target) {
        if (query == null) {
            query = new Query();
        }
        if(request != null)
            PageRequestUtil.injectQuery(request,query);

        injectObject(query, target);
        log.info("query list sql:{}",query);
        List<T> results = mongoTemplate.find(query, (Class) tClass);
        return results;
    }

    @Override
    public List<T> selectList(PageRequest request, T target) {
        return selectList(null,request,target);
    }

    @Override
    public List<T> selectList(T target) {
        return selectList(null,null,target);
    }

    @Override
    public long count(Query query, PageRequest request, T target) {
        if (query == null) {
            query = new Query();
        }

        if(request != null)
            PageRequestUtil.injectQuery(request,query);

        injectObject(query, target);
        return doCount(query);
    }

    private void injectObject(Query query, T target) {
        if (target != null) {
            List<Field> fields = FieldUtils.getAllFieldsList(target.getClass());
            for (Field f : fields) {
                Search search = f.getAnnotation(Search.class);
                if (search == null)
                    continue;
                String key = StringUtils.isNotEmpty(search.columnName()) ? search.columnName() : f.getName();
                Search.Op op = search.op();
                Object value = null;
                try {
                    f.setAccessible(true);
                    value = FieldUtils.readField(f, target);
                } catch (IllegalAccessException e) {
                    log.error("", e);
                }
                if (value instanceof String && StringUtils.isBlank(((String) value))) {
                    continue;
                }
                if (value != null) {
                    if (op.equals(Search.Op.IS)) {
                        if(key.equals("isDelete")) {
                            if (value instanceof Boolean) {
                                if(((Boolean) value).booleanValue()) {
                                    CriteriaUtil.addCriteria(query, CriteriaUtil.deleteCriteria());
                                } else if(!((Boolean) value).booleanValue()){
                                    CriteriaUtil.addCriteria(query, CriteriaUtil.notDeleteCriteria());
                                }
                            }
                        } else {
                            if (value instanceof String)
                                CriteriaUtil.addCriteria(query, Criteria.where(key).is(value.toString().trim()));
                            else
                                CriteriaUtil.addCriteria(query, Criteria.where(key).is(value));
                        }
                    } else if (op.equals(Search.Op.REGEX)) {
                        if (value instanceof String)
                            CriteriaUtil.addCriteria(query,Criteria.where(key).regex(value.toString().trim()));
                    } else if (op.equals(Search.Op.IN)) {
                        if(value instanceof Number[]) {
                            Number[] numValue = (Number[]) value;
                            if(numValue.length > 0)
                                CriteriaUtil.addCriteria(query,Criteria.where(key).in(numValue));
                        }else if(value instanceof String[]) {
                            String[] strValue = (String[]) value;
                            if(strValue.length > 0)
                                CriteriaUtil.addCriteria(query,Criteria.where(key).in(strValue));
                        }
                    } else if (op.equals(Search.Op.GT)) {
                        if(value instanceof Number) {
                            Number numValue = (Number) value;
                            CriteriaUtil.addCriteria(query,Criteria.where(key).gt(numValue));
                        }
                    } else if (op.equals(Search.Op.GTE)) {
                        if(value instanceof Number) {
                            Number numValue = (Number) value;
                            CriteriaUtil.addCriteria(query,Criteria.where(key).gte(numValue));
                        }
                    } else if (op.equals(Search.Op.LT)) {
                        if(value instanceof Number) {
                            Number numValue = (Number) value;
                            CriteriaUtil.addCriteria(query,Criteria.where(key).lt(numValue));
                        }
                    } else if (op.equals(Search.Op.GTE)) {
                        if(value instanceof Number) {
                            Number numValue = (Number) value;
                            CriteriaUtil.addCriteria(query,Criteria.where(key).lte(numValue));
                        }
                    }
                }
            }
        }
    }

    @Override
    public long count(PageRequest request, T target) {
        return count(null,request,target);
    }

    @Override
    public long count() {
        return doCount(null);
    }

    private long doCount(Query query){
        if(query == null)
            query = new Query();
        log.info("query count sql:{}",query);
        return mongoTemplate.count(query, (Class)tClass);
    }

    @Override
    public T selectByPId(K pId) {
        Assert.notNull(pId,"");

        String fieldName = doGetPrimaryId();

        Query query = new Query();
        query.addCriteria(Criteria.where(fieldName).is(pId));
        List<T> rs = mongoTemplate.find(query, (Class) tClass);
        if(rs == null || rs.size() == 0){
            return null;
        } else if(rs.size() > 1) {
            throw BusinessException.build("查询到多个结果");
        }
        return rs.get(0);
    }

    @Override
    public List<T> selectAll() {
        Query query = new Query();
        query.addCriteria(CriteriaUtil.notDeleteCriteria());
        List<T> rs = mongoTemplate.find(query, (Class) tClass);
        return rs;
    }

    @Transactional
    @Override
    public long deleteByPId(K pId) throws BusinessException {
        Assert.notNull(pId,"");
        String fieldName = doGetPrimaryId();

        Query query = new Query();
        query.addCriteria(Criteria.where(fieldName).is(pId));
        query.addCriteria(CriteriaUtil.notDeleteCriteria());

        Update update = new Update();
        update.set("isDelete",true);

        UpdateResult ur = mongoTemplate.updateMulti(query, update,(Class) tClass);
        return ur.getModifiedCount();
    }

    @Transactional
    @Override
    public long deleteByPIds(String ids) throws BusinessException {
        Assert.notNull(ids,"");
        String fieldName = doGetPrimaryId();

        Long[] postIds = Convert.toLongArray(ids);
        if(postIds.length < 1)
            throw BusinessException.build("删除的数量不能小于1");

        Query query = new Query();
        query.addCriteria(Criteria.where(fieldName).in(postIds));
        query.addCriteria(CriteriaUtil.notDeleteCriteria());

        Update update = new Update();
        update.set("isDelete",true);

        UpdateResult ur = mongoTemplate.updateMulti(query, update,(Class) tClass);
        return ur.getModifiedCount();
    }

    @Override
    public long deleteAll() {
        Query query = new Query();
        Update update = new Update();
        update.set("isDelete",true);
        UpdateResult ur = mongoTemplate.updateMulti(query,update,(Class) tClass);
        return ur.getModifiedCount();
    }

    @Override
    public long clearAll() {
        DeleteResult dr = mongoTemplate.remove(new Query(), (Class) tClass);
        return dr.getDeletedCount();
    }

    @Transactional
    @Override
    public T insert(T target) {
        processionAnnotations(target);
        String fieldName = doGetPrimaryId();
        Field field = FieldUtils.getField((Class<?>) tClass,fieldName,true);
        Object keyValue = null;
        try {
            keyValue = FieldUtils.readField(field,target);
        } catch (IllegalAccessException e) {
            log.error("",e);
        }
        if(keyValue == null)
            throw BusinessException.build("主键" + fieldName + "不能为空");
        return mongoTemplate.save(target);
    }

    @Transactional
    @Override
    public T update(T target) {

        String fieldName = doGetPrimaryId();
        Field field = FieldUtils.getField((Class<?>) tClass,fieldName,true);
        Object keyValue = null;
        try {
            keyValue = FieldUtils.readField(field,target);
        } catch (IllegalAccessException e) {
            log.error("",e);
        }
        if(keyValue == null)
            throw BusinessException.build("读取主键异常");
        T dbValue = selectByPId((K)keyValue);

        Field ufield = FieldUtils.getField((Class<?>) tClass,"id",true);
        Object idValue = null;
        try {
            idValue = FieldUtils.readField(ufield,dbValue);
            FieldUtils.writeField(ufield,target,idValue);
        } catch (IllegalAccessException e) {
            log.error("",e);
            throw BusinessException.build("更新异常");
        }

        processionAnnotations(target);
        return mongoTemplate.save(target);
    }

    @Transactional
    @Override
    public long remove(K pId) {
        Assert.notNull(pId,"");
        String fieldName = doGetPrimaryId();

        Query query = new Query();
        query.addCriteria(Criteria.where(fieldName).is(pId));
        DeleteResult em = mongoTemplate.remove(query, (Class) tClass);
        return em.getDeletedCount();
    }

    @Override
    public long remove(List<K> pIds) {
        Assert.notNull(pIds,"");
        String fieldName = doGetPrimaryId();

        Query query = new Query();
        query.addCriteria(Criteria.where(fieldName).in(pIds));
        DeleteResult em = mongoTemplate.remove(query, (Class) tClass);
        return em.getDeletedCount();
    }

    @Transactional
    @Override
    public long removeByPIds(String ids) throws BusinessException {
        Assert.notNull(ids,"");
        String fieldName = doGetPrimaryId();

        Long[] postIds = Convert.toLongArray(ids);
        if(postIds.length < 1)
            throw BusinessException.build("删除的数量不能小于1");

        Query query = new Query();
        query.addCriteria(Criteria.where(fieldName).in(postIds));

        DeleteResult ur = mongoTemplate.remove(query, (Class) tClass);
        return ur.getDeletedCount();
    }

    private String doGetPrimaryId(){
        Field[] fields = FieldUtils.getFieldsWithAnnotation((Class) tClass, PrimaryId.class);
        if(fields ==null || fields.length == 0)
            throw BusinessException.build("PrimaryId主键没有设置");

        String fieldName = fields[0].getName();
        return  fieldName;
    }

    private void processionAnnotations(Object source) {
        if(source == null)
            return;
        ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                ReflectionUtils.makeAccessible(field);
                boolean isNew = false;
                if (field.isAnnotationPresent(AutoId.class)) {
                    if (!field.getType().equals(Long.class)) {
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

    public Long getNextId(String collectionName) {
        Query query = new Query(Criteria.where("collectionName").is(collectionName));
        Update update = new Update();
        update.inc("seqId", 1);
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);

        SysSeqInfo seq = mongoTemplate.findAndModify(query, update, options, SysSeqInfo.class);
        log.debug(collectionName + "generate id:" + seq.getSeqId());
        return seq.getSeqId();
    }

}
