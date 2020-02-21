package me.izhong.shop.controller;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    private RedisTemplate redisTemplate;

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

        Map map = new HashedMap();
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
        Map map = new HashedMap();
        map.put("local_ip", InetAddress.getLocalHost().toString());
        return map;
    }

}