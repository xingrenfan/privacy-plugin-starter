package cn.privacy.plugin.interceptor;


import cn.privacy.plugin.annotation.FieldEncrypt;
import cn.privacy.plugin.privacy.IPrivacy;
import cn.privacy.plugin.enums.Algorithm;
import cn.privacy.plugin.enums.CryptoType;
import cn.privacy.plugin.properties.PrivacyProperties;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 说明: Mybatis加解密拦截器
 *
 * @author: Glendon.Li
 * @date: 2022/07/05
 * @version: <p>
 * 2.0.0 Glendon.Li 2022/07/05
 * 1.0.0 ZHZ 2021-11-16
 * </p>
 **/
@Component
@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        }
)
public class PrivacyInterceptor implements Interceptor {
    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(PrivacyInterceptor.class.getName());

    @Resource
    private PrivacyProperties privacyProperties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.getMethod();

        switch (method.getName()) {
            case "update":
                return updateHandle(invocation);
            case "query":
                return selectHandle(invocation);
            default:
                return invocation.proceed();
        }

    }

    /**
     * 查询操作处理
     *
     * @param invocation 调用
     * @return {@link Object}
     * @throws Throwable
     */
    private Object selectHandle(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler<Object> resultHandler = (ResultHandler) args[3];
        Executor executor = (Executor) invocation.getTarget();

        CacheKey cacheKey;
        BoundSql boundSql;

        //处理参数作为条件查询需要加密
        handleParameterOrResult(parameter, CryptoType.ENCRYPT);

        //由于逻辑关系，只会进入一次
        if (args.length == 4) {
            //4 个参数时
            boundSql = ms.getBoundSql(parameter);
            cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
        } else {
            //6 个参数时
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }

        List<Object> resultList;
        resultList = executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);

        for (Object o : resultList) {
            handleParameterOrResult(o, CryptoType.DECRYPT);
        }

        return resultList;
    }

    /**
     * 新增修改操作处理
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    private Object updateHandle(Invocation invocation) throws Throwable {
        //处理参数
        handleParameterOrResult(invocation.getArgs()[1], CryptoType.ENCRYPT);
        return invocation.proceed();
    }

    /**
     * 处理参数或结果
     *
     * @param object
     * @param cryptoType
     * @throws IllegalAccessException
     */
    private void handleParameterOrResult(Object object, CryptoType cryptoType) throws IllegalAccessException {
        HashMap<Field, Object> fieldObjectHashMap = new HashMap<>();
        //多个参数
        if (object instanceof Map) {
            Map paramMap = (Map) object;
            Set keySet = paramMap.keySet();
            for (Object key : keySet) {
                Object o = paramMap.get(key);
                if (o != null) {
                    handleObject(o, o.getClass(), fieldObjectHashMap);
                }

            }
        } else {
            if (object != null) {
                handleObject(object, object.getClass(), fieldObjectHashMap);
            }
        }

        //统一修改加密解密值
        fieldObjectHashMap.keySet().forEach(key -> {
            try {
                handleString(key, fieldObjectHashMap.get(key), cryptoType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 是否是基本类型
     *
     * @param type
     * @return
     */
    private boolean isBase(Type type) {

        return boolean.class.equals(type) ||
                char.class.equals(type) ||
                long.class.equals(type) ||
                int.class.equals(type) ||
                byte.class.equals(type) ||
                short.class.equals(type) ||
                double.class.equals(type) ||
                float.class.equals(type);
    }

    /**
     * 是否是
     *
     * @param object
     * @return
     */
    private boolean isFilter(Object object) {

        return object == null || object instanceof CharSequence || object instanceof Number || object instanceof Collection || object instanceof Date || object instanceof ChronoLocalDate;
    }

    /**
     * 聚合父类属性
     *
     * @param oClass
     * @param fields
     * @return
     */
    private List<Field> mergeField(Class<?> oClass, List<Field> fields) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        Class<?> superclass = oClass.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class) && superclass.getDeclaredFields().length > 0) {
            mergeField(superclass, fields);
        }
        for (Field declaredField : oClass.getDeclaredFields()) {

            int modifiers = declaredField.getModifiers();

            if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isVolatile(modifiers) || Modifier.isSynchronized(modifiers)) {
                continue;
            }
            fields.add(declaredField);
        }

        return fields;

    }

    /**
     * 处理Object
     *
     * @param obj
     * @param oClass
     * @throws IllegalAccessException
     */
    private void handleObject(Object obj, Class<?> oClass, HashMap<Field, Object> fieldObjectHashMap) throws IllegalAccessException {
        //过滤
        if (isFilter(obj)) {
            return;
        }

        List<Field> fields = mergeField(oClass, null);

        for (Field declaredField : fields) {

            //静态属性直接跳过
            if (Modifier.isStatic(declaredField.getModifiers())) {
                continue;
            }

            boolean accessible = declaredField.isAccessible();
            declaredField.setAccessible(true);
            Object value = declaredField.get(obj);
            declaredField.setAccessible(accessible);

            if (value == null) {
                continue;
            } else if (value instanceof Number) {
                continue;
            } else if (value instanceof String) {

                FieldEncrypt annotation = declaredField.getAnnotation(FieldEncrypt.class);
                if (annotation != null) {
                    fieldObjectHashMap.put(declaredField, obj);
                }

            } else if (value instanceof Collection) {
                Collection coll = (Collection) value;
                for (Object o : coll) {
                    if (isFilter(o)) {
                        //默认集合内类型一致
                        break;
                    }
                    handleObject(o, o.getClass(), fieldObjectHashMap);
                }
            } else {
                handleObject(value, value.getClass(), fieldObjectHashMap);
            }
        }

    }

    /**
     * 处理字符
     *
     * @param field
     * @param object
     * @param cryptoType
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private void handleString(Field field, Object object, CryptoType cryptoType) throws Exception {

        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        Object value = field.get(object);

        FieldEncrypt annotation = field.getAnnotation(FieldEncrypt.class);
        if (annotation != null) {

            String key;
            //全局配置的key
            String propertiesKey = privacyProperties.getKey();
            log.debug("全局key是：" + propertiesKey);
            //属性上的key
            String annotationKey = annotation.key();
            log.debug("注解key是：" + annotationKey);

            if (!"".equals(annotationKey)) {
                key = annotationKey;
            } else {
                key = propertiesKey;
            }

            Algorithm algorithm = annotation.algorithm();
            Class<? extends IPrivacy> iCryptoImpl = annotation.iCrypto();
            IPrivacy iPrivacy = iCryptoImpl.newInstance();

            String valueResult;
//            Method decrypt = iCryptoImpl.getDeclaredMethod(cryptoType.getMethod(), Algorithm.class, String.class, String.class);
            //解密后的值
//            Object valueDecrypt = decrypt.invoke(iCrypto, algorithm, String.valueOf(value), key);
            if (cryptoType.equals(CryptoType.DECRYPT)) {
                valueResult = iPrivacy.decrypt(algorithm, String.valueOf(value), key);
            } else {
                valueResult = iPrivacy.encrypt(algorithm, String.valueOf(value), key);
            }

            log.debug("原值：" + value);
            log.debug("现在：" + valueResult);
            field.set(object, String.valueOf(valueResult));
            field.setAccessible(accessible);
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
