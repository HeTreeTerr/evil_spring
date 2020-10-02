package com.hss.controller;

import com.hss.annotation.Autowired;
import com.hss.annotation.Controller;
import com.hss.annotation.RequestMapping;
import com.hss.service.DeptService;
import com.hss.service.impl.DeptServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(name = "/hello")
public class DeptController {

    @Autowired
    public DeptService deptService;

    @RequestMapping(name = "/spring")
    public String helloSpring(String id){
        deptService.findDeptById(Long.valueOf(id));
        return "hello Spring";
    }
}
