package cn.privacy.plugin.enums;

/**
 * 说明: 加解密枚举
 *
 * @author: Glendon.Li
 * @date: 2022/07/05
 * @version: <p>
 * 2.0.0 Glendon.Li 2022/07/05
 * 1.0.0 ZHZ 2021-11-16
 * </p>
 **/
public enum CryptoType {
    /**
     * ENCRYPT 加密
     * DECRYPT 解密
     */
    ENCRYPT("encrypt"), DECRYPT("decrypt");

    /**
     * 对应加密器方法名称
     */
    private String method;

    CryptoType(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
