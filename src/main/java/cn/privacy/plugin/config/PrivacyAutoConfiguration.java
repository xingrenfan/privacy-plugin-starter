package cn.privacy.plugin.config;

import cn.privacy.plugin.interceptor.PrivacyInterceptor;
import cn.privacy.plugin.interceptor.DesensitizeInterceptor;
import cn.privacy.plugin.properties.PrivacyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 说明: 插件开启时自动配置类
 *
 * @author: Glendon.Li
 * @date: 2022/07/05
 * @version: <p>
 * 2.0.0 Glendon.Li 2022/07/05
 * 1.0.0 ZHZ 2021-11-16
 * </p>
 **/
@Configuration
@EnableConfigurationProperties(PrivacyProperties.class)
public class PrivacyAutoConfiguration {

    @Bean
    public PrivacyInterceptor cryptoInterceptor() {
        return new PrivacyInterceptor();
    }

    @Bean
    public DesensitizeInterceptor desensitizeInterceptor() {
        return new DesensitizeInterceptor();
    }


}
