package cn.privacy.plugin.interceptor;


import cn.privacy.plugin.annotation.FieldDesensitize;
import cn.privacy.plugin.desensitizer.IDesensitizeExecutor;
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * 说明: Mybatis脱敏拦截器
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
        }
)
public class DesensitizeInterceptor implements Interceptor {
    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(DesensitizeInterceptor.class.getName());

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return selectHandle(invocation);
    }

    /**
     * 查询操作处理
     *
     * @param invocation
     * @return
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

        for (Object object : resultList) {
            HashMap<Field, Object> fieldObjectHashMap = new HashMap<>();

            if (object != null) {
                handleObject(object, object.getClass(), fieldObjectHashMap);
            }
            //统一修改加密解密值
            fieldObjectHashMap.keySet().forEach(key -> {
                try {
                    handleString(key, fieldObjectHashMap.get(key));
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            });
        }

        return resultList;
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

                FieldDesensitize annotation = declaredField.getAnnotation(FieldDesensitize.class);
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
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void handleString(Field field, Object object) throws IllegalAccessException, InstantiationException {

        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        Object value = field.get(object);

        FieldDesensitize annotation = field.getAnnotation(FieldDesensitize.class);
        if (annotation != null) {

            String fillValue = annotation.fillValue();

            Class<? extends IDesensitizeExecutor> desensitizer = annotation.desensitizer();
            IDesensitizeExecutor iDesensitizeExecutor = desensitizer.newInstance();
            String desensitizerValue = iDesensitizeExecutor.execute(String.valueOf(value), fillValue);

            log.debug("原值：" + value);
            log.debug("脱敏后：" + desensitizerValue);
            field.set(object, String.valueOf(desensitizerValue));
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
