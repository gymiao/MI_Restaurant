package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "用户查询营业状态接口")
public class ShopController {
    public static final String key = "SHOP_STATUS";

    @Resource
    RedisTemplate redisTemplate;

    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(key);
        return Result.success(status);
    }
}
