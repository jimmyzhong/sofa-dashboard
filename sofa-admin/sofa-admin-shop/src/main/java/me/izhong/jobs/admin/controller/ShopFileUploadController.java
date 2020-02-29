package me.izhong.jobs.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.exception.BusinessException;
import me.izhong.jobs.admin.service.ShopServiceReference;

@Slf4j
@Controller
@RequestMapping("/ext/shop/oss")
public class ShopFileUploadController {

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@PostMapping(value = "/upload", consumes = "multipart/form-data")
    @ResponseBody
	public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
		log.info("upload content type:{}", file.getContentType());
		if (file.getContentType() == null || !file.getContentType().contains("image")) {
			throw BusinessException.build("无法识别图片");
		}
		Map<String, Object> map = new HashMap<>();
		map.put("file", file);
		String pictureUrl = shopServiceReference.fileUploadService.uploadFile(map);
		Map<String, Object> result = new HashMap<>();
		result.put("pictureUrl", pictureUrl);
		return result;
	}
}
