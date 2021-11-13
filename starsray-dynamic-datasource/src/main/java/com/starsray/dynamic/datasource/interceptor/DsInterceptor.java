//package com.starsray.dynamic.datasource.interceptor;
//
//import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
//import com.starsray.dynamic.datasource.annotation.DefaultDs;
//import com.starsray.dynamic.datasource.exception.ExceptionEnum;
//import com.starsray.dynamic.datasource.exception.GlobalException;
//import lombok.RequiredArgsConstructor;
//import org.apache.commons.lang3.StringUtils;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.Signature;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.HttpServletRequest;
//import java.lang.reflect.Method;
//
///**
// * <p>
// * 数据源选择器切面
// * </p>
// *
// * @author starsray
// * @since 2021-11-10
// */
//@Aspect
//@Component
//@RequiredArgsConstructor(onConstructor_ = @Autowired)
//public class DsInterceptor implements HandlerInterceptor {
//
//    @Pointcut("execution(public * com.starsray.dynamic.datasource.controller.*.*(..))")
//    public void datasourcePointcut() {
//    }
//
//    /**
//     * 前置操作，拦截具体请求，获取header里的数据源id，设置线程变量里，用于后续切换数据源
//     */
//    @Before("datasourcePointcut()")
//    public void doBefore(JoinPoint joinPoint) {
//        Signature signature = joinPoint.getSignature();
//        MethodSignature methodSignature = (MethodSignature) signature;
//        Method method = methodSignature.getMethod();
//
//        // 排除不可切换数据源的方法
//        DefaultDs annotation = method.getAnnotation(DefaultDs.class);
//        if (null != annotation) {
//            DynamicDataSourceContextHolder.push("master");
//        } else {
//            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//            ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
//            assert attributes != null;
//            HttpServletRequest request = attributes.getRequest();
//            String header = request.getHeader("tenantName");
//            if (StringUtils.isNotBlank(header)) {
//                DynamicDataSourceContextHolder.push(header);
//            } else {
//                throw new GlobalException(ExceptionEnum.NOT_TENANT);
//            }
//        }
//    }
//
//    /**
//     * 后置操作，设置回默认的数据源id
//     */
//    @AfterReturning("datasourcePointcut()")
//    public void doAfter() {
//        DynamicDataSourceContextHolder.push("master");
//    }
//
//}
