package com.hss.service.impl;

import com.hss.annotation.Service;
import com.hss.bean.Dept;
import com.hss.service.DeptService;

@Service(value = "")
public class DeptServiceImpl implements DeptService {

    @Override
    public Dept findDeptById(Long id) {
        System.out.println("执行方法--查找部门");
        return null;
    }
}
