package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@Slf4j
@RequestMapping("/admin/workspace")
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes() {
        DishOverViewVO vo = workspaceService.overviewDishes();
        return Result.success(vo);
    }

    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData() {
        LocalDate now = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(now, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(now, LocalTime.MAX);
        BusinessDataVO vo = workspaceService.businessData(beginTime,endTime);
        return Result.success(vo);
    }

    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders() {
        OrderOverViewVO vo = workspaceService.overviewOrders();
        return Result.success(vo);
    }

    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> setmealOverView(){
        SetmealOverViewVO vo = workspaceService.getSetmealOverView();
        return Result.success(vo);
    }





}
