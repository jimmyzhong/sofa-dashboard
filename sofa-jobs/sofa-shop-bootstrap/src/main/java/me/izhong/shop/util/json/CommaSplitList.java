package me.izhong.shop.util.json;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import lombok.extern.slf4j.Slf4j;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CommaSplitList implements ObjectDeserializer {
    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        String commaSplitStr = parser.parseObject(String.class);
        List<String> list = null;
        if (commaSplitStr != null) {
            list = new ArrayList<>(Arrays.asList(commaSplitStr.split(",")));
        }

        if (!(type instanceof ParameterizedTypeImpl)) {
            return (T) list;
        }


        Type[] types = ((ParameterizedTypeImpl) type).getActualTypeArguments();
        if (Number.class.isAssignableFrom((Class)types[0])) {
            Class clazz = (Class) types[0];
            try {
                Method method = clazz.getDeclaredMethod("valueOf", String.class);
                method.setAccessible(true);
                List res = new ArrayList();
                for (String e : list) {
                    if (e != null) {
                        res.add(method.invoke(null, e));
                    }
                }
                return (T) res;
            } catch (Exception e) {
                log.error("deserialize error", e);
            }
        }

        return (T) list;
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
