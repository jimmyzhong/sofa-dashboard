package me.izhong.dashboard.web.controller;

import me.izhong.common.annotation.AjaxWrapper;
import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.constants.Global;
import me.izhong.dashboard.manage.util.FileUploadUtil;
import me.izhong.dashboard.manage.util.FileUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用请求处理
 */
@Controller
public class CommonController {
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    /**
     * 通用下载请求，导出的excel文件
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @RequiresAuthentication
    @GetMapping("profile/export")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request) {
        try {
            if (!FileUtil.isValidFilename(fileName)) {
                throw new Exception(String.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = Global.getExportPath() + fileName;

            response.setCharacterEncoding("utf-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition",
                    "attachment;fileName=" + FileUtil.setFileDownloadHeader(request, realFileName));
            FileUtil.writeBytes(filePath, response.getOutputStream());
            if (delete) {
                FileUtil.deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
            throw BusinessException.build("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求，编辑器用的
     */
    @RequiresAuthentication
    @PostMapping("/common/upload")
    @AjaxWrapper
    public Map uploadFile(MultipartFile file) throws Exception {
        // 上传并返回新文件名称
        String url = FileUploadUtil.upload(Global.getUploadPath(), file);
        //String url = serverConfig.getRequestUrl() + fileName;
        Map ajax = new HashMap();
        ajax.put("fileName", file.getOriginalFilename());
        ajax.put("url", url);
        return ajax;

    }

//    /**
//     * 本地资源通用下载
//     */
//    @GetMapping("/common/download/resource")
//    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
//            throws Exception
//    {
//        // 本地资源路径
//        String localPath = Global.getProfile();
//        // 数据库资源地址
//        String downloadPath = localPath + StringUtil.substringAfter(resource, SystemConstants.RESOURCE_PREFIX);
//        // 下载名称
//        String downloadName = StringUtil.substringAfterLast(downloadPath, "/");
//        response.setCharacterEncoding("utf-8");
//        response.setContentType("multipart/form-data");
//        response.setHeader("Content-Disposition",
//                "attachment;fileName=" + FileUtil.setFileDownloadHeader(request, downloadName));
//        FileUtil.writeBytes(downloadPath, response.getOutputStream());
//    }
}
