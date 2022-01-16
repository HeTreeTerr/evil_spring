package com.hss.controller;

import com.hss.annotation.*;
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
    public String helloSpring(@RequestParam(value = "id") String id){
        deptService.findDeptById(Long.valueOf(id));
        return "hello Spring";
    }

    @RequestMapping(name = "/getDept")
    public String getDept(@RequestParam(value = "name") String name,
                          @RequestParam(value = "cardNo") String cardNo){
        return "getDept param{" + name + "--"+ cardNo +"}";
    }
}
