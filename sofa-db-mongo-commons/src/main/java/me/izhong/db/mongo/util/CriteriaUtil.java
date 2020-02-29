package me.izhong.db.mongo.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;

import java.lang.reflect.Field;
import java.util.Map;

@Slf4j
public class CriteriaUtil {

    public static Criteria notDeleteCriteria() {
        Criteria cr = new Criteria();
        cr.orOperator(Criteria.where("isDelete").is(false), Criteria.where("isDelete").is(null));
        return cr;
    }

    public static Criteria deleteCriteria() {
        return Criteria.where("isDelete").is(true);
    }

    public static void addCriteria(Query query, Criteria criteria){
        try {
            Field cField = FieldUtils.getField(query.getClass(), "criteria",true);
            cField.setAccessible(true);
            Object v = cField.get(query);
            Map<String, CriteriaDefinition> mq = (Map<String, CriteriaDefinition> )v;

            String cKey = criteria.getKey();

            Criteria reqDeptCri = null;
            if (mq != null) {
                Criteria[] criterias = mq.values().toArray(new Criteria[]{});
                for(Criteria c : criterias){
                    if(StringUtils.equals(c.getKey(),cKey)) {
                        reqDeptCri = c;
                        break;
                    }
                }
            }

            if(reqDeptCri != null) {
                if(!reqDeptCri.equals(criteria))
                   reqDeptCri.andOperator(criteria);
            } else {
                query.addCriteria(criteria);
            }

        } catch (Exception e) {
            log.error("",e);
        }
    }

}
