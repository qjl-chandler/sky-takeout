package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Employee;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    //涉及菜品表和口味表 多表操作需要考虑事务
    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);

        //比较特别的问题 就是两张表有关联数据 从flavors中无法获取dishid 需要从dish中获取 获取inser语句生成的主键值
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> {flavor.setDishId(dishId);});
            dishFlavorMapper.insertBatch(flavors);
        }


    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //1.开始分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        //2分页查询
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        //3制作PageResult
        long total = page.getTotal();
        List<DishVO> records = page.getResult();
        PageResult pageResult = new PageResult(total, records);

        return pageResult;
    }

    @Transactional
    @Override
    public void delete(List<Long> ids) {
        for (long id : ids) {
            //查询当前菜品是否起售，如果起售了就抛出业务异常
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //菜品起售，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //查询当前ID是否关联了套餐，如果关联了就抛出业务异常
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds!=null && setmealIds.size()>0) {
            //当前id有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除套餐数据
//        for (long id : ids) {
//            dishMapper.deleteById(id);
//            dishFlavorMapper.deleteByDishId(id);
//        }

        //优化为批量删除菜品
        dishMapper.deleteBatch(ids);
        dishFlavorMapper.deleteBatch(ids);

    }


    @Override
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        //口味难度很高 有非常多的可能性 我打算把和这个dish关联的口味全部删除然后再插入
        //第一步 先删除
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //第二步 再插入
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(flavor -> {flavor.setDishId(dish.getId());});
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    @Override
    public DishVO findByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        List<DishFlavor> flavors = dishFlavorMapper.getFlavorsById(id);
        dishVO.setFlavors(flavors);
        return dishVO;
    }


    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> selectByCategoryId(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }


}
