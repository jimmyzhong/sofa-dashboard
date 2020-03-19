package me.izhong.jobs.admin.controller;

import java.util.HashMap;
import java.util.Map;

import me.izhong.dashboard.common.constants.Global;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.common.util.FileUploadUtil;
import me.izhong.dashboard.common.util.MimeTypeUtil;

@Slf4j
@Controller
@RequestMapping("/ext/shop/oss")
public class ShopFileUploadController {

	@PostMapping(value = "/upload", consumes = "multipart/form-data")
	@AjaxWrapper
	public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
		log.info("upload content type:{}", file.getContentType());
		if (file.getContentType() == null || !file.getContentType().contains("image")) {
			throw BusinessException.build("无法识别图片");
		}
		String pictureUrl = null;
		try {
			pictureUrl = FileUploadUtil.upload(Global.getUploadPath(), file, MimeTypeUtil.IMAGE_EXTENSION);
		} catch (Exception e) {
			log.error("上传图片失败", e);
		}
		Map<String, Object> result = new HashMap<>();
		result.put("pictureUrl", pictureUrl);
		return result;
	}

}
