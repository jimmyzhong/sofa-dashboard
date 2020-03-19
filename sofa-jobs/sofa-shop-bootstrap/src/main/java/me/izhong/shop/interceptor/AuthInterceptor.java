package me.izhong.shop.interceptor;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.cache.SessionInfo;
import me.izhong.shop.consts.Constants;
import me.izhong.shop.config.JWTProperties;
import me.izhong.shop.consts.ErrorCode;
import me.izhong.shop.expection.UserNotLoginException;
import me.izhong.shop.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private JWTProperties jwtConfig;
    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequireUserLogin requireUserLogin = null;
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            requireUserLogin = handlerMethod.getMethodAnnotation(RequireUserLogin.class);
        }

        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 忽略没有被注解的请求, 不做后续token认证校验
        if (requireUserLogin == null) {
            return true;
        }

        String token = getTok(request);

        if(token == null) {
            throw new UserNotLoginException();
        }

        SessionInfo session = null;
        if((session = CacheUtil.getSessionInfo(token)) == null) {
            throw new UserNotLoginException("用户未登陆,或者登陆已经过期");
        }

        request.setAttribute("userId", session.getId());

//        validateToken(token, request, jwtConfig);
//
//        String userId = getUserId(token, jwtConfig.getSecret());
//        if (StringUtils.isEmpty(userId)) {
//            throw new RuntimeException("unable to find user id.");
//        }
//
//        userService.validateRole(requireUserLogin.roles(), userId);
//        request.setAttribute("userId", userId);
        return true;
    }

    private String getTok(HttpServletRequest request){
        return request.getHeader(Constants.AUTHORIZATION);
    }
}
