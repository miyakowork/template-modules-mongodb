package org.templateproject.mongodb.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.templateproject.mongodb.annotation.DynamicMongoSource;
import org.templateproject.mongodb.factory.MongoFactory;

import java.lang.reflect.Method;

/**
 * Created by wuwenbin on 2017/4/22.
 */
@Aspect
@Component
@Order(1)
public class MongoSourceAspect {

    @Autowired
    MongoFactory mongoFactory;


    /**
     * 定义切入点
     */
    @Pointcut("@annotation(org.templateproject.mongodb.annotation.DynamicMongoSource)")
    public void DynamicDataSourceAspect() {
    }

    /**
     * 切换
     *
     * @param joinPoint
     */
    @Before("DynamicDataSourceAspect()")
    public void switchMongoSource(JoinPoint joinPoint) throws NoSuchMethodException {
        Class clazz = joinPoint.getTarget().getClass();
        String methodName = joinPoint.getSignature().getName();
        Class[] argClass = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
        Method method = clazz.getMethod(methodName, argClass);
        if (method.isAnnotationPresent(DynamicMongoSource.class)) {
            String key = method.getAnnotation(DynamicMongoSource.class).key();
            String db = method.getAnnotation(DynamicMongoSource.class).db();
            if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(db))
                mongoFactory.setDynamicMongoDao(key, db);
            if (StringUtils.isEmpty(key) && !StringUtils.isEmpty(db))
                mongoFactory.setDynamicMongoDaoByDatabase(db);
            if (StringUtils.isEmpty(db) && !StringUtils.isEmpty(key))
                mongoFactory.setDynamicMongoDaoByKey(key);
        }
    }

    /**
     * 方法执行完毕后回滚回原来的数据源
     *
     * @param joinPoint
     */
    @After("DynamicDataSourceAspect()")
    public void rollbackMongoSource2Default(JoinPoint joinPoint) throws NoSuchMethodException {
        Class clazz = joinPoint.getTarget().getClass();
        String methodName = joinPoint.getSignature().getName();
        Class[] argClass = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
        Method method = clazz.getMethod(methodName, argClass);
        if (method.isAnnotationPresent(DynamicMongoSource.class)) {
            mongoFactory.dynamicMongoDao = mongoFactory.defaultMongoDao;

        }
    }
}
