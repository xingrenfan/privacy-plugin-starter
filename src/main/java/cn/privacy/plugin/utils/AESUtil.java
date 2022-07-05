package cn.privacy.plugin.utils;

import cn.privacy.plugin.privacy.DefaultPrivacyDefinition;
import cn.privacy.plugin.exception.PluginCoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * 说明: AES工具类
 *
 * @author: Glendon.Li
 * @date: 2022/07/05
 * @version: <p>
 * 2.0.0 Glendon.Li 2022/07/05
 * 1.0.0 ZHZ 2021-11-16
 * </p>
 **/
public class AESUtil {
    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(DefaultPrivacyDefinition.class.getName());

    private final static String ALGORITHM = "AES";

    /**
     * 加密
     *
     * @param key     密钥
     * @param content 需要加密的字符串
     * @return 密文字节数组
     */
    private static byte[] encrypt(String key, String content) {
        byte[] rawKey = genKey(key.getBytes());
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(rawKey, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return cipher.doFinal(content.getBytes());
        } catch (Exception e) {
            log.error("{'key':'{}','content':'{}'} source data AES encrypt exception:", key, content, e);
            throw new PluginCoderException("AES encrypt exception");
        }
    }

    /**
     * 加密
     *
     * @param key     关键
     * @param content 内容
     * @return {@link String}
     */
    public static String encryptBase64(String key, String content) {
        byte[] encrypt = encrypt(key, content);
        return Base64.getEncoder().encodeToString(encrypt);
    }

    /**
     * 解密
     *
     * @param key     关键
     * @param content 内容
     * @return {@link String}
     */
    public static String decryptBase64(String key, String content) {
        byte[] decodeContent = Base64.getDecoder().decode(content);
        return decrypt(key, decodeContent);
    }


    /**
     * 解密
     *
     * @param encrypted 密文字节数组
     * @param key       密钥
     * @return 解密后的字符串
     */
    private static String decrypt(String key, byte[] encrypted) {
        byte[] rawKey = genKey(key.getBytes());
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(rawKey, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        } catch (Exception e) {
            log.debug("解密失败以为你返回原值");
            return "";
        }
    }

    /**
     * 密钥生成AES密钥
     *
     * @param seed 种子数据
     * @return 密钥数据
     */
    private static byte[] genKey(byte[] seed) {
        byte[] rawKey = null;
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(seed);
            // AES加密数据块分组长度必须为128比特，密钥长度可以是128比特、192比特、256比特中的任意一个
            kgen.init(128, secureRandom);
            SecretKey secretKey = kgen.generateKey();
            rawKey = secretKey.getEncoded();
        } catch (NoSuchAlgorithmException ignored) {
        }
        return rawKey;
    }


    public static void main(String[] args) {
        // 密钥的种子，可以是任何形式，本质是字节数组
        String key = UUID.randomUUID().toString();
        System.out.println(key);
        // 密码的明文
        String clearPwd = "123456";

        // 密码加密后的密文
        byte[] encryptedByteArr = encrypt(key, clearPwd);
        String encryptedPwd = Base64.getEncoder().encodeToString(encryptedByteArr);
        System.out.println(encryptedPwd);

        // 解密后的字符串
        byte[] decode = Base64.getDecoder().decode(encryptedPwd);
        String decryptedPwd = decrypt(key, decode);
        System.out.println(decryptedPwd);
    }

}

