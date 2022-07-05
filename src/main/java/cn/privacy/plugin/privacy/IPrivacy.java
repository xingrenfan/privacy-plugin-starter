package cn.privacy.plugin.privacy;

import cn.privacy.plugin.enums.Algorithm;

/**
 * 说明: 加解密接口定义
 *
 * @author: Glendon.Li
 * @date: 2022/07/05
 * @version: <p>
 * 2.0.0 Glendon.Li 2022/07/05
 * 1.0.0 ZHZ 2021-11-16
 * </p>
 **/
public interface IPrivacy {

    /**
     * 加密
     *
     * @param algorithm 加密算法
     * @param value     加密前的值
     * @param key       秘钥
     * @return 加密后的值
     * @throws Exception 异常
     */
    String encrypt(Algorithm algorithm, String value, String key) throws Exception;

    /**
     * 解密
     *
     * @param algorithm 解密算法
     * @param value     解密前的值
     * @param key       秘钥
     * @return 解密后的值
     * @throws Exception 异常
     */
    String decrypt(Algorithm algorithm, String value, String key) throws Exception;
}
