package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    List<ShoppingCart> selectByShoppingCart(ShoppingCart cart);


    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    @Insert("insert into shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time, number) " +
            "values "+
            "(#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{amount}, #{createTime}, #{number})")
    void insert(ShoppingCart cart);

    @Delete("delete from shopping_cart where user_id = #{user_id}")
    void deleteAll(Long userId);

    void sub(ShoppingCart shoppingCart);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}

