package me.izhong.shop.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.model.UserInfo;
import me.izhong.db.common.exception.BusinessException;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.cache.SessionInfo;
import me.izhong.shop.config.Constants;
import me.izhong.shop.entity.User;
import me.izhong.shop.service.IUserService;
import me.izhong.shop.service.impl.AuthService;
import me.izhong.shop.service.impl.ThirdPartyService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@AjaxWrapper
@Api(value = "用户相关接口",description = "用户相关接口描述")
@RequestMapping(value = "/api/user")
//, consumes = "application/json"
@Slf4j
public class UserController {

    @Autowired private IUserService userService;
    @Autowired private AuthService authService;
    @Autowired private ThirdPartyService thirdService;

    @GetMapping(path="/", consumes = "application/json", produces = "application/json;charset=UTF-8")
    @ResponseBody
    @RequireUserLogin
    @ApiOperation(value="获取当前登录用户信息",httpMethod = "GET")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION, value = "登录成功后response Authorization header", required = true)
    public UserInfo getCurrentUser(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        User user = userService.findById(userId);
        UserInfo userInfo = new UserInfo();
        userInfo.setAvatar(user.getAvatar());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhoneNumber(user.getPhone());
        userInfo.setLoginName(user.getLoginName());
        userInfo.setUserName(user.getName());
        return userInfo;
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("没有找到User Id");
        }
        return userId;
    }

    @PostMapping("/")
    @ResponseBody
    @RequireUserLogin
    @ApiOperation(value="更新当前登录用户信息", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION, value = "登录成功后response Authorization header", required = true)
    public String updateUserInfo(@RequestBody UserInfo userInfo, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        User user = userService.findById(userId);

        //make sure email, telephone and login name are unique
        User test = new User();
        test.setPhone(userInfo.getPhoneNumber());
        test.setEmail(userInfo.getEmail());
        test.setLoginName(userInfo.getLoginName());
        userService.expectNew(test);

        user.setLoginName(userInfo.getLoginName());
        user.setEmail(userInfo.getEmail());
        user.setPhone(userInfo.getPhoneNumber());
        user.setAvatar(userInfo.getAvatar());

        if (!user.getIsCertified() && !StringUtils.isEmpty(userInfo.getUserName())) {
            user.setName(userInfo.getUserName());
        }

        userService.saveOrUpdate(user);

        return "Success.";
    }



    @PostMapping("/login")
    @ApiOperation(value="用户登陆",httpMethod = "POST",
            notes = "用户登陆使用的接口(登陆后返回的token在后续需要登陆接口head里面的Authorization字段上送)",consumes = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "string"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "string"),
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "正常应答，响应数据在data节点{token}")
    })
//    {
//        "code": 200,
//            "data": {
//        "token": "11a977e694bd241e4be8ba99bb571e051"
//    },
//        "msg": "成功"
//    }
    public Map login(@ApiParam(hidden = true) @RequestBody Map<String,String> params, HttpServletResponse response) {
        String phone = params.get("phone");
        String password = params.get("password");
        //TODO verify login attempt, e.g. exceed max count
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(password)) {
            throw BusinessException.build("用户名密码不能为空");
        }
        User persistedUser = authService.attemptLogin(phone, password);
        SessionInfo session = new SessionInfo();
        session.setLasttimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        String token = persistedUser.getId() + UUID.randomUUID().toString().replaceAll("-","");
        session.setId(persistedUser.getId());
        CacheUtil.setSessionInfo(token, session);
        //response.addHeader(Constants.AUTHORIZATION, token);
        return new HashMap(){{
            put("token",token);
        }};
    }

    @PostMapping("/register")
    @ApiOperation(value="用户注册",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "string"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "string"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, dataType = "string"),
    })
    public String register(@RequestBody Map<String,String> params) {
        User user = new User();
        user.setPhone(params.get("phone"));
        user.setPassword(params.get("password"));
        ensureRequiredFieldWhenRegistering(user);
        verifyPhoneCode(params.get("phone"), params.get("code"));

        userService.expectNew(user);
        user.setId(null);
        user.encryptUserPassword();
        userService.saveOrUpdate(user);
        return "Success.";
    }

    @PostMapping("/resetPassword")
    @ApiOperation(value="重置密码",httpMethod = "POST")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "string"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "string"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, dataType = "string"),
    })
    public String resetPassword(@RequestBody Map<String,String> params) {
        String phone = params.get("phone");
        String password = params.get("password");
        User user = new User();
        user.setPhone(phone);
        user.setPassword(password);
        ensureRequiredFieldWhenRegistering(user);
        verifyPhoneCode(phone, params.get("code"));

        user = userService.expectExists(user);
        user.setPassword(password);
        user.encryptUserPassword();
        userService.saveOrUpdate(user);
        return "Success.";
    }

    private void ensureRequiredFieldWhenRegistering(User user) {
        if (StringUtils.isEmpty(user.getPassword())){
            throw BusinessException.build("密码不能为空");
        }
        if (StringUtils.isEmpty(user.getLoginName())
                && StringUtils.isEmpty(user.getEmail()) && StringUtils.isEmpty(user.getPhone())) {
            throw BusinessException.build("用户名不能为空");
        }
    }

    @PostMapping("/certify")
    @RequireUserLogin
    @ApiOperation(value="用户实名认证",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "姓名", required = true, dataType = "string"),
            @ApiImplicitParam(name = "idCard", value = "身份证号码", required = true, dataType = "string"),
            @ApiImplicitParam(paramType = "header", dataType = "string", name = Constants.AUTHORIZATION, value = "登录成功后token", required = true)
    })
    public String certify(@RequestBody Map<String,String> params, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        User user = userService.findById(Long.valueOf(userId));
        user.setName(params.get("name"));
        user.setIdentityID(params.get("idCard"));
        userService.certify(user);
        return "Success.";
    }

    @GetMapping("/register/phoneCode")
    @ApiOperation(value="获取验证码",httpMethod = "GET")
    public String getPhoneCode(@RequestParam("phone")String phoneNumber,
                               @RequestParam(name="resetPass", defaultValue = "false")Boolean resetPass) {
        // TODO a valid phone number and valid attempt to send sms
        String randomNumber = RandomStringUtils.randomNumeric(6);
        String res = thirdService.sendSms(phoneNumber, new JSONObject(){{put("code", randomNumber);}}, resetPass);
        if (res != null) {
            log.info("sms res:" + res);
            return res;
        }
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:SS")));
        sessionInfo.setData(randomNumber);
        //TODO 过期时间 ？
        CacheUtil.setSessionInfo("phone" + phoneNumber, sessionInfo);
        return "Success.";
    }

    @GetMapping("/expectNew")
    @ApiOperation(value="判断用户名是否存在",httpMethod = "GET")
    @ResponseBody
    public String expectNew(@RequestParam("phone")String phoneNumber) {
        if (StringUtils.isEmpty(phoneNumber)) {
            throw BusinessException.build("输入不能为空");
        }
        User user = new User();
        user.setPhone(phoneNumber);
        user.setEmail(phoneNumber);
        user.setLoginName(phoneNumber);
        userService.expectNew(user);
        return "Success.";
    }

    private String verifyPhoneCode(@RequestParam("phone")String phoneNumber, @RequestParam("code")String code) {
        SessionInfo sessionInfo = CacheUtil.getSessionInfo("phone" + phoneNumber);
        if (sessionInfo == null || StringUtils.isEmpty(sessionInfo.getData())
            || !sessionInfo.getData().equalsIgnoreCase(code)) {
            log.warn(phoneNumber + " invalid phone code " + code);
            throw BusinessException.build("验证失败");
        }
        return "Success.";
    }

}
