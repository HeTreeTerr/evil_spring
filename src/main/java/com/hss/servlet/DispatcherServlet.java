package com.hss.servlet;

import com.hss.annotation.Controller;
import com.hss.annotation.RequestMapping;
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
                String beanName = StrUtil.toLowerCaseFirstOne(method.getDeclaringClass().getSimpleName());
                // todo 参数还是死的
                Object name = method.invoke(abstractFactory.getBeanMap().get(beanName), new Object[]{ req.getParameterValues("id")[0]});
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
                handlerMapping.put((aClass.getAnnotation(RequestMapping.class).name()+"/"+ value).replaceAll("/+","/"),method);
            }
        }


    }
}
