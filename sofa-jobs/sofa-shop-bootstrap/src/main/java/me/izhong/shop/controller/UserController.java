package me.izhong.shop.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.domain.PageModel;
import me.izhong.common.domain.PageRequest;
import me.izhong.common.exception.BusinessException;
import me.izhong.common.model.UserInfo;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.cache.SessionInfo;
import me.izhong.shop.consts.Constants;
import me.izhong.shop.consts.ErrorCode;
import me.izhong.shop.consts.MoneyTypeEnum;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.PayRecord;
import me.izhong.shop.entity.User;
import me.izhong.shop.entity.UserMoney;
import me.izhong.shop.entity.UserScore;
import me.izhong.shop.service.IOrderService;
import me.izhong.shop.service.IUserService;
import me.izhong.shop.service.impl.AuthService;
import me.izhong.shop.service.impl.ThirdPartyService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static me.izhong.shop.util.ShareCodeUtil.decodeUserCode;

@Controller
@AjaxWrapper
@Api(value = "用户相关接口",description = "用户相关接口描述")
@RequestMapping(value = "/api/user")
@Slf4j
public class UserController {

    @Autowired private IUserService userService;
    @Autowired private IOrderService orderService;
    @Autowired private AuthService authService;
    @Autowired private ThirdPartyService thirdService;
    @Value("${password.pattern}") private String passwordPattern;

    @GetMapping(path="/current",  produces = "application/json;charset=UTF-8")
    @ResponseBody
    @RequireUserLogin
    @ApiOperation(value="获取当前登录用户信息",httpMethod = "GET")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION, value = "登录成功后response Authorization header", required = true)
    @ApiResponses({@ApiResponse(code=200, message = "当前用户信息, 比如: \n{\n" +
            "  \"avatar\": \"头像\",\n" +
            "  \"phone\": \"手机\",\n" +
            "  \"userId\": 0,\n" +
            "  \"nickName\": \"昵称\"\n" +
            "}")})
    public Map getCurrentUser(HttpServletRequest request) {
        SessionInfo session = CacheUtil.getSessionInfo(request);
        Long userId = session.getId();
        User user = userService.findById(userId);
        return new HashMap(){{
            put("userId",user.getId());
            put("phone",user.getPhone());
            put("nickName",user.getNickName());
            put("userCode",user.getUserCode());
            put("avatar",user.getAvatar());
        }};
    }

    @PostMapping(path="/update", consumes = "application/json")
    @ResponseBody
    @RequireUserLogin
    @ApiOperation(value="更新当前登录用户信息", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION, value = "登录成功后response Authorization header", required = true)
    public void updateUserInfo(
            @ApiParam(required = true, type = "object", value = "uer info, like: \n{" +
                    "  \"avatar\": \"头像\",\n" +
                    "  \"nickName\": \"姓名\"\n" +
                    "}")
            @RequestBody UserInfo userInfo, HttpServletRequest request) {

        SessionInfo session = CacheUtil.getSessionInfo(request);
        Long userId = session.getId();
        User user = userService.findById(userId);

        if (StringUtils.isNotBlank(userInfo.getAvatar())) {
            user.setAvatar(userInfo.getAvatar());
        }
        if (StringUtils.isNotBlank(userInfo.getNickName())) {
            user.setNickName(userInfo.getNickName());
        }
        userService.saveOrUpdate(user);
    }

    @PostMapping(value = "/avatar/upload", consumes = "multipart/form-data")
    @ResponseBody
    @RequireUserLogin
    @ApiOperation(value="上传用户头像", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION, value = "登录成功后response Authorization header", required = true)
    public Map uploadFile(@RequestParam("file") MultipartFile file, HttpServletRequest request){
        log.info("upload content type:" + file.getContentType());
        if (file.getContentType()==null || !file.getContentType().contains("image")) {
            throw BusinessException.build("无法识别图片");
        }
        SessionInfo session = CacheUtil.getSessionInfo(request);
        Long userId = session.getId();
        User user = userService.findById(userId);

        String fileName = "shop/upload/avatar/" + user.getId() + ".jpg";
        String fileUploadedUrl = thirdService.uploadFile(fileName, file);

        user.setAvatar(fileUploadedUrl);
        userService.saveOrUpdate(user);
        return new HashMap(){{
            put("avatar",fileUploadedUrl);
        }};
    }

    @PostMapping("/login")
    @ApiOperation(value="用户登陆",httpMethod = "POST",
            notes = "用户登陆使用的接口(登陆后返回的token在后续需要登陆接口head里面的Authorization字段上送)",consumes = "application/json")
    @ApiResponses({
            @ApiResponse(code = 200, message = "正常应答，响应数据在data节点{token}, 例如:\n" + "{\n" +
                    "  \"code\":200,\n" +
                    "  \"data\":{\n" +
                    "      \"token\":\"11a977e694bd241e4be8ba99bb571e051\"\n" +
                    "  },\n" +
                    "  \"msg\":\"成功\"\n" +
                    "}")
    })
    public Map login(
            @ApiParam(required = true, type = "object", value = "login request body, like: \n{" +
                    "  \"phone\": \"12345678\",\n" +
                    "  \"password\": \"12345678\"\n" +
                    "}")
            @RequestBody Map<String,String> params) {
        String phone = params.get("phone");
        String password = params.get("password");
        //TODO verify login attempt, e.g. exceed max count
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(password)) {
            throw BusinessException.build("用户名密码不能为空");
        }

        User persistedUser = authService.attemptLogin(phone, password);

        String token = userService.setUserSession(persistedUser);

        return new HashMap(){{
            put("token",token);
            put("userId",persistedUser.getId());
            put("phone",persistedUser.getPhone());
            put("nickName",persistedUser.getNickName());
            put("avatar",persistedUser.getAvatar());
        }};
    }

    private String getToken(User persistedUser) {
        return persistedUser.getId() + UUID.randomUUID().toString().replaceAll("-", "");
    }

    @PostMapping("/password/valid")
    @ApiOperation(value="密码是否规范",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", dataType = "string"),

    })
    public Map checkPasswordPattern(@RequestBody Map<String,String> params) {
        String password = params.get("password");
        Map map = new HashMap();
        map.put("valid", password.matches(passwordPattern));
        return map;
    }

    @RequireUserLogin
    @PostMapping("/alipay/account")
    @ApiOperation(value="设置支付宝账号",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "alipayAccount", value = "账号", dataType = "string"),
            @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
                    value = "登录成功后response Authorization header", required = true)
    })
    public void setAlipayAccount(@RequestBody Map<String,String> params, HttpServletRequest request) {
        String alipayAccount = params.get("alipayAccount");
        String alipayName = params.get("alipayName");
        if (StringUtils.isEmpty(alipayAccount) || StringUtils.isEmpty(alipayName)) {
            throw BusinessException.build("账号为空");
        }
        userService.setAlipayAccount(CacheUtil.getSessionInfo(request).getId(), alipayAccount, alipayName);
    }

    @RequireUserLogin
    @GetMapping("/alipay/account")
    @ApiOperation(value="获取支付宝账号",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
                    value = "登录成功后response Authorization header", required = true)
    })
    public Map getAlipayAccount( HttpServletRequest request) {
        User user = userService.findById(CacheUtil.getSessionInfo(request).getId());
        if (StringUtils.isEmpty(user.getAlipayAccount())) {
            throw BusinessException.build(ErrorCode.USER_ALIPAY_ACCOUNT_NOT_EXISTS, "请绑定支付宝账号");
        }
        return new HashMap(){{
            put("alipayName",user.getAlipayName());
            put("alipayAccount",user.getAlipayAccount());
        }};
    }

    @PostMapping("/register")
    @ApiOperation(value="用户注册",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号",  dataType = "string"),
            @ApiImplicitParam(name = "password", value = "密码", dataType = "string"),
            @ApiImplicitParam(name = "token", value = "token", dataType = "string"),
            @ApiImplicitParam(name = "code", value = "验证码", dataType = "string"),
            @ApiImplicitParam(name = "refUserCode", value = "邀请人注册码", dataType = "string"),

    })
    public Map register(@RequestBody Map<String,String> params) {
        String phone = params.get("phone");
        String token = params.get("token");
        String password = params.get("password");
        String code = params.get("code");
        String nickName = params.get("nickName");
        String refUserCode = params.get("refUserCode");

        User user = new User();
        user.setPhone(phone);
        user.setPassword(password);

        String nickPhone = nickName;
        try {
            phone = phone.trim();
            nickPhone = phone.substring(0, 3) + "****" + phone.substring(7);
        } finally {
        }
        user.setNickName(nickPhone);
        ensureRequiredFieldWhenRegistering(user);

        if (!password.matches(passwordPattern)) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_NOT_MATCH_PATTERN, "密码过于简单");
        }

        verifyPhoneCode(token,phone,code);

        if (StringUtils.isEmpty(refUserCode)) {
            throw BusinessException.build("没有邀请码不能注册");
        }
        Long refUserId = decodeUserCode(refUserCode.toUpperCase());
        if (refUserId == null) {
            throw BusinessException.build("邀请码不存在");
        }
        User refUser = userService.findById(refUserId);
        if (refUser == null) {
            throw BusinessException.build("邀请码不存在");
        }
        user.setInviteUserId(refUserId);

        final User dbUser = userService.registerUser(user);

        SessionInfo session = new SessionInfo();
        session.setLasttimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        String loginToken = getToken(dbUser);
        session.setId(dbUser.getId());
        CacheUtil.setSessionInfo(token, session);
        return new HashMap(){{
            put("token",loginToken);
            put("userId",dbUser.getId());
            put("phone",dbUser.getPhone());
            put("nickName",dbUser.getNickName());
            put("userCode",dbUser.getUserCode());
            put("avatar",dbUser.getAvatar());
        }};
    }

    @PostMapping("/resetPassword")
    @ApiOperation(value="重置密码",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", dataType = "string"),
            @ApiImplicitParam(name = "password", value = "密码", dataType = "string"),
            @ApiImplicitParam(name = "code", value = "验证码", dataType = "string"),
    })
    public void resetPassword(@RequestBody Map<String,String> params) {
        String token = params.get("token");
        String phone = params.get("phone");
        String password = params.get("password");
        User user = new User();
        user.setPhone(phone);
        user.setPassword(password);
        ensureRequiredFieldWhenRegistering(user);
        if (!user.getPassword().matches(passwordPattern)) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_NOT_MATCH_PATTERN, "密码过于简单");
        }

        verifyPhoneCode(token,phone, params.get("code"));

        user = userService.expectExists(user);
        user.setPassword(password);
        user.encryptUserPassword();
        userService.saveOrUpdate(user);
    }

    @PostMapping("/resetAssetPassword")
    @ApiOperation(value="重置支付密码",httpMethod = "POST")
    @RequireUserLogin
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", dataType = "string"),
            @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
                    value = "登录成功后response Authorization header", required = true)
    })
    public void resetAssetPassword(@RequestBody Map<String,String> params, HttpServletRequest request) {
        String password = params.get("password");
        if (!password.matches(passwordPattern)) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_NOT_MATCH_PATTERN, "密码过于简单");
        }
        userService.setAssetPassword(CacheUtil.getSessionInfo(request).getId(), password);
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
            @ApiImplicitParam(name = "name", value = "姓名",  dataType = "string"),
            @ApiImplicitParam(name = "idCard", value = "身份证号码",  dataType = "string"),
            @ApiImplicitParam(paramType = "header", dataType = "string", name = Constants.AUTHORIZATION, value = "登录成功后token", required = true)
    })
    public Map certify(@RequestBody Map<String,String> params, HttpServletRequest request) {
        SessionInfo session = CacheUtil.getSessionInfo(request);
        Long userId = session.getId();
        User user = userService.findById(Long.valueOf(userId));
        user.setName(params.get("name"));
        user.setIdentityID(params.get("idCard"));
        boolean certified = userService.certify(user);
        return new HashMap(){{
            put("certified", certified);
            put("name", user.getName());
            put("idCard", user.getIdentityID());
        }};
    }

    @GetMapping("/certify")
    @RequireUserLogin
    @ApiOperation(value="用户实名认证",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "string", name = Constants.AUTHORIZATION, value = "登录成功后token", required = true)
    })
    public Map getCertified(HttpServletRequest request) {
        SessionInfo session = CacheUtil.getSessionInfo(request);
        Long userId = session.getId();
        User user = userService.findById(Long.valueOf(userId));

        return new HashMap(){{
            put("certified", user.getIsCertified());
            put("name", user.getName());
            put("idCard", user.getIdentityID());
        }};
    }

    @RequestMapping("/phoneCode")
    @ApiOperation(value="获取验证码",httpMethod = "GET")
    public Map getPhoneCode(@RequestBody Map<String,String> params) {
        String phone = params.get("phone");
        String resetPass = params.get("resetPass");
        String randomNumber = RandomStringUtils.randomNumeric(6);

        thirdService.sendSms(phone, new JSONObject(){{put("code", randomNumber);}}, StringUtils.equals(resetPass,"true"));

        String randomToken = RandomStringUtils.randomNumeric(32) + "_" + phone;
        CacheUtil.setSmsInfo(randomToken,randomNumber);
        return new HashMap(){{
            put("token",randomToken);
        }};
    }

    @RequestMapping("/expectNew")
    @ApiOperation(value="判断用户手机是否存在",httpMethod = "GET")
    public void expectNew(@RequestBody Map<String,String> params) {
        String phoneNumber = params.get("phone");
        if (StringUtils.isEmpty(phoneNumber)) {
            throw BusinessException.build("输入不能为空");
        }
        User user = new User();
        user.setPhone(phoneNumber);
        user.setEmail(phoneNumber);
        user.setLoginName(phoneNumber);
        userService.expectNew(user);
    }


    @PostMapping("/list")
    @RequireUserLogin
    @ResponseBody
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public PageModel<User> list(@RequestBody PageQueryParamDTO pageRequest, HttpServletRequest request) {
        SessionInfo session = CacheUtil.getSessionInfo(request);
        Long userId = session.getId();
        return userService.list(userId, pageRequest);
    }

    @GetMapping("/money")
    @RequireUserLogin
    @ResponseBody
    @ApiOperation(value = "获取当前登录用户余额", httpMethod = "GET")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public UserMoney money(HttpServletRequest request) {
        SessionInfo session = CacheUtil.getSessionInfo(request);
        Long userId = session.getId();
        return userService.findMoneyByUserId(userId);
    }

    @PostMapping("/money/detail")
    @RequireUserLogin
    @ResponseBody
    @ApiOperation(value = "获取当前登录用户返现明细", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public PageModel<PayRecord> moneyReturnDetail(@RequestBody PageQueryParamDTO pageRequest, HttpServletRequest request) {
        SessionInfo session = CacheUtil.getSessionInfo(request);
        Long userId = session.getId();
        Set<MoneyTypeEnum> types = new HashSet<>();
        if (pageRequest.getMoneyTypes()!= null) {
            types = pageRequest.getMoneyTypes().stream().map(i->{
                MoneyTypeEnum type = MoneyTypeEnum.getByType(i);
                if (type == null) {
                    throw BusinessException.build("类型信息不正确");
                }
                return type;
            }).collect(Collectors.toSet());
        }
        return userService.listMoneyReturnRecord(userId, pageRequest, types);
    }

    @GetMapping("/score")
    @RequireUserLogin
    @ResponseBody
    @ApiOperation(value = "获取当前登录用户积分", httpMethod = "GET")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public UserScore score(HttpServletRequest request) {
        SessionInfo session = CacheUtil.getSessionInfo(request);
        Long userId = session.getId();
        return userService.findScoreByUserId(userId);
    }

    @PostMapping("/score/detail")
    @RequireUserLogin
    @ResponseBody
    @ApiOperation(value = "获取当前登录用户积分明细", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public PageModel<PayRecord> scoreReturnDetail(@RequestBody PageQueryParamDTO pageRequest, HttpServletRequest request) {
        SessionInfo session = CacheUtil.getSessionInfo(request);
        Long userId = session.getId();
        return userService.listScoreReturnRecord(userId, pageRequest);
    }

    private void verifyPhoneCode( String token, String phone , String code) {
//        SessionInfo sessionInfo = CacheUtil.getSessionInfo("phone" + phoneNumber);
//        if (sessionInfo == null || StringUtils.isEmpty(sessionInfo.getData())
//            || !sessionInfo.getData().equalsIgnoreCase(code)) {
//            log.warn(phoneNumber + " invalid phone code " + code);
//            throw BusinessException.build("验证失败");
//        }
//        return "Success.";
        if(StringUtils.isBlank(phone)) {
            throw BusinessException.build("手机号不能为空");
        }
        if(StringUtils.isBlank(token)) {
            log.info("token不能为空");
            throw BusinessException.build("请获取验证码");
        }
        if(!token.endsWith(phone)){
            log.info("非法请求，用户换手机号了，要用发验证码的手机号");
            throw BusinessException.build("非法请求，要用发验证码的手机号");
        }
        String cacheCode = CacheUtil.getSmsInfo(token);
        if(cacheCode == null){
            throw BusinessException.build("短信验证码过期");
        }
        if(!StringUtils.equals(cacheCode,code)){
            throw BusinessException.build("短信验证码不正确");
        }
    }

}
