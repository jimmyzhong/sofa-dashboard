package me.izhong.dashboard.web.controller.admin;

import lombok.extern.slf4j.Slf4j;
import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.exception.BusinessException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("admin")
public class UploadAdminController {


    @Value("${dashboard.upload.filepath}")
    private String uploadFilePath;

    @RequiresAuthentication
    @PostMapping(value = "/upload")
    @AjaxWrapper
    public Map<String, String> upload(HttpServletRequest request,
                                      @RequestParam(value = "description", required = false) String description,
                                      @RequestParam("file") MultipartFile file) throws Exception {

        log.info("文件上传:{},{},{}", uploadFilePath, file.getName(), description);
        //如果文件不为空，写入上传路径
        if (!file.isEmpty()) {
            //上传文件路径
            //String path = request.getServletContext().getRealPath("/images/");
            String path = uploadFilePath;
            String originName = file.getOriginalFilename();
            if (StringUtils.isNotBlank(description))
                originName = description;
            //上传文件名
            String filename = RandomStringUtils.randomNumeric(10) + "@" + originName;

            File filepath = new File(path, filename);
            //判断路径是否存在，如果不存在就创建一个
            if (!filepath.getParentFile().exists()) {
                filepath.getParentFile().mkdirs();
            }
            String fileAbsFullName = path + File.separator + filename;
            log.info("文件保存:{}", fileAbsFullName);

            //将上传文件保存到一个目标文件当中
            file.transferTo(new File(fileAbsFullName));

            Map<String, String> map = new HashMap<>();
            map.put("file", "showImage/" + filename);
            return map;
        } else {
            throw BusinessException.build("文件上传失败");
        }

    }

    @RequiresAuthentication
    @RequestMapping(value = "/download")
    public ResponseEntity<byte[]> download(HttpServletRequest request,
                                           @RequestParam("filename") String filename,
                                           Model model) throws Exception {
        //下载文件路径
        //String path = request.getServletContext().getRealPath("/images/");
        String path = uploadFilePath;
        File file = new File(path + File.separator + filename);
        HttpHeaders headers = new HttpHeaders();
        //下载显示的文件名，解决中文名称乱码问题
        String downloadFielName = new String(filename.getBytes("UTF-8"), "iso-8859-1");
        //通知浏览器以attachment（下载方式）打开图片
        headers.setContentDispositionFormData("attachment", downloadFielName);
        //application/octet-stream ： 二进制流数据（最常见的文件下载）。
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file),
                headers, HttpStatus.CREATED);
    }


}
