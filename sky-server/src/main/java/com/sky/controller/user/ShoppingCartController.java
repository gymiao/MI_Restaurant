package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "购物车管理")
public class ShoppingCartController {

    @Resource
    ShoppingCartService shoppingCartService;

    @ApiOperation("添加购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    @ApiOperation("查询购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        List<ShoppingCart> shoppingCarts = shoppingCartService.list();
        return Result.success(shoppingCarts);
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clean")
    public Result clean() {
        shoppingCartService.clean();
        return Result.success();
    }

    @ApiOperation("删除购物车中一个商品")
    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }

}
