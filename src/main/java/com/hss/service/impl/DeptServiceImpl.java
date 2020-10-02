package com.hss.service.impl;

import com.hss.annotation.Autowired;
import com.hss.annotation.Service;
import com.hss.bean.Dept;
import com.hss.service.DeptService;
import com.hss.service.EmpService;

@Service(value = "")
public class DeptServiceImpl implements DeptService {

    @Autowired
    public EmpService empService;

    @Override
    public Dept findDeptById(Long id) {
        System.out.println("执行方法--查找部门");
        return new Dept(id,"研发部");
    }
}
