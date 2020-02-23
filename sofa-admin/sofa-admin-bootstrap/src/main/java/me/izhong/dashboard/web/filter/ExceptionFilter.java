package me.izhong.dashboard.web.filter;


import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.expection.user.UserHasNotPermissionException;
import me.izhong.dashboard.manage.expection.user.UserNotLoginException;
import me.izhong.dashboard.manage.util.HttpUtil;
import me.izhong.dashboard.web.bean.ResponseContainer;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
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
        if (e instanceof UserHasNotPermissionException) {
            UserHasNotPermissionException ce = (UserHasNotPermissionException) e;
            if (StringUtils.isNotBlank(ce.getPermission())) {
                msg = String.format("缺少权限[%s]", ce.getPermission());
            } else {
                msg = ce.getMessage();
            }
        } else if (e instanceof UnauthenticatedException) {
            UnauthenticatedException ce = (UnauthenticatedException) e;
            log.info("请求UnauthenticatedException异常", e);
            msg = "用户未登陆:" + ce.getMessage();
        } else if (e instanceof UnauthorizedException) {
            UnauthorizedException ce = (UnauthorizedException) e;
            log.info("请求UnauthorizedException异常", e);
            msg = "用户缺少权限:" + ce.getMessage();
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            msg = String.format("系统不支持请求[%s]", e.getMessage());
        } else if (e instanceof UserNotLoginException || e instanceof UserHasNotPermissionException) {
            log.error("请求UserNotLoginException异常");
            BusinessException bexp = (BusinessException) e;
            code = bexp.getCode();
            msg = bexp.getMessage();
        } else if (e instanceof BusinessException) {
            log.error("请求BusinessException异常", e);
            BusinessException bexp = (BusinessException) e;
            code = bexp.getCode();
            msg = bexp.getMessage();
        } else if(e instanceof BindException){
            BindException ex = (BindException)e;
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError error = errors.get(0);
            msg = error.getDefaultMessage();
        } else {
            log.error("请求异常", e);
            String message = e.getMessage();
            if (StringUtils.isNotBlank(message))
                msg = message;
        }

        if (HttpUtil.isAjaxRequest(httpServletRequest)) {
            ModelAndView v = new ModelAndView(new MappingJackson2JsonView());
            v.addObject("code", code);
            v.addObject("msg", msg);
            return v;
        } else {
            ModelAndView v = new ModelAndView();
            v.setViewName("error/exception");
            v.addObject("message", msg);
            return v;
        }

    }
}
