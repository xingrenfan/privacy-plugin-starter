package cn.privacy.plugin.desensitizer;

import cn.privacy.plugin.privacy.DefaultPrivacyDefinition;
import cn.privacy.plugin.utils.DesensitizeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 说明: 默认脱敏实现
 *
 * @author: Glendon.Li
 * @date: 2022/07/05
 * @version: <p>
 * 2.0.0 Glendon.Li 2022/07/05
 * 1.0.0 ZHZ 2021-11-16
 * </p>
 **/
public class DefaultDesensitizeExecutor implements IDesensitizeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DefaultPrivacyDefinition.class.getName());

    /**
     * 执行脱敏处理
     *
     * @param value     要脱敏的值
     * @param fillValue 填充的副号
     * @return
     */
    @Override
    public String execute(String value, String fillValue) {
        if (value == null || value.length() == 0 || fillValue == null || fillValue.length() == 0) {
            return "";
        }
        String sensitiveInfo = DesensitizeUtil.encryptSensitiveInfo(value, fillValue);
        log.debug("脱敏原值：" + value);
        log.debug("脱敏后值：" + sensitiveInfo);
        return sensitiveInfo;
    }
}
