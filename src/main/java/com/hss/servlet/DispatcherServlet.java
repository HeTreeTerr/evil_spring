package com.hss.servlet;

import com.hss.annotation.Controller;
import com.hss.annotation.RequestMapping;
import com.hss.annotation.RequestParam;
import com.hss.factory.AbstractFactory;
import com.hss.factory.FactoryBean;
import com.hss.handler.Handler;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatcherServlet extends HttpServlet {

    //保存所有的 Url 和方法的映射关系
    private List<Handler> handlerMapping = new ArrayList<Handler>();

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
        Handler handler = null;
        try {
            handler = getHandler(req);
            if(handler == null){
                resp.getWriter().write("404 Not Found!!!");
                return;
            }

            // 获取方法的形参列表
            Class<?>[] parameterTypes = handler.parameterTypes;
            // 实参
            Object[] paramValues = new Object[parameterTypes.length];

            Map<String, String[]> parameters = req.getParameterMap();
            // 赋予实参
            for (Map.Entry<String, String[]> param : parameters.entrySet()) {
                String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","")
                        .replaceAll("\\s","");
                if(!handler.paramIndexMapping.containsKey(param.getKey())){continue;}

                Integer index = handler.paramIndexMapping.get(param.getKey());
                paramValues[index] = convert(parameterTypes[index], value);
            }

            if(handler.paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
                int reqIndex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
                paramValues[reqIndex] = req;
            }

            if(handler.paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
                int respIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
                paramValues[respIndex] = resp;
            }
            //执行方法
            Object returnValue = handler.method.invoke(handler.controller,paramValues);
            //返回结果
            if(returnValue == null || returnValue instanceof Void){
                return;
            }
            resp.getWriter().write(returnValue.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void destroy() {
        System.out.println("销毁servlet");
    }

    private void loadHandlerMapping() throws ClassNotFoundException {
        Map<String, Object> ioc = abstractFactory.getBeanMap();
        if(ioc.isEmpty()){ return; }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> aClass = entry.getValue().getClass();
            if(!aClass.isAnnotationPresent(Controller.class)){continue;}
            Method[] methods = aClass.getMethods();
            for (Method method : methods){
                if(!method.isAnnotationPresent(RequestMapping.class)){
                    continue;
                }
                String value = method.getAnnotation(RequestMapping.class).name();
                //判断类上有没有RequestMapping标签
                String url;
                if(!aClass.isAnnotationPresent(RequestMapping.class)){
                    url = ("/"+ value).replaceAll("/+","/");
                }else {
                    url = (aClass.getAnnotation(RequestMapping.class).name()+"/"+ value).replaceAll("/+","/");
                }
                Pattern pattern = Pattern.compile(url);
                handlerMapping.add(new Handler(pattern, entry.getValue(), method));
            }
        }


    }

    private Handler getHandler(HttpServletRequest req) throws Exception{
        if(handlerMapping.isEmpty()){
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        for (Handler handler : handlerMapping) {
            try{Matcher matcher = handler.pattern.matcher(url);
                //如果没有匹配上继续下一个匹配
                 if(!matcher.matches()){ continue;
                 } return handler;
            }catch(Exception e){
                throw e;
            }
        }return null;
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
