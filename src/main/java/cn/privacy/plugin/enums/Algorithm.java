package cn.privacy.plugin.enums;

/**
 * 说明: 加密方式
 *
 * @author: Glendon.Li
 * @date: 2022/07/05
 * @version: <p>
 * 2.0.0 Glendon.Li 2022/07/05
 * 1.0.0 ZHZ 2021-11-16
 * </p>
 **/
public enum Algorithm {

    /**
     * 不可逆加密 MD5
     * 对称加密  AES （推荐；速度快、可解密）
     */
    MD5, AES;


    Algorithm() {
    }
}
