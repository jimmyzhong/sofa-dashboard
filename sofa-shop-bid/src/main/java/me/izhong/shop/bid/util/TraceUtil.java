package me.izhong.shop.bid.util;

import com.alibaba.fastjson.JSONObject;

import me.izhong.shop.bid.frame.BidContext;
import me.izhong.shop.bid.pojo.BaseRequest;
import me.izhong.shop.bid.pojo.BaseResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public class TraceUtil {
    public final static String TRACE_ID = "_traceId";

    public static void initTrace() {
        String traceId = generateTraceId();
        setTraceId(traceId);
    }

    public static void initTraceFrom(HttpServletRequest request) {

        String traceId;

        traceId = request.getParameter(TRACE_ID);
        if (StringUtils.isNotBlank(traceId)) {
            setTraceId(traceId);
            return;
        }

        traceId = (String) request.getAttribute(TRACE_ID);
        if (StringUtils.isNotBlank(traceId)) {
            setTraceId(traceId);
            return;
        }

        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables != null) {
            traceId = (String) pathVariables.get(TRACE_ID);
        }
        if (StringUtils.isNotBlank(traceId)) {
            setTraceId(traceId);
            return;
        }

        traceId = generateTraceId();
        setTraceId(traceId);
    }


    public static void initTraceFrom(BidContext context) {
        String traceId = null;
        if (context != null) {
            traceId = context.getTraceId();
        }
        if (traceId == null) {
            traceId = generateTraceId();
        }
        setTraceId(traceId);
    }

    public static void initTraceFrom(JSONObject json) {
        String traceId = json.getString(TRACE_ID);
        if (traceId == null) {
            traceId = generateTraceId();
        }
        setTraceId(traceId);
    }

    @SuppressWarnings("rawtypes")
    public static void initTraceFrom(Map map) {
        String traceId = (String) map.get(TRACE_ID);
        if (traceId == null) {
            traceId = generateTraceId();
        }
        setTraceId(traceId);
    }

    public static void initTraceFrom(Method method) {
        String traceId = null;
        try {
            traceId = (String) method.invoke(TRACE_ID);
        } catch (Exception e) {
        }
        if (traceId == null) {
            traceId = generateTraceId();
        }
        setTraceId(traceId);
    }


    public static void putTraceInto(HttpServletRequest request) {
        String traceId = getTraceId();
        if (StringUtils.isNotBlank(traceId)) {
            request.setAttribute(TRACE_ID, traceId);
        }
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void putTraceInto(Map map) {
        String traceId = getTraceId();
        if (traceId != null) {
            map.put(TRACE_ID, traceId);
        }
    }

    public static void putTraceInto(Method method) {
        String traceId = getTraceId();
        if (traceId != null) {
            try {
                method.invoke(TRACE_ID, traceId);
            } catch (Exception e) {
            }
        }
    }

    public static void clearTrace() {
        MDC.remove(TRACE_ID);
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    public static void setTraceId(String traceId) {
        if (StringUtils.isNotBlank(traceId)) {
            traceId = StringUtils.left(traceId, 36);
        }
        MDC.put(TRACE_ID, traceId);
    }

    static private String generateTraceId() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0,16);
        return uuid;
    }

}
