package me.izhong.shop.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.config.JWTProperties;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.impl.AuthService;
import me.izhong.shop.service.IUserService;
import me.izhong.shop.service.impl.ThirdPartyService;
import me.izhong.shop.util.JWTUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api("用户登陆")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    public UserController(IUserService userService,
                          AuthService authService,
                          ThirdPartyService thirdService,
                          JWTProperties jwtConfig) {
        this.authService = authService;
        this.jwtConfig = jwtConfig;
        this.userService = userService;
        this.thirdService = thirdService;
    }

    private IUserService userService;
    private AuthService authService;
    private JWTProperties jwtConfig;
    private ThirdPartyService thirdService;

    @PostMapping("/login")
    @ResponseBody
    @ApiOperation("验证用户登陆")
    public String login(User user, HttpServletResponse response) {
        User persistedUser = authService.attemptLogin(user);
        String token = JWTUtils.createJWT(persistedUser.getId().toString(), persistedUser.getUserName(),
                jwtConfig);
        response.addHeader(JWTUtils.AUTH_HEADER_KEY, JWTUtils.TOKEN_PREFIX + token);
        return "Success.";
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    @ResponseBody
    public String register(User user) {
        validateInput(user);
        userService.saveOrUpdate(user);
        return "Success.";
    }

    @PostMapping("/certify")
    @ApiOperation("用户实名认证")
    @ResponseBody
    @RequireUserLogin
    public String certify(HttpServletRequest request) {
        String userId = (String)request.getAttribute("userId");
        if (StringUtils.isEmpty(userId)) {
            throw new RuntimeException("没有找到User Id");
        }
        User user = userService.findById(Long.valueOf(userId));
        userService.certify(user);
        return "Success.";
    }

    @PostMapping("/register/phoneCode")
    @ApiOperation("获取验证码")
    @ResponseBody
    public String getPhoneCode(User user) {
        // TODO a valid phone number and valid attempt to send sms
        String res = thirdService.sendSms(user.getPhone());
        if (res != null) {
            return res;
        }
        return "Success.";
    }

    private void validateInput(User user) {
        userService.expectNew(user);
    }

}
