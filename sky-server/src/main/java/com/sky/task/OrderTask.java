package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/*
    定时任务类 定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;

    //@Scheduled(cron="0/5 * * * * ?")用于测试

    //处理超时订单的方法 15分钟
    @Scheduled(cron="0 * * * * ?")//每分钟触发一次
    //@Scheduled(cron="1/5 * * * * ?")
    public void processTimeoutOrder(){
        log.info("处理超时订单");
        long out = 15L;
        LocalDateTime outTime = LocalDateTime.now().minusMinutes(out);
        List<Orders> timeOutOrders = ordersMapper.getOrderByStatusAndTime(Orders.PENDING_PAYMENT, outTime);
        if(timeOutOrders!=null && timeOutOrders.size()>0){
            timeOutOrders.forEach(order -> {
                Long id = order.getId();
                Orders tempOrders = new Orders();
                tempOrders.setId(id);
                tempOrders.setStatus(Orders.CANCELLED);
                tempOrders.setCancelReason("订单超时，自动取消");
                tempOrders.setCancelTime(LocalDateTime.now());
                ordersMapper.update(tempOrders);
            });

        }
    }

    //处理一直派送中的订单 上一天的
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨一点触发一次
    //@Scheduled(cron="0/5 * * * * ?")
    public void processDeliveryOrder(){
        log.info("处理一直派送中的订单");
        long out = 1L;
        LocalDateTime outTime = LocalDateTime.now().minusHours(out);
        List<Orders> timeOutOrders = ordersMapper.getOrderByStatusAndTime(Orders.DELIVERY_IN_PROGRESS, outTime);
        if(timeOutOrders!=null && timeOutOrders.size()>0){
            timeOutOrders.forEach(order -> {
                Long id = order.getId();
                Orders tempOrders = new Orders();
                tempOrders.setId(id);
                tempOrders.setStatus(Orders.COMPLETED);
                ordersMapper.update(tempOrders);
            });

        }

    }



}
