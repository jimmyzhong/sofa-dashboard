package me.izhong.shop.controller;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.shop.annotation.RequireUserLogin;
import me.izhong.shop.consts.Constants;
import me.izhong.shop.dto.PageQueryParamDTO;
import me.izhong.shop.entity.City;
import me.izhong.shop.entity.County;
import me.izhong.shop.entity.Province;
import me.izhong.shop.entity.Town;
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

import java.util.List;

@Controller
@AjaxWrapper
@RequestMapping("/api/address")
public class UserReceiveAddressController {

	@Autowired
	private IReceiveAddressService receiveAddressService;

	@PostMapping("/add")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="添加收货地址", httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
	public void add(@RequestBody ReceiveAddressParam param, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		param.setUserId(userId);
		receiveAddressService.add(param);
	}

	@PostMapping("/delete/{id}")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="删除收货地址", httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
	public void delete(@PathVariable Long id, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		receiveAddressService.delete(userId, id);
	}

    @PostMapping("/update/{id}")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="更新收货地址", httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
	public void update(@PathVariable("id") Long addressId,
					   @RequestBody ReceiveAddressParam param, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
    	receiveAddressService.update(userId, addressId, param);
	}

	@PostMapping("/default/{id}")
	@ResponseBody
	@RequireUserLogin
	@ApiOperation(value="更新收货地址", httpMethod = "POST")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
	public void defaultAddress(@PathVariable("id") Long addressId, HttpServletRequest request) {
		Long userId = getCurrentUserId(request);
		receiveAddressService.setDefault(userId, addressId);
	}

    @GetMapping("/list")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="当前登录用户的收货地址", httpMethod = "GET")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
    public List<ReceiveAddressParam> list(HttpServletRequest request) {
    	Long userId = getCurrentUserId(request);
    	return receiveAddressService.list(userId);
    }

    @GetMapping(value = "/{id}")
    @ResponseBody
	@RequireUserLogin
	@ApiOperation(value="收货地址详情", httpMethod = "GET")
	@ApiImplicitParam(paramType = "header", dataType = "String", name = Constants.AUTHORIZATION,
			value = "登录成功后response Authorization header", required = true)
    public ReceiveAddressParam detail(@PathVariable Long id, HttpServletRequest request) {
    	Long userId = getCurrentUserId(request);
    	return receiveAddressService.detail(userId, id);
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

	@PostMapping("/list/province")
	@ResponseBody
	@ApiOperation(value="省,直辖市", httpMethod = "POST")
	public List<Province> listProvince(@RequestBody PageQueryParamDTO queryParamDTO) {
		return receiveAddressService.listProvince(queryParamDTO.getQuery());
	}

	@PostMapping("/list/city")
	@ResponseBody
	@ApiOperation(value="市", httpMethod = "POST")
	public List<City> listCity(@RequestBody PageQueryParamDTO queryParamDTO) {
		return receiveAddressService.listCity(queryParamDTO.getCode(), queryParamDTO.getQuery());
	}

	@PostMapping("/list/county")
	@ResponseBody
	@ApiOperation(value="区县", httpMethod = "POST")
	public List<County> listCounty(@RequestBody PageQueryParamDTO queryParamDTO) {
		return receiveAddressService.listCounty(queryParamDTO.getCode(), queryParamDTO.getQuery());
	}

	@PostMapping("/list/town")
	@ResponseBody
	@ApiOperation(value="镇", httpMethod = "POST")
	public List<Town> listTown(@RequestBody PageQueryParamDTO queryParamDTO) {
		return receiveAddressService.listTown(queryParamDTO.getCode(), queryParamDTO.getQuery());
	}
}
