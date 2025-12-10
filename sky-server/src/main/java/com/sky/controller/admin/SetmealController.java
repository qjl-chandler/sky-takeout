package com.sky.controller.admin;

import com.github.pagehelper.Page;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    SetmealService setmealService;


    @PostMapping()
    public Result insert (@RequestBody SetmealDTO setmealDTO) {
        setmealService.insert(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping()
    //前段传过来的是字符串 想要按照数组接收 需要加注解
    public Result delete (@RequestParam List<Long> ids) {
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SetmealVO> select(@PathVariable Long id) {
        SetmealVO setmealVO = setmealService.getSetmealAndSetmeanDishes(id);
        return Result.success(setmealVO);
    }

    @PutMapping()
    public Result update (@RequestBody SetmealDTO setmealDTO) {
        setmealService.updateSetmealAndSetmeanDishes(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result updateSetmealStatus(@PathVariable Integer status, Long id) {
        setmealService.updateSetmealStatus(status,id);
        return Result.success();

    }

}
