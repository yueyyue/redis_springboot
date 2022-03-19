package com.atguigu.demo.redis_springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 1.0
 * @auther sparklight
 */
@RestController
@RequestMapping("/redisTest")
public class RedisTestController {

    @Autowired //注入RedisTemplate
    private RedisTemplate redisTemplate;

    @GetMapping
    public String testRedis() {
        //向redis中设置值
        redisTemplate.opsForValue().set("name","lucy");
        String  name = (String)redisTemplate.opsForValue().get("name");
        return name;
    }
}
