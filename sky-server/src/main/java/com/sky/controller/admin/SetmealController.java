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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    SetmealService setmealService;


    //新增套餐导致“该套餐分类id 比如人气套餐”下所有套餐与数据库不一致，例如缓存里3个套餐，数据库4个，所以需要清除缓存中“这一个分类id”下的所有套餐，
    // 用户查询时重新从数据库获取最新的数据
    @PostMapping()
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
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
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
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
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update (@RequestBody SetmealDTO setmealDTO) {
        setmealService.updateSetmealAndSetmeanDishes(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    //查两次太麻烦所以一起删除
    public Result updateSetmealStatus(@PathVariable Integer status, Long id) {
        setmealService.updateSetmealStatus(status,id);
        return Result.success();

    }

}
