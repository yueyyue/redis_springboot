package com.atguigu.demo.redis_springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @auther sparklight
 */
@RestController
@RequestMapping("/redisTest")
public class RedisTestController {

    @Autowired //注入RedisTemplate
    private RedisTemplate redisTemplate;

    //测试分布式锁. 锁指定一个唯一的uuid,防止误删 ==> 出现新的问题, 删除操作缺乏原子性.
    @GetMapping("testLockByUUID")
    public void testLock2() {
        String uuid = UUID.randomUUID().toString();
        //1. 获取锁, setne
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);//,3,TimeUnit.SECONDS 3秒后过期
        //2. 获取锁成功,查询num的值
        if (lock) {
            Object value = redisTemplate.opsForValue().get("num");
            //2.1 判断num为null return
            if (StringUtils.isEmpty(value)) {
                return;
            }
            //2.2 有值就转化成int
            int num = Integer.parseInt(value + "");
            //2.3 把 redis 的num + 1
            redisTemplate.opsForValue().set("num", ++num);
            //2.4 释放锁, del
            //判断此时获取到的uuid和开始生成的uuid值是否一样
            String lockUUid = (String) redisTemplate.opsForValue().get("lock");
            if (lockUUid.equals(uuid)) {
                redisTemplate.delete("lock");
            }
        } else {
            //3. 获取锁失败, 每隔0.1s 再获取
            try {
                Thread.sleep(100);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    //测试分布式锁. 一般操作 ==> 引出问题: 可能出现误删除其他服务器的锁 ==> 使用uuid解决
    @GetMapping("testLock")
    public void testLock() {
        //1. 获取锁, setne
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "111", 3, TimeUnit.SECONDS);//,3,TimeUnit.SECONDS 3秒后过期
        //2. 获取锁成功,查询num的值
        if (lock) {
            Object value = redisTemplate.opsForValue().get("num");
            //2.1 判断num为null return
            if (StringUtils.isEmpty(value)) {
                return;
            }
            //2.2 有值就转化成int
            int num = Integer.parseInt(value + "");
            //2.3 把 redis 的num + 1
            redisTemplate.opsForValue().set("num", ++num);
            //2.4 释放锁, del
            redisTemplate.delete("lock");
        } else {
            //3. 获取锁失败, 每隔0.1s 再获取
            try {
                Thread.sleep(100);
                testLock();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @GetMapping
    public String testRedis() {
        //向redis中设置值
        redisTemplate.opsForValue().set("name", "lucy");
        String name = (String) redisTemplate.opsForValue().get("name");
        return name;
    }
}
