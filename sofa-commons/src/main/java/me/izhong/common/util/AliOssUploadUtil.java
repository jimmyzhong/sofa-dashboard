package me.izhong.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class AliOssUploadUtil {

    private final static String CHARSET_UTF8 = "utf8";
    private final static String ALGORITHM = "HmacSHA1";

    //OSS读取
    public static byte[] getOssObj(String accessKeyId,String secretAccessKey,String ossBucket,String endpoint,String key) throws Exception {
        if(!key.startsWith("/")) {
            throw new Exception("key must start with /");
        }
//        if(!endpoint.endsWith("/"))
//            endpoint += "/";
        String signResourcePath = "/"+ossBucket+key;
        String url = "https://"+ossBucket+"."+endpoint;

        String date = getGMTDate();
        String Signature = (hmacSha1(buildGetSignData(date,signResourcePath),secretAccessKey));
        String Authorization = "OSS " + accessKeyId + ":" + Signature;

        URL getUrl = new URL(url + key);
        HttpURLConnection connection;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            //添加 请求内容
            connection= (HttpURLConnection) getUrl.openConnection();
            //设置http连接属性
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            //设置请求头
            connection.setRequestProperty("Date", date);
            connection.setRequestProperty("Authorization", Authorization);

            connection.setReadTimeout(10000);//设置读取超时时间
            connection.setConnectTimeout(10000);//设置连接超时时间
            connection.connect();

            //读取响应
            if (connection.getResponseCode()==200) {
                // 从服务器获得一个输入流
                BufferedInputStream inputStream =new BufferedInputStream(connection.getInputStream());
                byte[] buf = new byte[512];
                int rc = 0;
                while ((rc = inputStream.read(buf)) > 0) {
                    baos.write(buf,0,rc);
                }
                inputStream.close();
            } else {
                //连接失败
                return null;
            }
            //断开连接
            connection.disconnect();
        } catch (Exception e) {
            log.error("",e);
            throw new Exception("上传图片失败");
        }

        return baos.toByteArray();
    }
    public static String putOssObj(String accessKeyId,String secretAccessKey,String ossBucket,String endpoint,String key,File file) throws Exception {
        String fileName = file.getName();
        String ext = fileName.substring(fileName.lastIndexOf("."));
        return putOssObj( accessKeyId, secretAccessKey, ossBucket, endpoint,key, FileUtils.readFileToByteArray(file),getContentType(ext));
    }

    public static String putOssObj(String accessKeyId,String secretAccessKey,String ossBucket,String endpoint,String key,byte[] content) throws Exception {
        return putOssObj( accessKeyId, secretAccessKey, ossBucket, endpoint,key, content,null);
    }
    //OSS上传
    public static String putOssObj(String accessKeyId,String secretAccessKey,String ossBucket,String endpoint,String key,byte[] content, String contentType) throws Exception {
        if(!key.startsWith("/")) {
            throw new Exception("key must start with /");
        }

        String date = getGMTDate();
        String signResourcePath = "/"+ossBucket+key;
        String connectUrl = "https://"+ossBucket+"."+endpoint;
        String ct = getContentType(contentType);
        String signData = buildPutSignData(ct,date,signResourcePath);
        String auth = hmacSha1(signData,secretAccessKey);
        //log.info("sign data:{}",signData);
        String Authorization = "OSS " + accessKeyId + ":" + auth;
        //log.info("Authorization:{}",Authorization);

        URL putUrl = new URL(connectUrl + key);
        HttpURLConnection connection = null;
        try {
            //添加 请求内容
            connection = (HttpURLConnection) putUrl.openConnection();
            //设置http连接属性
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            //设置请求头
            connection.setRequestProperty("Date", date);
            connection.setRequestProperty("Content-Type", ct);
            connection.setRequestProperty("Authorization", Authorization);


            connection.setReadTimeout(10000);//设置读取超时时间
            connection.setConnectTimeout(10000);//设置连接超时时间
            connection.connect();
            OutputStream out = connection.getOutputStream();
            out.write(content);
            out.flush();
            out.close();
            //读取响应
            if (connection.getResponseCode()==200) {
                log.info("上传{}成功",key);
            } else {
                log.info("上传失败 {} {}",connection.getResponseCode(),connection.getResponseMessage());
                //连接失败
                throw new Exception("链接失败");
            }
            //断开连接
            connection.disconnect();
        } catch (Exception e) {
            log.error("",e);
            throw new Exception("上传图片失败");
        } finally {
            if(connection != null)
                connection.disconnect();
        }
        return putUrl.toURI().toString();
    }

    private static String hmacSha1(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
            mac.init(keySpec);
            byte[] rawHmac;
            rawHmac = mac.doFinal(data.getBytes(CHARSET_UTF8));
            return new String(Base64.getEncoder().encode(rawHmac));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildGetSignData(String Date,String CanonicalizedResource){
        return  "GET" + "\n"+ "\n"+ "\n"
                + Date + "\n"
                + CanonicalizedResource;
    }

    private static String buildPutSignData(String contentType,String Date, String CanonicalizedResource){
        return  "PUT" + "\n"+ "\n"
                + contentType + "\n"
                + Date + "\n"
                + CanonicalizedResource;
    }

    private static String getGMTDate(){
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(cd.getTime());
    }

    private static Map<String,String> contentMapping = new HashMap<String,String>(){{
        put(".bmp","image/bmp");
        put(".gif","image/gif");
        put(".jpeg","image/jpg");
        put("image/jpeg","image/jpg");
        put(".jpg","image/jpg");
        put(".png","image/jpg");
        put("image/png","image/jpg");
        put(".html","text/html");
        put(".txt","image/plain");
        put(".vsd","application/vnd.visio");
        put(".ppt","application/vnd.ms-powerpoint");
        put(".pptx","application/vnd.ms-powerpoint");
        put(".doc","application/msword");
        put(".docx","application/msword");
        put(".xml","text/xml");
    }};

    public static String getContentType(String fileExt) {
        if(contentMapping.values().contains(fileExt))
            return fileExt;
        String dest = contentMapping.get(fileExt.toLowerCase());
        if(StringUtils.isBlank(dest)) {
            dest = "application/octet-stream";
        }
        return dest;
    }

    public static void main(String[] args) throws Exception{//api请求示例

        String ossBucket= "";
        String accessKeyId= "";
        String secretAccessKey= "";
        String endpoint= "";

        putOssObj(accessKeyId,secretAccessKey,ossBucket,endpoint,"/baby.jpeg", new File("/Users/jimmy/baby.jpeg"));

//        byte[] getResult = getOssObj(accessKeyId,secretAccessKey,ossBucket,endpoint,"/baby.jpeg");
//        System.out.println(getResult);
    }

}