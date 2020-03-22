package me.izhong.common.util.json;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeSerializer implements ObjectSerializer {

    public static final LocalDateTimeSerializer instance = new LocalDateTimeSerializer();
    private String defaultPattern = "yyyy-MM-dd HH:mm:ss";

    public LocalDateTimeSerializer() {
    }

    public LocalDateTimeSerializer(String dateFormat) {
        defaultPattern = dateFormat;
    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
        } else {
            LocalDateTime result = (LocalDateTime) object;
            out.writeString(result.format(DateTimeFormatter.ofPattern(defaultPattern)));
        }
    }

}