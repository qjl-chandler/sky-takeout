package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    void paySuccess(String outTradeNo);


    void reject(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    OrderStatisticsVO statisticis();

    void complete(Long id);

    void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    OrderVO detail(Long id);

    void delivery(Long id);

    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    void reminder(Long id);

    void repetition(Long id);

    PageResult pageQuery4User(int page, int pageSize, Integer status);

    void userCancelById(Long id) throws Exception;
}
