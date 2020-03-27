package me.izhong.shop.util;

import com.alipay.sofa.rpc.common.utils.JSONUtils;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
import me.izhong.shop.config.AliCloudProperties;
import me.izhong.shop.response.ali.CertifyServiceResponse;
import me.izhong.shop.response.ali.SmsResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AliCloudUtils {
    public final static AliCloudUtils instance = new AliCloudUtils();

    private AliCloudUtils() {

    }

    public CertifyServiceResponse fetchCertifiedUserInfo(AliCloudProperties props, String personName, String idCard) {
        String host = props.getCertifyServiceHost();
        String path = props.getCertifyServicePath();
        String method = "POST";
        String appCode = props.getAppCode();
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appCode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        bodys.put("idcard", idCard);
        bodys.put("name", personName);

        try {
            log.info("certifying ===> " + idCard);
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            String responseBody = EntityUtils.toString(response.getEntity());
            log.info("certified info <=== " + responseBody);
            return JSONUtils.parseObject(responseBody, CertifyServiceResponse.class);
        } catch (Exception e) {
            log.error("request certify service error", e);
        }
        return null;
    }

    public SmsResponse sendSms(AliCloudProperties props, String phoneNumber, String params, boolean usePasswordResetTempate) {
        DefaultProfile profile = DefaultProfile.getProfile(props.getSmsRegionId(), props.getSmsAccessKey(), props.getSmsSecret());
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain(props.getSmsDomain());
        request.setVersion(props.getSmsVersion());
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", props.getSmsRegionId());
        request.putQueryParameter("PhoneNumbers", phoneNumber);
        request.putQueryParameter("SignName", props.getSmsSignName());
        request.putQueryParameter("TemplateCode", usePasswordResetTempate ? props.getSmsPassResetTemplate() :
                props.getSmsTemplate());
        if (!StringUtils.isEmpty(params)) {
            request.putQueryParameter("TemplateParam", params);
        }

        try {
            CommonResponse response = client.getCommonResponse(request);
            String responseData = response.getData();
            log.info("sms response " + responseData);
            return JSONUtils.parseObject(responseData, SmsResponse.class);
        } catch (ServerException e) {
            log.error("sms server error", e);
            throw new RuntimeException(e);
        } catch (ClientException e) {
            log.error("sms client error", e);
            throw new RuntimeException(e);
        }
    }

    public List<String> listBucket(AliCloudProperties props) {
        String endpoint = props.getOssEndpoint();
        String accessKeyId = props.getOssAccessKey();
        String accessKeySecret = props.getOssAccessSecret();

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            List<Bucket> buckets = ossClient.listBuckets();
            List<String> res = new ArrayList<>();
            for (Bucket bucket : buckets) {
                res.add(bucket.getName());
            }
            return res;
        } finally {
            ossClient.shutdown();
        }
    }

    public List<String> listObjsOfBucket(AliCloudProperties props, String bucketName, String keyPrefix) {
        String endpoint = props.getOssEndpoint();
        String accessKeyId = props.getOssAccessKey();
        String accessKeySecret = props.getOssAccessSecret();

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ObjectListing objectListing = ossClient.listObjects(bucketName, keyPrefix);
            List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
            List<String> res = new ArrayList<>();
            for (OSSObjectSummary s : sums) {
                res.add(s.getKey());
            }
            return res;
        } finally {
            ossClient.shutdown();
        }
    }

    public void uploadStream(AliCloudProperties props, String fileName, InputStream inputStream) {
        String endpoint = props.getOssEndpoint();
        String accessKeyId = props.getOssAccessKey();
        String accessKeySecret = props.getOssAccessSecret();
        String bucketName = props.getOssPicBucket();

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(bucketName, fileName, inputStream);

        } finally {
            ossClient.shutdown();
        }
    }
}
