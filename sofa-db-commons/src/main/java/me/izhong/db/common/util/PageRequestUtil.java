package me.izhong.db.common.util;

import me.izhong.common.util.TimeUtil;
import me.izhong.common.domain.PageRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;

@Slf4j
public class PageRequestUtil {

    public static PageRequest fromRequestIgnorePageSize(HttpServletRequest request) {
        return doFromRequest(request,true);
    }

    public static PageRequest fromRequest(HttpServletRequest request) {
        return doFromRequest(request,false);
    }


    private static PageRequest doFromRequest(HttpServletRequest request, boolean ignorePageSize) {
        PageRequest pageRequest = new PageRequest();
        if(ignorePageSize) {
            pageRequest.setPageSize(Integer.MAX_VALUE);
            pageRequest.setPageNum(1L);
        } else {
            String pageSize = request.getParameter("pageSize");
            if (StringUtils.isNotBlank(pageSize)) {
                try {
                    pageRequest.pageSize(Long.valueOf(pageSize));
                } catch (Exception e) {

                }
            }
            String pageNum = request.getParameter("pageNum");
            if (StringUtils.isNotBlank(pageNum)) {
                try {
                    pageRequest.pageNum(Long.valueOf(pageNum));
                } catch (Exception e) {

                }
            }
        }
        String orderByColumn = request.getParameter("orderByColumn");
        if (StringUtils.isNotBlank(orderByColumn)) {
            pageRequest.orderBy(orderByColumn);
        }
        String isAsc = request.getParameter("isAsc");
        if (StringUtils.isNotBlank(isAsc)) {
            pageRequest.isAsc(isAsc);
        }
        String status = request.getParameter("status");
        if (StringUtils.isNotBlank(status)) {
            pageRequest.status(status);
        }

        String beginTime = request.getParameter("beginTime");
        if (StringUtils.isNotBlank(beginTime)) {
            pageRequest.beginDate(TimeUtil.parseDate_yyyyMMdd_hl(beginTime));
        }

        String endTime = request.getParameter("endTime");
        if (StringUtils.isNotBlank(endTime)) {
            pageRequest.endDate(TimeUtil.parseDate_yyyyMMdd_hl(endTime));
        }

        return pageRequest;
    }

    public static void injectQuery(PageRequest request, Query query) {

        boolean succ  = request.alreadyInjectToQuery.compareAndSet(false,true);
        //if(!succ)
        //    return;
        //if (StringUtils.isNotBlank(status)) {
        //    query.addCriteria(Criteria.where("status").is(status));
        //}

        if (request.getBeginDate() != null && request.getEndDate() != null) {
            query.addCriteria(Criteria.where("createTime").gte(request.getBeginDate()).lte(request.getEndDate() ));
        } else if (request.getBeginDate() != null) {
            query.addCriteria(Criteria.where("createTime").gte(request.getBeginDate()));
        } else if (request.getEndDate()  != null) {
            query.addCriteria(Criteria.where("createTime").lte(request.getEndDate() ));
        }

        if (StringUtils.isNotBlank(request.getOrderByColumn())) {
            query.with(
                    new Sort(
                            Sort.Direction.fromOptionalString(request.getIsAsc()).orElse(Sort.Direction.ASC), request.getOrderByColumn()));
        }
        if (request.getPageNum() < 1)
            request.setPageNum(1);
        if (request.getPageSize() <= 0)
            request.setPageSize(10);
        if (request.getPageSize() > Integer.MAX_VALUE)
            request.setPageSize(Integer.MAX_VALUE);
        Pageable pageableRequest = org.springframework.data.domain.PageRequest.of((int) request.getPageNum() - 1, (int) request.getPageSize());
        query.with(pageableRequest);

        if(request.getDepts() != null && request.getDepts().size() > 0) {
            try {
                Field cField = FieldUtils.getField(query.getClass(), "criteria",true);
                cField.setAccessible(true);
                Object v = cField.get(query);
                Map<String, CriteriaDefinition> mq = (Map<String, CriteriaDefinition> )v;

                Criteria reqDeptCri = null;
                if (mq != null) {
                    Criteria[] criterias = mq.values().toArray(new Criteria[]{});
                    for(Criteria c : criterias){
                        if(StringUtils.equals(c.getKey(),"deptId")) {
                            reqDeptCri = c;
                            break;
                        }
                    }
                }

                if(reqDeptCri != null) {
                    //reqDeptCri.andOperator()
                    //Long[] xx = new Long[]{34L,35L};
                    //reqDeptCri.getCriteriaObject();
                    //reqDeptCri.in(xx);
                    reqDeptCri.andOperator(Criteria.where("deptId").in(request.getDepts()));
                } else {
                    Criteria addCri = Criteria.where("deptId").in(request.getDepts());
                    query.addCriteria(addCri);
                }


                //log.info("{}",mq);

            } catch (Exception e) {
                log.error("",e);
            }
        }

    }

    public static void injectAggregation(PageRequest request, List<AggregationOperation> aggregationOperations) {
        boolean succ  = request.alreadyInjectToQuery.compareAndSet(false,true);
        //if(!succ)
        //    return;

        if (StringUtils.isNotBlank(request.getStatus())) {
            aggregationOperations.add(Aggregation.match(Criteria.where("status").is(request.getStatus())));
        }
        Date beginDate = request.getBeginDate();
        Date endDate = request.getEndDate();
        if (beginDate != null && endDate != null) {
            aggregationOperations.add(Aggregation.match(Criteria.where("createTime").gte(beginDate).lte(endDate)));
        } else if (beginDate != null) {
            aggregationOperations.add(Aggregation.match(Criteria.where("createTime").gte(beginDate)));
        } else if (endDate != null) {
            aggregationOperations.add(Aggregation.match(Criteria.where("createTime").lte(endDate)));
        }
        injectAggregationOnlyPage(request, aggregationOperations);
    }

    public static void injectAggregationOnlyPage(PageRequest request, List<AggregationOperation> aggregationOperations) {
        boolean succ  = request.alreadyInjectToQuery.compareAndSet(false,true);
        //if(!succ)
        //    return;

        if (StringUtils.isNotBlank(request.getOrderByColumn())) {
            aggregationOperations.add(Aggregation.sort(
                    new Sort(Sort.Direction.fromOptionalString(request.getIsAsc()).orElse(Sort.Direction.ASC), request.getOrderByColumn())));
        }
        long pageNum = request.getPageNum();
        long pageSize = request.getPageSize();
        if (request.getPageNum() < 1)
            pageNum = 1;
        if (pageSize > Integer.MAX_VALUE)
            pageSize = Integer.MAX_VALUE;
        Pageable pageableRequest = org.springframework.data.domain.PageRequest.of((int) pageNum - 1, (int) pageSize);
        aggregationOperations.add(Aggregation.skip((pageNum - 1) * pageSize));
        aggregationOperations.add(Aggregation.limit(pageSize));

        if(request.getDepts() != null && request.getDepts().size() > 0) {
            aggregationOperations.add(Aggregation.match(Criteria.where("deptId").in(request.getDepts())));
        }
    }
}
