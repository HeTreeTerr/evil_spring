package com.hss.factory;

import com.hss.annotation.Autowired;
import com.hss.annotation.Component;
import com.hss.annotation.Controller;
import com.hss.annotation.Service;
import com.hss.util.GridProperties;
import com.hss.util.SPIExtensionLoader;
import com.hss.util.StrUtil;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class FactoryBean implements AbstractFactory{

    private static Map<String,Object> beanMap = new LinkedHashMap<String, Object>();

    private static Map<String,Object> beanMapCache = new LinkedHashMap<String, Object>();

    private List<String>  classNames = new ArrayList<>();

    private static String scanPackage = null;

    public FactoryBean() {
        try {
            refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refresh() throws IOException {
        initSingletonBean();
    }

    private void initSingletonBean() throws IOException {
        //1.读取配置文件
        scanPackage = GridProperties.SCAN_PACKAGE;
        //2.扫描该包下的所有class文件
        String packAgeDir = scanPackage.replace('.', '/');
        Enumeration<URL> urls = this.getClass().getClassLoader().getResources(packAgeDir);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if("file".equals(url.getProtocol())){
                //获取物理路径
                String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                //通过物理路径获取文件对象，捕捉合适的类放入工厂
                this.findClassesByFile(filePath,scanPackage);
            }else if ("jar".equals(url.getProtocol())) {
                // 如果是jar包文件 TODO
            }
        }
    }

    @Override
    public Object getBeanByBeanName() {
        return null;
    }

    @Override
    public Map<String, Object> getBeanMap() {
        return beanMap;
    }

    @Override
    public List<String> getClassNames() {
        return classNames;
    }

    private void findClassesByFile(String filePath, String pkgName){
        //1. 获取此包的目录 建立一个File
        File dir = new File(filePath);
        //2. 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        //3.获取目录下的文件夹或class文件
        File[] dirfiles = dir.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith("class"));
        if(null != dirfiles && dirfiles.length > 0){
            for(File file : dirfiles){
                if(file.isDirectory()){
                    findClassesByFile(file.getPath(),pkgName+"."+file.getName());
                }else {
                    //锁定class文件，识别自定义的标签。并将符合条件的放入bean工厂
                    try {
                        reflectionAndAnnotationHanlder(file, pkgName);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void reflectionAndAnnotationHanlder(File file,String pkgName) throws IllegalAccessException, InstantiationException {

        try {
            String className = file.getName().substring(0, file.getName().length() - 6);
            classNames.add(pkgName + "." + className);
            //通过反射获取类对象
            Class<?> aClass = Class.forName(pkgName + "." + className);
            boolean isAnnotation = aClass.isAnnotation();
            String beanName = this.createBeanName(aClass,className);
            //先从缓存工厂中拿，没有再去生产
            if(null != beanName && !"".equals(beanName)){
                if(beanMapCache.containsKey(beanName)){
                    beanMap.put(beanName,beanMapCache.get(beanName));
                    beanMapCache.remove(beanName);
                }else{
                    beanMap.put(beanName,aClass.newInstance());
                }
            }
            //处理Autowired注解
            try {
                autowiredAnnotationHandler(aClass,className);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void reflectionAndAnnotationHanlderCache(Class<?> aClass) throws IllegalAccessException, InstantiationException {
        String className = aClass.getSimpleName();
        boolean isAnnotation = aClass.isAnnotation();
        String beanName = this.createBeanName(aClass,className);

        if(null != beanName && !"".equals(beanName)){
            beanMapCache.put(beanName,aClass.newInstance());
        }
        //处理Autowired注解
        try {
            autowiredAnnotationHandler(aClass,className);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private String createBeanName(Class<?> aClass,String className){
        boolean isAnnotation = aClass.isAnnotation();
        String beanName = null;
        if(!isAnnotation){
            //判断类对象有没有被标签注解
            boolean annotationComponent = aClass.isAnnotationPresent(Component.class);
            boolean annotationService = aClass.isAnnotationPresent(Service.class);
            boolean annotationController = aClass.isAnnotationPresent(Controller.class);
            //包含该注解，同时自身不是注解
            if(annotationComponent){//处理Component注解
                beanName = StrUtil.toLowerCaseFirstOne(className);
            }else if(annotationService){//处理Service注解
                //判断标签的属性值
                Service mt = aClass.getAnnotation(Service.class);
                String value =  mt.value();
                if(null == value || "".equals(value)){//没有自定义beanName
                    Class<?> inter[]=null;//声明一个对象数组
                    inter=aClass.getInterfaces();//获取类实现的所有接口
                    //当且仅当类只有一个实现类时，采用接口名作为beanName
                    if(null != inter && inter.length == 1){
                        String interName = inter[0].getSimpleName();
                        beanName = StrUtil.toLowerCaseFirstOne(interName);
                    }else{
                        beanName = StrUtil.toLowerCaseFirstOne(className);
                    }
                }else{//自定义beanName
                    beanName = value;
                }

            }else if(annotationController){//处理Controller注解
                Controller mt = aClass.getAnnotation(Controller.class);
                String value =  mt.value();
                if(null == value || "".equals(value)){
                    beanName = StrUtil.toLowerCaseFirstOne(className);
                }else{
                    beanName = value;
                }
            }
        }
        return beanName;
    }

    private void autowiredAnnotationHandler(Class<?> aClass,String className) throws IllegalAccessException, InstantiationException {

        /*获得域*/
        Field[]fields=aClass.getDeclaredFields();
        //返回值用于判断是否完成这个方法
        boolean b=false;
        //遍历域
        for (Field f:fields) {
            Annotation[] anns=f.getAnnotations();
            for (Annotation ann:anns) {
                if(ann instanceof Autowired){
                    Object annObj = beanMap.get(f.getName());
                    if(null == annObj){//bean还没有生产出
                        Class c2= (Class) f.getGenericType();
                        ////生产一个出来
                        if(c2.isInterface()){//如果是接口，获取唯一的实现类
                            Object extension = SPIExtensionLoader.loadExtension(c2);
                            reflectionAndAnnotationHanlderCache(extension.getClass());
                        }else{
                            reflectionAndAnnotationHanlderCache(c2);
                        }
                        f.set(beanMap.get(StrUtil.toLowerCaseFirstOne(className)),beanMapCache.get(f.getName()));
                    }else{
                        f.set(beanMap.get(StrUtil.toLowerCaseFirstOne(className)),beanMap.get(f.getName()));
                    }
                }
            }
        }

    }
}
