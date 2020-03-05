package me.izhong.jobs.admin.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.exception.BusinessException;
import me.izhong.common.util.AliOssUploadUtil;
import me.izhong.dashboard.manage.constants.Global;
import me.izhong.dashboard.manage.expection.file.FileNameLengthLimitExceededException;
import me.izhong.dashboard.manage.expection.file.FileSizeLimitExceededException;
import me.izhong.dashboard.manage.util.FileUploadUtil;
import me.izhong.jobs.admin.service.ShopServiceReference;

@Slf4j
@Controller
@RequestMapping("/ext/shop/oss")
public class ShopFileUploadController {

	@Autowired(required = false)
	private ShopServiceReference shopServiceReference;

	@PostMapping(value = "/upload", consumes = "multipart/form-data")
	@AjaxWrapper
	public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
		log.info("upload content type:{}", file.getContentType());
		if (file.getContentType() == null || !file.getContentType().contains("image")) {
			throw BusinessException.build("无法识别图片");
		}
		checkFileName(file);
		checkFileSize(file);
		String fileName = UUID.randomUUID().toString().replace("-", "");
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		String key = "/" + fileName + extension;
		String pictureUrl = null;
		try {
			log.info("");
//			pictureUrl = AliOssUploadUtil.putOssObj(Global.getAliOssAccessKey(), Global.getAliOssAccessSecret(),
//					Global.getAliOssBucket(), Global.getAliOssEndpoint(), key, file.getBytes(), file.getContentType());
			Map<String, Object> map = new HashMap<>();
			map.put("fileBytes", file.getBytes());
			map.put("contentType", file.getContentType());
			pictureUrl = shopServiceReference.fileUploadService.uploadFile(map);
		} catch (Exception e) {
			log.error("上传图片失败", e);
		}
		Map<String, Object> result = new HashMap<>();
		result.put("pictureUrl", pictureUrl);
		return result;
	}

	private void checkFileName(MultipartFile file) {
		int fileNamelength = file.getOriginalFilename().length();
		if (fileNamelength > FileUploadUtil.DEFAULT_FILE_NAME_LENGTH) {
			throw new FileNameLengthLimitExceededException(FileUploadUtil.DEFAULT_FILE_NAME_LENGTH);
		}
	}

	private void checkFileSize(MultipartFile file) {
        long size = file.getSize();
        if (FileUploadUtil.DEFAULT_MAX_SIZE != -1 && size > FileUploadUtil.DEFAULT_MAX_SIZE) {
            throw new FileSizeLimitExceededException(FileUploadUtil.DEFAULT_MAX_SIZE / 1024 / 1024);
        }
	}
}
