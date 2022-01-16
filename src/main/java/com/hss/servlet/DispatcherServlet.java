package com.hss.servlet;

import com.hss.annotation.Controller;
import com.hss.annotation.RequestMapping;
import com.hss.annotation.RequestParam;
import com.hss.factory.AbstractFactory;
import com.hss.factory.FactoryBean;
import com.hss.util.GridProperties;
import com.hss.util.StrUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class DispatcherServlet extends HttpServlet {

    private Map<String,Method> handlerMapping = new HashMap<String,Method>();

    private AbstractFactory abstractFactory = null;

    public void init() throws ServletException {
        //1.读取单例bean放入工厂
        abstractFactory = new FactoryBean();

        try {
            loadHandlerMapping();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url =  req.getRequestURI();
        if(handlerMapping.containsKey(url)){
            System.out.println("执行方法controller");
            Method method = handlerMapping.get(url);
            if(method==null){
                try {
                    throw  new Exception("404，没有找到对Action");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            try {
                // 获取用户的入参
                Map<String, String[]> parameters = req.getParameterMap();
                // 获取方法的形参列表
                Class<?>[] parameterTypes = method.getParameterTypes();
                // 实参
                Object[] paramValues = new Object[parameterTypes.length];
                // 赋予实参
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    // 不能用instanceof ,parameterType它不是实参而是形参
                    if(parameterType == HttpServletRequest.class){
                        paramValues[i] = req;
                        continue;
                    }else if(parameterType == HttpServletResponse.class){
                        paramValues[i] = req;
                        continue;
                    }
                    Annotation[][] pa = method.getParameterAnnotations();
                    Annotation[] annotations = pa[i];
                    for (Annotation a : annotations) {
                        if(a instanceof RequestParam){
                            String paramName = ((RequestParam)a).value();
                            if(parameters.containsKey(paramName)){
                                String value = Arrays.toString(parameters.get(paramName))
                                        .replaceAll("\\[|\\]","")
                                        .replaceAll("\\s","");
                                paramValues[i] = this.convert(parameterType,value);
                            }
                        }
                    }
                }

                String beanName = StrUtil.toLowerCaseFirstOne(method.getDeclaringClass().getSimpleName());
                Object name = method.invoke(abstractFactory.getBeanMap().get(beanName), paramValues);
                resp.setContentType("text/html;charset=UTF-8");
                PrintWriter out = resp.getWriter();
                out.println(name);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void destroy() {
        System.out.println("销毁servlet");
    }

    private void loadHandlerMapping() throws ClassNotFoundException {
        List<String> classNames = abstractFactory.getClassNames();
        if(classNames.isEmpty()){ return;}
        for(String className:classNames){
            Class<?> aClass = Class.forName(className);
            if(!aClass.isAnnotationPresent(Controller.class)){continue;}
            Method[] methods = aClass.getMethods();
            for (Method method : methods){
                if(!method.isAnnotationPresent(RequestMapping.class)){
                    continue;
                }
                String value = method.getAnnotation(RequestMapping.class).name();
                //判断类上有没有RequestMapping标签
                if(!aClass.isAnnotationPresent(RequestMapping.class)){
                    handlerMapping.put(("/"+ value).replaceAll("/+","/"),method);
                }else {
                    handlerMapping.put((aClass.getAnnotation(RequestMapping.class).name()+"/"+ value).replaceAll("/+","/"),method);
                }
            }
        }


    }

    /**
     * url 传过来的参数都是 String 类型的，HTTP 是基于字符串协议
     *      只需要把 String 转换为任意类型就好
     * @param type
     * @param value
     * @return
     */
     private Object convert(Class<?> type,String value){
//          如果是int
        if(Integer.class == type){
            return Integer.valueOf(value);
        }
        /*
            todo 如果还有 double 或者其他类型，继续加 if
            这时候，我们应该想到策略模式了
            在这里暂时不实现
         */
        return value;
    }
}
