package me.izhong.shop.interceptor;

import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.config.JWTProperties;
import me.izhong.shop.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static me.izhong.shop.util.JWTUtils.*;

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

        String token = getToken(request);
        validateToken(token, request, jwtConfig);

        String userId = getUserId(token, jwtConfig.getSecret());
        if (StringUtils.isEmpty(userId)) {
            throw new RuntimeException("unable to find user id.");
        }

        userService.validateRole(requireUserLogin.roles(), userId);
        request.setAttribute("userId", userId);
        return true;
    }
}
