package cn.privacy.plugin.desensitizer;

/**
 * 说明: 脱敏执行器定义
 *
 * @author: Glendon.Li
 * @date: 2022/07/05
 * @version: <p>
 * 2.0.0 Glendon.Li 2022/07/05
 * 1.0.0 ZHZ 2021-11-16
 * </p>
 **/
public interface IDesensitizeExecutor {

    /**
     * 执行脱敏处理
     *
     * @param value     要脱敏的值
     * @param fillValue 填充的副号
     * @return {@link String}
     */
    String execute(String value, String fillValue);
}
