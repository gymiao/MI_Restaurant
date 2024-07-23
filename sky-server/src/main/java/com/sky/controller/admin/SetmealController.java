package com.sky.controller.admin;

import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐管理")
public class SetmealController {

    @Resource
    SetmealService setmealService;

    @PostMapping
    @ApiOperation("新增套餐")
    public Result saveSetmealDish(@RequestBody SetmealDTO setmealDTO) {
        setmealService.saveSetmealDish(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据Id查询")
    public Result<SetmealVO> getById(@PathVariable("id") Long id) {
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }

    @DeleteMapping
    @ApiOperation("批量删除")
    public Result deleteBatch(@RequestParam("ids")List<Long> ids) {
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售、停售")
    public Result startOrStop(@PathVariable("status")Integer status, Long id) {
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO setmealDTO) {
        setmealService.update(setmealDTO);
        return Result.success();
    }
}
