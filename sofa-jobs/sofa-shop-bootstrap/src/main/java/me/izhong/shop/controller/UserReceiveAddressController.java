package me.izhong.shop.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import me.izhong.shop.cache.CacheUtil;
import me.izhong.shop.cache.SessionInfo;
import me.izhong.shop.dto.ReceiveAddressParam;
import me.izhong.shop.service.IReceiveAddressService;

@Controller
@RequestMapping("/api/address")
public class UserReceiveAddressController {

	@Autowired
	private IReceiveAddressService receiveAddressService;

	@PostMapping("/add")
    @ResponseBody
	public void add(@RequestBody ReceiveAddressParam param, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		param.setUserId(userId);
		receiveAddressService.add(param);
	}

	@PostMapping("/delete/{id}")
    @ResponseBody
	public void delete(@PathVariable Long id, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		receiveAddressService.delete(userId, id);
	}

    @PostMapping("/update/{id}")
    @ResponseBody
	public void update(@RequestBody ReceiveAddressParam param, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
    	receiveAddressService.update(userId, param);
	}

    @GetMapping("/list")
    @ResponseBody
    public void list(HttpServletRequest request) {
    	Long userId = getCurrentUserId(request);
    	receiveAddressService.list(userId);
    }

    @GetMapping(value = "/{id}")
    @ResponseBody
    public void detail(@PathVariable Long id, HttpServletRequest request) {
    	Long userId = getCurrentUserId(request);
    	receiveAddressService.detail(userId, id);
    }

    /**
     * 
     * @param request
     * @return
     */
	private Long getCurrentUserId(HttpServletRequest request) {
		SessionInfo session = CacheUtil.getSessionInfo(request);
		return session.getId();
	}
}
