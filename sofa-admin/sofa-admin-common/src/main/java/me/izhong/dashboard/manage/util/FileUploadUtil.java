package me.izhong.dashboard.manage.util;

import me.izhong.common.util.DateUtil;
import me.izhong.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import me.izhong.dashboard.manage.constants.Global;
import me.izhong.dashboard.manage.expection.file.FileNameLengthLimitExceededException;
import me.izhong.dashboard.manage.expection.file.FileSizeLimitExceededException;
import me.izhong.dashboard.manage.expection.file.InvalidExtensionException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 文件上传工具类
 */
@Slf4j
public class FileUploadUtil {
    /**
     * 默认大小 50M
     */
    public static final long DEFAULT_MAX_SIZE = 50 * 1024 * 1024;

    /**
     * 默认的文件名最大长度 100
     */
    public static final int DEFAULT_FILE_NAME_LENGTH = 100;

    /**
     * 默认上传的地址
     */
    private static String defaultBaseDir = Global.getDefaultUploadDir();

    private static int counter = 0;

    public static void setDefaultBaseDir(String defaultBaseDir) {
        FileUploadUtil.defaultBaseDir = defaultBaseDir;
    }

    public static String getDefaultBaseDir() {
        return defaultBaseDir;
    }

    /**
     * 以默认配置进行文件上传
     *
     * @param file 上传的文件
     * @return 文件名称
     * @throws Exception
     */
    public static final String upload(MultipartFile file) throws IOException {
        try {
            return upload(getDefaultBaseDir(), file, MimeTypeUtil.DEFAULT_ALLOWED_EXTENSION);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 根据文件路径上传
     *
     * @param baseDir 相对应用的基目录
     * @param file    上传的文件
     * @return 文件名称
     * @throws IOException
     */
    public static final String upload(String baseDir, MultipartFile file) throws IOException {
        try {
            return upload(baseDir, file, MimeTypeUtil.DEFAULT_ALLOWED_EXTENSION);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 文件上传
     *
     * @param baseDir          相对应用的基目录
     * @param file             上传的文件
     * @param allowedExtension 上传文件类型
     * @return 返回上传成功的文件名
     * @throws FileSizeLimitExceededException       如果超出最大大小
     * @throws FileNameLengthLimitExceededException 文件名太长
     * @throws IOException                          比如读写文件出错时
     * @throws InvalidExtensionException            文件校验异常
     */
    public static final String upload(String baseDir, MultipartFile file, String[] allowedExtension)
            throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
            InvalidExtensionException {
        log.info("上传文件，保存路径:{},文件名称:{},原始文件名称:{}",baseDir,file.getName(),file.getOriginalFilename());
        String lastChar = baseDir.substring(baseDir.length()-1);
        if(!StringUtils.equals(lastChar,"/")){
            baseDir += "/";
        }
        int fileNamelength = file.getOriginalFilename().length();
        if (fileNamelength > FileUploadUtil.DEFAULT_FILE_NAME_LENGTH) {
            throw new FileNameLengthLimitExceededException(FileUploadUtil.DEFAULT_FILE_NAME_LENGTH);
        }

        assertAllowed(file, allowedExtension);

        String fileName = extractFilename(file);

        File desc = getAbsoluteFile(baseDir, fileName);
        log.info("上传文件路径:{}",desc.getAbsolutePath());
        file.transferTo(desc);
        return fileName;
    }

    /**
     * 编码文件名
     */
    public static final String extractFilename(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String extension = getExtension(file);
        filename = DateUtil.datePath() + "/" + encodingFilename(filename) + "." + extension;
        return filename;
    }

    private static final File getAbsoluteFile(String uploadDir, String filename) throws IOException {
        File desc = new File(uploadDir + File.separator + filename);

        if (!desc.getParentFile().exists()) {
            log.info("创建文件路径:{}",desc.getParentFile().getAbsolutePath());
            boolean succ = desc.getParentFile().mkdirs();
            if(!succ)
                throw BusinessException.build("创建目录文件失败" + desc.getParent());
        }
        if (!desc.exists()) {
            desc.createNewFile();
        }
        return desc;
    }

    /**
     * 编码文件名
     */
    private static final String encodingFilename(String filename) {
        filename = filename.replace("_", " ");
        filename = MD5Util.hash(filename + System.nanoTime() + counter++);
        return filename;
    }

    /**
     * 文件大小校验
     *
     * @param file 上传的文件
     * @return
     * @throws FileSizeLimitExceededException 如果超出最大大小
     * @throws InvalidExtensionException
     */
    public static final void assertAllowed(MultipartFile file, String[] allowedExtension)
            throws FileSizeLimitExceededException, InvalidExtensionException {
        long size = file.getSize();
        if (DEFAULT_MAX_SIZE != -1 && size > DEFAULT_MAX_SIZE) {
            throw new FileSizeLimitExceededException(DEFAULT_MAX_SIZE / 1024 / 1024);
        }

        String filename = file.getOriginalFilename();
        String extension = getExtension(file);
        if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension)) {
            if (allowedExtension == MimeTypeUtil.IMAGE_EXTENSION) {
                throw new InvalidExtensionException.InvalidImageExtensionException(allowedExtension, extension,
                        filename);
            } else if (allowedExtension == MimeTypeUtil.FLASH_EXTENSION) {
                throw new InvalidExtensionException.InvalidFlashExtensionException(allowedExtension, extension,
                        filename);
            } else if (allowedExtension == MimeTypeUtil.MEDIA_EXTENSION) {
                throw new InvalidExtensionException.InvalidMediaExtensionException(allowedExtension, extension,
                        filename);
            } else {
                throw new InvalidExtensionException(allowedExtension, extension, filename);
            }
        }

    }

    /**
     * 判断MIME类型是否是允许的MIME类型
     *
     * @param extension
     * @param allowedExtension
     * @return
     */
    public static final boolean isAllowedExtension(String extension, String[] allowedExtension) {
        for (String str : allowedExtension) {
            if (str.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件名的后缀
     *
     * @param file 表单文件
     * @return 后缀名
     */
    public static final String getExtension(MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isEmpty(extension)) {
            extension = MimeTypeUtil.getExtension(file.getContentType());
        }
        return extension;
    }

}
