package me.izhong.shop.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.exception.BusinessException;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.cache.SessionInfo;
import me.izhong.shop.config.AliCloudProperties;
import me.izhong.shop.consts.Constants;
import me.izhong.shop.entity.Lots;
import me.izhong.shop.service.impl.job.LotsServiceHelper;
import me.izhong.shop.util.AliCloudUtils;
import me.izhong.shop.util.ShareCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AliCloudProperties cloudProperties;

    @Autowired
    private LotsServiceHelper lotsServiceHelper;

    @GetMapping("/usercode/{id}")
    public String userCode(@PathVariable("id") Long id) {
        return ShareCodeUtil.generateUserCode(id);
    }

    @GetMapping("/userid/{code}")
    public Long userId(@PathVariable("code") String code) {
        return ShareCodeUtil.decodeUserCode(code);
    }


    @GetMapping("/bucket/list")
    public List<String> listBuckets(){
        return AliCloudUtils.instance.listBucket(cloudProperties);
    }

    @GetMapping("/bucket/{bucketName}")
    public List<String> listBuckets(@PathVariable("bucketName") String bucketName,
                                    @RequestParam(value = "prefix", defaultValue = "") String prefix){
        return AliCloudUtils.instance.listObjsOfBucket(cloudProperties, bucketName, prefix);
    }

    @PostMapping("/bucket/upload")
    @ResponseBody
    public void uploadFile(@RequestParam("file") MultipartFile file){
        String fileName = "shop/upload/avatar/1.jpg";
        try {
            AliCloudUtils.instance.uploadStream(cloudProperties, fileName, file.getInputStream());
        } catch (IOException e) {
            throw BusinessException.build("upload file failed.");
        }
    }

    @GetMapping("/session")
    public Map getByName(HttpServletRequest request)  throws Exception{
        log.info(">>>> start  getByName <<<<",request.getParameter("name"));

        HttpSession httpSession = request.getSession();
        String sessionId = httpSession.getId();
        httpSession.setAttribute("name",request.getParameter("name"));


        //ValueOperations objOps = redisTemplate.opsForValue();

        HashOperations hashOps = redisTemplate.opsForHash();
        hashOps.put("hash","hashKey","hashValue");

        Map bMap = (Map)hashOps.get("xxx-data", "tome");

        Map map = new HashMap();
        if(bMap !=null) {
            String last_local_ip = (String) bMap.get("local_ip");
            String last_access_time = (String) bMap.get("access_time");
            map.put("last_local_ip", last_local_ip);
            map.put("last_access_time", last_access_time);
        }


        map.put("local_ip", InetAddress.getLocalHost().toString());
        map.put("access_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        //objOps.set("xxx-data", map);
        //objOps.set("xxx-data", map, 180, TimeUnit.SECONDS);
        hashOps.put("xxx-data", "tome", map);


        //redisTemplate.delete("city");

        return map;
    }

    @GetMapping("/ajax")
    @AjaxWrapper
    public Map ajaxWrapper()  throws Exception{
        Map map = new HashMap();
        map.put("local_ip", InetAddress.getLocalHost().toString());
        return map;
    }

    @RequestMapping("/testcache")
    @AjaxWrapper
    @RequireUserLogin
    public SessionInfo test2(String uid)  throws Exception{

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        SessionInfo sessionInfo = CacheUtil.getSessionInfo(uid);
        if(sessionInfo == null) {
            sessionInfo = new SessionInfo();
            sessionInfo.setTimestamp(now);
        } else {
            sessionInfo.setLasttimestamp(sessionInfo.getTimestamp());
            sessionInfo.setTimestamp(now);
        }
        CacheUtil.setSessionInfo(uid,sessionInfo);

        return sessionInfo;
    }

    @PostMapping(value = "/endBid/{lotsNo}")
    @RequireUserLogin
    @ResponseBody
    @ApiOperation(value="结束拍卖", httpMethod = "POST")
    @ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
            value = "登录成功后response Authorization header", required = true)
    public void endBid(@PathVariable("lotsNo") String lotsNo, HttpServletRequest request) {
        lotsServiceHelper.endBid(lotsNo);
    }
}