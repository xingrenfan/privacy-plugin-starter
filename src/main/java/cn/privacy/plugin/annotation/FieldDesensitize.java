package cn.privacy.plugin.annotation;

import cn.privacy.plugin.desensitizer.DefaultDesensitizeExecutor;
import cn.privacy.plugin.desensitizer.IDesensitizeExecutor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 说明: 字段脱敏注解
 *
 * @author: Glendon.Li
 * @date: 2022/07/04
 * @version: <p>
 * 2.0.0 Glendon.Li 2022/07/05
 * 1.0.0 ZHZ 2021-11-16
 * </p>
 **/
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface FieldDesensitize {

    /**
     * 填充值 默认使用"*"填充
     *
     * @return
     */
    String fillValue() default "*";

    /**
     * 脱敏器
     *
     * @return
     */
    Class<? extends IDesensitizeExecutor> desensitizer() default DefaultDesensitizeExecutor.class;
}
