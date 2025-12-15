package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;


    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO cartDTO) {
        shoppingCartService.add(cartDTO);

        return Result.success();

    }

    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        List<ShoppingCart> shoppingCartList = shoppingCartService.list();
        return Result.success(shoppingCartList);

    }

    @DeleteMapping("/clean")
    public Result cleanAll() {
        shoppingCartService.deleteAll();
        return Result.success();

    }

    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO cartDTO) {
        shoppingCartService.sub(cartDTO);
        return Result.success();

    }
}
