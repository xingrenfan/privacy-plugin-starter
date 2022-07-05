package cn.privacy.plugin.annotation;


import cn.privacy.plugin.privacy.DefaultPrivacyDefinition;
import cn.privacy.plugin.privacy.IPrivacy;
import cn.privacy.plugin.enums.Algorithm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 说明: 字段加解密注解
 *
 * @author: Glendon.Li
 * @date: 2022/07/05
 * @version: <p>
 * 2.0.0 Glendon.Li 2022/07/05
 * 1.0.0 ZHZ 2021-11-16
 * </p>
 **/
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface FieldEncrypt {

    /**
     * 秘钥
     *
     * @return
     */
    String key() default "";

    /**
     * 加密解密算法
     *
     * @return
     */
    Algorithm algorithm() default Algorithm.AES;

    /**
     * 加密解密器
     *
     * @return
     */
    Class<? extends IPrivacy> iCrypto() default DefaultPrivacyDefinition.class;

}
