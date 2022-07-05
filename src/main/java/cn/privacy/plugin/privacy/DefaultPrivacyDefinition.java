package cn.privacy.plugin.privacy;


import cn.privacy.plugin.enums.Algorithm;
import cn.privacy.plugin.utils.AESUtil;
import cn.privacy.plugin.utils.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 说明: 默认加解密实现
 *
 * @author: Glendon.Li
 * @date: 2022/07/05
 * @version: <p>
 * 2.0.0 Glendon.Li 2022/07/05
 * 1.0.0 ZHZ 2021-11-16
 * </p>
 **/
public class DefaultPrivacyDefinition implements IPrivacy {
    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(DefaultPrivacyDefinition.class.getName());

    /**
     * 默认加密Key
     */
    private final static String KEY = "edcb87b4-68b1-466b-8f6d-256ef53e50f0";

    /**
     * 加密
     *
     * @param algorithm 加密算法
     * @param value     加密前的值
     * @param key       秘钥
     * @return 加密后的值
     */
    @Override
    public String encrypt(Algorithm algorithm, String value, String key) throws Exception {
        String result;

        if (key == null || key.length() == 0) {
            key = KEY;
        }

        // 暂时只支持MD5和AES
        switch (algorithm) {
            case MD5:
                return CryptoUtil.encryptBASE64(CryptoUtil.encryptMD5(value.getBytes()));
            default:
                return AESUtil.encryptBase64(key, value);
        }
    }

    /**
     * 解密
     *
     * @param algorithm 解密算法
     * @param value     解密前的值
     * @param key       秘钥
     * @return 解密后的值
     */
    @Override
    public String decrypt(Algorithm algorithm, String value, String key) {
        if (key == null || key.length() == 0) {
            key = KEY;
        }
        try {
            switch (algorithm) {
                case MD5:
                    log.debug("MD5不支持解密,返回原数据.");
                    return value;
                default:
                    return AESUtil.decryptBase64(key, value);
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            log.warn("值：{}不支持解密", value);
            return value;
        }
    }

}
