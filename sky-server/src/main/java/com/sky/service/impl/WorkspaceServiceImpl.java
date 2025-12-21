package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    @Autowired
    DishMapper dishMapper;

    @Autowired
    SetmealMapper setmealMapper;

    @Autowired
    OrdersMapper ordersMapper;

    @Autowired
    UserMapper userMapper;

    @Override
    public DishOverViewVO overviewDishes() {
        DishOverViewVO vo = new DishOverViewVO();
        Integer sold = dishMapper.getByStatus(StatusConstant.ENABLE);
        Integer discontinued = dishMapper.getByStatus(StatusConstant.DISABLE);
        vo = DishOverViewVO.builder().sold(sold).discontinued(discontinued).build();
        return vo;
    }

    @Override
    public BusinessDataVO businessData() {
        LocalDate now = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(now, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(now, LocalTime.MAX);

        BusinessDataVO vo = new BusinessDataVO();
        Map param = new HashMap();
        param.put("begin", beginTime);
        param.put("end", endTime);
        param.put("status", Orders.COMPLETED);

        Double turnover = ordersMapper.getByMap(param);
        //sum获得的结果有可能是null
        turnover = turnover == null? 0.0 : turnover;

        Integer validOrderCount = ordersMapper.getCountByMap(param);
        param.put("status", null);
        Integer allOrderCount = ordersMapper.getCountByMap(param);

        Double orderCompletionRate = 0.0;
        if(allOrderCount!=0){
            orderCompletionRate = validOrderCount.doubleValue()/allOrderCount;
        }

        Integer newUsers = userMapper.selectByMap(param);

        Double unitPrice = 0.0;
        if(validOrderCount!=0){
            unitPrice = turnover/validOrderCount;
        }

        vo = BusinessDataVO.builder()
                .newUsers(newUsers)
                .unitPrice(unitPrice)
                .orderCompletionRate(orderCompletionRate)
                .validOrderCount(validOrderCount)
                .turnover(turnover).build();
        
        return vo;


    }

    @Override
    public OrderOverViewVO overviewOrders() {
        Map map = new HashMap();
        map.put("begin", LocalDateTime.now().with(LocalTime.MIN));
        map.put("end", LocalDateTime.now().with(LocalTime.MAX));
        OrderOverViewVO vo = new OrderOverViewVO();

        map.put("status", Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = ordersMapper.getCountByMap(map);

        map.put("status", Orders.CONFIRMED);
        Integer deliveredOrders = ordersMapper.getCountByMap(map);

        map.put("status", Orders.COMPLETED);
        Integer completedOrders = ordersMapper.getCountByMap(map);

        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = ordersMapper.getCountByMap(map);

        map.put("status", null);
        Integer allOrders = ordersMapper.getCountByMap(map);
        vo = OrderOverViewVO.builder().allOrders(allOrders).cancelledOrders(cancelledOrders).completedOrders(completedOrders).waitingOrders(waitingOrders).deliveredOrders(deliveredOrders).build();
        return vo;
    }

    @Override
    public SetmealOverViewVO getSetmealOverView() {
        SetmealOverViewVO vo = new SetmealOverViewVO();
        Integer sold = setmealMapper.getByStatus(StatusConstant.ENABLE);
        Integer discontinued = setmealMapper.getByStatus(StatusConstant.DISABLE);
        vo = SetmealOverViewVO.builder().sold(sold).discontinued(discontinued).build();
        return vo;
    }
}
