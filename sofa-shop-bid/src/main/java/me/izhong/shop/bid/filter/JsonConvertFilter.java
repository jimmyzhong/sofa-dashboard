package me.izhong.shop.bid.filter;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.bid.frame.BidContext;
import me.izhong.shop.bid.frame.IFilterCallback;
import me.izhong.shop.bid.frame.IFilterChain;
import me.izhong.shop.bid.frame.IProcessFilter;
import me.izhong.shop.bid.pojo.BaseRequest;
import me.izhong.shop.bid.pojo.BaseResponse;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.stereotype.Service;

import javax.validation.*;
import java.util.Set;

@Slf4j
@Service
public class JsonConvertFilter implements IProcessFilter {
    private Validator validator;

    public JsonConvertFilter() {
        Configuration<HibernateValidatorConfiguration>config =
                Validation.byProvider(HibernateValidator.class).configure();

        ValidatorFactory vf =config.buildValidatorFactory();
        validator = vf.getValidator();
    }

    @Override
    public void process(BidContext context, final IFilterCallback callback,
                        IFilterChain filterChain) throws BusinessException {
        String jsonRequest = context.getJsonRequest();
        BaseRequest request;
        try {
            request = (BaseRequest)JSON.parseObject(jsonRequest, context.getReqClass());
        } catch (Exception e) {
            throw new BusinessException(400,
                    "参数不合法: " + e.getMessage());
        }
        context.setRequest(request);

        validateAndProcessResult(request);
        filterChain.process(context, new IFilterCallback() {
            @Override
            public void onPostProcess(BidContext context)
                    throws BusinessException {
                callback.onPostProcess(context);
            }
        });
    }


    private void validateAndProcessResult(Object request) throws BusinessException{
        // 检查输入值是否合法
        Set violations = validator.validate(request);
        if (violations.size() > 0) {
            StringBuffer buf = new StringBuffer();
            for (Object v : violations) {
                ConstraintViolation cv = (ConstraintViolation) v;
                buf.append(cv.getPropertyPath());
                buf.append(" ");
                buf.append(cv.getMessage());
                buf.append(", ");
            }
            String violationErrInfo = buf.toString();
            if (StringUtils.isNotBlank(violationErrInfo) && violationErrInfo.length() > 0) {
                violationErrInfo = violationErrInfo.substring(0, violationErrInfo.length() - 1);
            }
            throw new BusinessException(400,
                    "参数不合法: " + violationErrInfo);
        }
    }
}
