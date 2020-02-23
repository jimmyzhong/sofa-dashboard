package me.izhong.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义搜索注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Search {

    String columnName() default "";

    Op op() default Op.IS;

    enum Op {
        IS, REGEX, IN, GT, GTE, LT,LTE;
    }

}
