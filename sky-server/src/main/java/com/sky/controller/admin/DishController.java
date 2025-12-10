package com.sky.controller.admin;

import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 分类分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping()
    //前段传过来的是字符串 想要按照数组接收 需要加注解
    public Result deleteTogether(@RequestParam List<Long> ids) {
        log.info("菜品批量删除，{}", ids);
        dishService.delete(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DishVO> findById(@PathVariable Long id) {
        log.info("<UNK>{}", id);
        DishVO dishVO = dishService.findByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("<UNK>{}", dishDTO);
        dishService.update(dishDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Dish>> findAll(Long categoryId) {
        log.info("<UNK>{}", categoryId);
        List<Dish> dish_list = dishService.selectByCategoryId(categoryId);
        return Result.success(dish_list);

    }
}
