package com.nowcoder.community.controller;

import com.nowcoder.community.until.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot.";
    }


    @RequestMapping(path ="/ajax", method = RequestMethod.POST)
    @ResponseBody
    public  String testAjax(String name,int age){
        System.out.println("name:"+name);

        return CommunityUtil.getJSONString(0, "操作成功");
    }
}



