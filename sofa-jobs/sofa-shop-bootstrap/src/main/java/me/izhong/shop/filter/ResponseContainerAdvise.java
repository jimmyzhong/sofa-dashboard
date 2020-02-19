package me.izhong.shop.filter;

import me.izhong.common.annotation.AjaxWrapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

//@ControllerAdvice
//@Order(1)
public class ResponseContainerAdvise implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        boolean classAnnotation = returnType.getDeclaringClass().isAnnotationPresent(AjaxWrapper.class);
        boolean methodAnnptation = returnType.getMethod().getDeclaredAnnotation(AjaxWrapper.class) != null;
        return classAnnotation || methodAnnptation;
    }

//    @Override
//    public String beforeBodyWrite(String body, MethodParameter returnType,
//                                  MediaType selectedContentType,
//                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
//                                  ServerHttpRequest request, ServerHttpResponse response) {
//        ResponseContainer res = ResponseContainer.successContainer(body);
//
//        return JSON.toJSONString(res);
//    }

    @Override
    public ResponseContainer beforeBodyWrite(Object body, MethodParameter returnType,
                                             MediaType selectedContentType,
                                             Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                             ServerHttpRequest request, ServerHttpResponse response) {
        return ResponseContainer.successContainer(body);
    }
}
