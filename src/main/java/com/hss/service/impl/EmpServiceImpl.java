package com.hss.service.impl;

import com.hss.annotation.Autowired;
import com.hss.annotation.Service;
import com.hss.service.DeptService;
import com.hss.service.EmpService;

@Service(value = "")
public class EmpServiceImpl implements EmpService {

    @Autowired
    public DeptService deptService;

    @Override
    public void findEmpById(Long id) {
        deptService.findDeptById(id);
    }
}
