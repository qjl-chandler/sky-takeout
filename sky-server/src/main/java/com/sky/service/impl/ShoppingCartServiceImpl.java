package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;


    @Override
    public void add(ShoppingCartDTO cartDTO) {
        ShoppingCart cart = new ShoppingCart();
        BeanUtils.copyProperties(cartDTO, cart);
        Long userId = BaseContext.getCurrentId();
        cart.setUserId(userId);

        //1.是否存在
        List<ShoppingCart> results = shoppingCartMapper.selectByShoppingCart(cart);

        if (results != null && results.size() > 0) {
            results.forEach(shoppingCart -> {
                shoppingCart.setNumber(shoppingCart.getNumber() + 1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            });
        }else{

            if(cart.getDishId()!=null){
                Dish dish = dishMapper.getById(cart.getDishId());
                cart.setName(dish.getName());
                cart.setImage(dish.getImage());
                cart.setAmount(dish.getPrice());

            }else if(cart.getSetmealId()!=null){
                Setmeal setmeal = setmealMapper.getSetmealById(cart.getSetmealId());
                cart.setName(setmeal.getName());
                cart.setImage(setmeal.getImage());
                cart.setAmount(setmeal.getPrice());

            }
            cart.setNumber(1);
            cart.setCreateTime(LocalDateTime.now());

            shoppingCartMapper.insert(cart);

        }
    }

    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectByShoppingCart(cart);
        return shoppingCartList;
    }

    @Override
    public void deleteAll() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteAll(userId);

    }

    @Override
    public void sub(ShoppingCartDTO cartDTO) {
        ShoppingCart cart = new ShoppingCart();
        BeanUtils.copyProperties(cartDTO, cart);
        Long userId = BaseContext.getCurrentId();
        cart.setUserId(userId);
        List<ShoppingCart> results = shoppingCartMapper.selectByShoppingCart(cart);
        if (results != null && results.size() > 0) {
            results.forEach(shoppingCart -> {
                Integer number = shoppingCart.getNumber();
                if (number == 1) {
                    shoppingCartMapper.sub(shoppingCart);
                }else{
                    shoppingCart.setNumber(number - 1);
                    shoppingCartMapper.updateNumberById(shoppingCart);
                }


            });
        }

    }
}
