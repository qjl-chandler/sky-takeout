package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    void add(ShoppingCartDTO cartDTO);

    List<ShoppingCart> list();

    void deleteAll();

    void sub(ShoppingCartDTO cartDTO);
}
