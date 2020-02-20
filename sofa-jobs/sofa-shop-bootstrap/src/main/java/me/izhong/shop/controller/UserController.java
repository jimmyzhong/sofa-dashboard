package me.izhong.shop.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.config.JWTProperties;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.impl.AuthService;
import me.izhong.shop.service.IUserService;
import me.izhong.shop.service.impl.ThirdPartyService;
import me.izhong.shop.util.JWTUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api
@RestController
@AjaxWrapper
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired private IUserService userService;
    @Autowired private AuthService authService;
    @Autowired private JWTProperties jwtConfig;
    @Autowired private ThirdPartyService thirdService;

    @PostMapping("/login")
    @AjaxWrapper
    @ApiOperation(value="验证用户登陆",httpMethod = "POST")
    public String login(String username, String password, HttpServletResponse response) {
//        User persistedUser = authService.attemptLogin(username,password);
//        String token = JWTUtils.createJWT(persistedUser.getId().toString(), persistedUser.getUserName(),
//                jwtConfig);
//        response.addHeader(JWTUtils.AUTH_HEADER_KEY, JWTUtils.TOKEN_PREFIX + token);
        return "Success.";
    }
//
//    @PostMapping("/register")
//    @ApiOperation(value="用户注册",httpMethod = "POST")
//    @ResponseBody
//    public String register(User user) {
//        validateInput(user);
//        userService.saveOrUpdate(user);
//        return "Success.";
//    }
//
    @PostMapping("/certify")
    @ApiOperation(value="用户实名认证",httpMethod = "POST")
    @ResponseBody
//    @RequireUserLogin
    public String certify(HttpServletRequest request) {
        String userId = (String)request.getAttribute("userId");
        if (StringUtils.isEmpty(userId)) {
            throw new RuntimeException("没有找到User Id");
        }
        User user = userService.findById(Long.valueOf(userId));
        userService.certify(user);
        return "Success.";
    }
//
    @PostMapping("/register/phoneCode")
    @ApiOperation(value="获取验证码",httpMethod = "POST")
    @ResponseBody
    public String getPhoneCode(@RequestParam("phone")String phoneNumber) {
        // TODO a valid phone number and valid attempt to send sms
        String randomNumber = RandomStringUtils.randomNumeric(6);
        String res = thirdService.sendSms(phoneNumber, new JSONObject(){{put("code", randomNumber);}});
        if (res != null) {
            log.info("sms res:" + res);
            return res;
        }
        return "Success.";
    }

//    private void validateInput(User user) {
//        userService.expectNew(user);
//    }

}
