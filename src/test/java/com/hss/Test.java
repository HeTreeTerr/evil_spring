package com.hss;

import com.hss.factory.AbstractFactory;
import com.hss.factory.FactoryBean;
import com.hss.service.DeptService;
import org.junit.Before;

import java.util.List;
import java.util.Map;

public class Test {

    private AbstractFactory abstractFactory;

    @Before
    public void before(){
        abstractFactory = new FactoryBean();
    }

    @org.junit.Test
    public void test(){
        DeptService deptService = (DeptService)abstractFactory.getBeanByBeanName("deptService");
        deptService.findDeptById(4396L);
    }
}
