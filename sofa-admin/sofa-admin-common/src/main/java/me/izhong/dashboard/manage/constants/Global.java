package me.izhong.dashboard.manage.constants;

import me.izhong.common.exception.BusinessException;
import me.izhong.dashboard.manage.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

public class Global {

    private static final Logger log = LoggerFactory.getLogger(Global.class);

    /**
     * 当前对象实例
     */
    private static Global global;

    /**
     * 保存全局属性值
     */
    private static Map<String, String> map = new HashMap<String, String>();

    private Global() {
    }

    /**
     * 静态工厂方法
     */
    public static synchronized Global getInstance() {
        if (global == null) {
            global = new Global();
        }
        return global;
    }

    /**
     * 获取配置
     */
    public static String getConfig(String key) {
        String value = map.get(key);
        if (value == null) {
            //Map<?, ?> yamlMap = null;
            try {
                //yamlMap = YamlUtil.loadYaml(NAME);
                //value = String.valueOf(YamlUtil.getProperty(yamlMap, key));

                Environment env = SpringUtil.getBean(Environment.class);
                value = env.getProperty(key);

                map.put(key, value != null ? value : StringUtils.EMPTY);
            } catch (Exception e) {
                log.error("获取全局配置异常 {}", key);
            }
        }
        return value;
    }

    /**
     * 获取项目名称
     */
    public static String getName() {
        return StringUtils.defaultIfBlank(getConfig("dashboard.name"), "RuoYi");
    }

    /**
     * 获取项目版本
     */
    public static String getVersion() {
        return StringUtils.defaultString(getConfig("dashboard.version"), "4.0.0");
    }

    public static String getSalt() {
        String salt = getConfig("dashboard.salt");
        if (StringUtils.isBlank(salt))
            throw BusinessException.build("dashboard.salt没配置");
        return salt;
    }

    /**
     * 获取版权年份
     */
    public static String getCopyrightYear() {
        return StringUtils.defaultIfBlank(getConfig("dashboard.copyrightYear"), "2019");
    }

    /**
     * 实例演示开关
     */
    public static String isDemoEnabled() {
        return StringUtils.defaultIfBlank(getConfig("dashboard.demoEnabled"), "true");
    }


    /**
     * 获取文件上传路径
     */
    public static String getProfile() {
        return getConfig("dashboard.filepath");
    }



    public static String getAvatarMapping() {
        return "/profile/avatar/";
    }

    public static String getUploadMapping() {
        return "/profile/upload/";
    }

    public static String getExportMapping() {
        return "/profile/export/";
    }

    /**
     * 获取头像上传路径
     */
    public static String getAvatarPath() {
        String avatar = getConfig("dashboard.avatar.filepath");
        if(StringUtils.isNotBlank(avatar)) {
            if(!avatar.startsWith("/"))
                throw BusinessException.build("dashboard.avatar.filepath需要配置以/开始");
            if(!avatar.endsWith("/")) {
                avatar += "/";
            }
            return avatar;
        } else {
            String dashPath = getConfig("dashboard.filepath");
            if(StringUtils.isBlank(dashPath)) {
                throw BusinessException.build("filepath没有配置");
            }
            if(!dashPath.startsWith("/"))
                throw BusinessException.build("dashboard.filepath需要配置以/开始");
            if(!dashPath.endsWith("/")) {
                dashPath += "/";
            }
            return dashPath + "avatar/";
        }
    }

    /**
     * 获取下载路径
     */
    public static String getExportPath() {
        String export = getConfig("dashboard.export.filepath");
        if(StringUtils.isNotBlank(export)) {
            if(!export.startsWith("/"))
                throw BusinessException.build("dashboard.export.filepath需要配置以/开始");
            if(!export.endsWith("/")) {
                export += "/";
            }
            return export;
        } else {
            String dashPath = getConfig("dashboard.filepath");
            if(StringUtils.isBlank(dashPath)) {
                throw BusinessException.build("filepath没有配置");
            }
            if(!dashPath.startsWith("/"))
                throw BusinessException.build("dashboard.filepath需要配置以/开始");
            if(!dashPath.endsWith("/")) {
                dashPath += "/";
            }
            return dashPath + "export/";
        }
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath() {
        String upload = getConfig("dashboard.upload.filepath");
        if(StringUtils.isNotBlank(upload)) {
            if(!upload.startsWith("/"))
                throw BusinessException.build("dashboard.upload.filepath需要配置以/开始");
            if(!upload.endsWith("/")) {
                upload += "/";
            }
            return upload;
        } else {
            String dashPath = getConfig("dashboard.filepath");
            if(StringUtils.isBlank(dashPath)) {
                throw BusinessException.build("filepath没有配置");
            }
            if(!dashPath.startsWith("/"))
                throw BusinessException.build("dashboard.filepath需要配置以/开始");
            if(!dashPath.endsWith("/")) {
                dashPath += "/";
            }
            return dashPath + "upload/";
        }
    }

    public static boolean isUploadOssEnable() {
        return StringUtils.equals(getConfig("dashboard.ali.oss.enable"),"true");
    }

    public static String getAliOssBucket() {
        return getConfig("dashboard.ali.oss.bucket");
    }

    public static String getAliOssEndpoint() {
        return getConfig("dashboard.ali.oss.endpoint");
    }

    public static String getAliOssAccessKey() {
        return getConfig("dashboard.ali.oss.accessKey");
    }

    public static String getAliOssAccessSecret() {
        return getConfig("dashboard.ali.oss.accessSecret");
    }

    /**
     * 获取ip地址开关
     */
    public static Boolean isAddressEnabled() {
        return Boolean.valueOf(getConfig("dashboard.ip.addressEnabled"));
    }


    public static Boolean isUseLocalIpDatabase() {
        return Boolean.valueOf(getConfig("dashboard.ip.userLocalDatabase"));
    }

    public static String getIpFilePath() {
        return getConfig("dashboard.ip.filepath");
    }
}
