package com.jayjay.formlogin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Copyright © 2020-ESS
 *
 * @Project: MRC
 * @Author: JayJay
 * @Date: 8/1/2021
 * @ClassName: HelloController
 * @Description:
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(){
        return "hello spring security";
    }
}
