# privacy-spring-boot-starter

## 介绍
基于mybatis对隐私数据处理如加密、解密、脱敏等
springboot 2.0
mybatis 3.5

基于[privacy-spring-boot-starter](https://gitee.com/china-zhz/privacy-spring-boot-starter)修改并且添加功能

## 软件架构
软件架构说明




## 安装教程


### 使用说明

1.  添加pom依赖

```xml
<dependency>
    <groupId>cn.privacy.plugin</groupId>
    <artifactId>privacy-plugin-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```




2.  开启插件

```java
@EnablePrivacyPlugin
@SpringBootApplication
public class CryptoApplication {
    public static void main(String[] args) {
        SpringApplication.run(CryptoApplication.class, args);
    }
}
```



3. @FieldEncrypt注解放到要加密解密类的属性上就可以了如：

```
    @FieldEncrypt
    private String password;

```



4. @FieldEncrypt 注解默认使用AES算法，现在是集成了两种算法MD5和AES ，MD5是不可逆算法不可以解密，AES可以反向解密，默认的AES加密解密时我固定了一个秘钥，如果想自定义秘钥有两种方式：

① 全局配置密钥yml文件

```
privacy:
  crypto:
    key: jshfdiwhfkjncwolmas
```

② 注解上指定密钥

```
    @FieldEncrypt(key = "qwertyuiop")
    private String password;
```
 **自定义注解秘钥优先级高于全局秘钥** 



5. 如果你想使用MD5加密

```
    @FieldEncrypt(algorithm = Algorithm.MD5)
    private String password;
```


6. 如果这两个都不想用可以自定义加密解密器只需要实现ICrypto接口自定义加密解密方法即可

```
@Slf4j
public class MyCrypto implements ICrypto {

    @Override
    public String encrypt(Algorithm algorithm, String s, String s1) throws Exception {
        log.debug("---------------------------"+s+s1);
        return "zxcvbnm";
    }

    @Override
    public String decrypt(Algorithm algorithm, String s, String s1) throws Exception {
        log.debug("---------------------------"+s+s1);
        return "mnbvcxz";
    }
}
```
7. @FieldDesensitize加类属性上可实现字段脱敏

