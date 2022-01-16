package com.hss.handler;

import com.hss.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Handler 记录 Controller 中的 RequestMapping 和 Method 的对应关系
 */
public class Handler {

    /** 保存方法对应的实例 */
    public Object controller;
    /** 保存映射的方法 */
    public Method method;

    public Pattern pattern;
    /** 参数顺序 */
    public Map<String,Integer> paramIndexMapping;

    public Class<?>[] parameterTypes;

    public Handler(Pattern pattern, Object controller, Method method){
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
        paramIndexMapping = new HashMap<>();
        parameterTypes = method.getParameterTypes();
        putParamIndexMapping(method);
    }

    private void putParamIndexMapping(Method method){
        //提取方法中加了注解的参数
        Annotation[][] pa = method.getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation a : pa[i]) {
                if(a instanceof RequestParam){
                    String paramName = ((RequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName,i);
                    }
                }
            }
        }
        //提取方法中的 request 和 response 参数
        Class<?> [] paramsTypes = method.getParameterTypes();
        for (int i = 0; i < paramsTypes.length ; i ++) {
            Class<?> type = paramsTypes[i];
            if(type == HttpServletRequest.class || type == HttpServletResponse.class){
                paramIndexMapping.put(type.getName(),i);
            }
        }
    }

}
