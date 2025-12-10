package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@Slf4j
@RequestMapping("/admin/shop")
public class ShopController {
    @Autowired
    RedisTemplate redisTemplate;

    public static final String key = "SHOP_STATUS";

    @PutMapping("/{status}")
    public Result updateStatus(@PathVariable Integer status) {
        redisTemplate.opsForValue().set(key, status);
        return Result.success();
    }

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(key);

        return Result.success(status);
    }


}
