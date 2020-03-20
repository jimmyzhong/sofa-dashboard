package me.izhong.shop.filter;


import lombok.extern.slf4j.Slf4j;
import me.izhong.common.constant.ErrCode;
import me.izhong.common.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@Component
@Order(0)
public class ExceptionFilter implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {

        //httpServletResponse.getWriter().write("wrong" + e.getMessage());
        //httpServletResponse.flushBuffer();

        int code = ResponseContainer.FAIL_CODE;
        String msg = "系统异常";
         if (e instanceof BusinessException) {
            log.error("请求BusinessException异常", e);
            BusinessException bexp = (BusinessException) e;
            msg = bexp.getMessage();
            if (bexp.getCode() != ErrCode.FAIL_CODE) {
                code = bexp.getCode();
            }
        } else if(e instanceof BindException){
            BindException ex = (BindException)e;
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError error = errors.get(0);
            msg = error.getDefaultMessage();
        } else if(e instanceof NoHandlerFoundException){
             NoHandlerFoundException ex = (NoHandlerFoundException)e;
             msg = "资源没有找到";
         } else if(e instanceof RuntimeException){
             msg = "网络异常,请稍后再试";
             log.error("系统异常", e);
         } else {
            log.error("请求异常", e);
            String message = e.getMessage();
            if (StringUtils.isNotBlank(message))
                msg = message;
        }

        ModelAndView v = new ModelAndView(new MappingJackson2JsonView());
        v.addObject("code", code);
        v.addObject("msg", msg);
        v.setStatus(HttpStatus.OK);
        return v;
    }
}
