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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //0.构造redis中的key 规则dish_分类id
        String key = "dish_"+dishDTO.getCategoryId();
        //redisTemplate.delete(key);
        cleanCache(key);
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

        //ids.forEach(id -> {redisTemplate.delete("dish_"+id);});
        //没有必要这么麻烦
        //redisTemplate.delete("dish_*");
        //删除的时候无法识别通配符
        cleanCache("dish_*");

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

        //0.构造redis中的key 规则dish_分类id
        //如果修改分类会影响多份数据
        //redisTemplate.delete("dish_*");
        //删除的时候无法识别通配符
        cleanCache("dish_*");

        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result updateStatus(@PathVariable Integer status, Long id) {
        dishService.updateStatus(status,id);
        //id又要去查数据库才能找到categoryid 得不偿失
        cleanCache("dish_*");
        return Result.success();


    }

    @GetMapping("/list")
    public Result<List<Dish>> findAll(Long categoryId) {
        log.info("<UNK>{}", categoryId);
        List<Dish> dish_list = dishService.selectByCategoryId(categoryId);
        return Result.success(dish_list);

    }

    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
