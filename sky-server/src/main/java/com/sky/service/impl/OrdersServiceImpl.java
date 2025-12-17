package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sky.entity.Orders.*;

@Service
@Slf4j
public class OrdersServiceImpl implements OrderService {
    @Autowired
    OrdersMapper ordersMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    AddressBookMapper addressBookMapper;

    @Autowired
    ShoppingCartMapper shoppingCartMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    WeChatPayUtil weChatPayUtil;

    @Autowired
    private WebSocketServer webSocketServer;

    //便于快速获取 只在跳过支付情况下使用
    private Orders orders;

    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //地址簿或者购物车是否为空
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectByShoppingCart(shoppingCart);
        if(shoppingCartList==null||shoppingCartList.size()==0){
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setStatus(Orders.PENDING_PAYMENT);
        LocalDateTime now = LocalDateTime.now();
        orders.setOrderTime(now);
        orders.setPayStatus(Orders.UN_PAID);
        //BeanUtils.copyProperties(addressBook, orders);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orders.setAddress(addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());
        //便于快速获取 只在跳过支付情况下使用
        this.orders = orders;
        //订单号 使用当前时间的时间戳
        orders.setNumber(String.valueOf(System.currentTimeMillis()));

        ordersMapper.insert(orders);


        List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>();
        for(ShoppingCart Cart: shoppingCartList){
                OrderDetail orderDetail = new OrderDetail();
                BeanUtils.copyProperties(Cart, orderDetail);
                orderDetail.setOrderId(orders.getId());
                orderDetailList.add(orderDetail);

        }

        log.info("begin insert");
        orderDetailMapper.insert(orderDetailList);


        log.info("finish insert");
        //清空购物车
        shoppingCartMapper.deleteAll(userId);
        log.info("delete");


        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setId(orders.getId());
        orderSubmitVO.setOrderTime(orders.getOrderTime());
        orderSubmitVO.setOrderNumber(orders.getNumber());
        orderSubmitVO.setOrderAmount(orders.getAmount());
        log.info("build");




        return orderSubmitVO;
    }




    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectByid(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));



        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code","ORDERPAID");

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        Integer OrderPaidStatus = Orders.PAID;//支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单
        LocalDateTime check_out_time = LocalDateTime.now();//更新支付时间
        ordersMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, this.orders.getId());

        //以下代码只是为了测试
        Map map = new HashMap();
        map.put("type",1);//1来单提醒 2客户催单
        map.put("orderId",this.orders.getId());
        map.put("content","订单号："+this.orders.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);








        return vo;
    }




    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    @Override
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = ordersMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.update(orders);

        //通过websocket通报下单
        Map map = new HashMap();
        map.put("type",1);//1来单提醒 2客户催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号："+ordersDB.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        // 根据订单号查询订单
        Orders orders = ordersMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //支付状态
        Integer payStatus = orders.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款
//            String result = weChatPayUtil.refund(
//                    orders.getNumber(),//商户订单号
//                    orders.getNumber(),//商户退款单号
//                    orders.getAmount(),//不知道退款金额是多少
//                    orders.getAmount() //原订单金额，单位 元
//
//            );
            //log.info("申请退款：{}", result);
        }

        Orders ordersNew = new Orders();
        ordersNew.setId(orders.getId());
        ordersNew.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        ordersNew.setPayStatus(Orders.REFUND);
        ordersNew.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.update(ordersNew);
    }

    @Override
    public OrderStatisticsVO statisticis() {
        Integer confirmedNumber = ordersMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgressNumber = ordersMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer toBeConfirmed = ordersMapper.countStatus(Orders.TO_BE_CONFIRMED);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmedNumber);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgressNumber);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        return orderStatisticsVO;
    }

    @Override
    public void complete(Long id) {
        Orders orders = ordersMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (orders == null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders ordersNew = new Orders();
        orders.setId(orders.getId());
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        ordersMapper.update(orders);


    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        // 根据订单号查询订单
        Orders orders = ordersMapper.getById(ordersCancelDTO.getId());
        log.info("begin cancel");

        //检查支付状态
        Integer payStatus = orders.getPayStatus();
//        if (payStatus == 1) {
//            //用户已支付，需要退款
//            String result = weChatPayUtil.refund(
//                    orders.getNumber(),//商户订单号
//                    orders.getNumber(),//商户退款单号
//                    orders.getAmount(),//不知道退款金额是多少
//                    orders.getAmount() //原订单金额，单位 元
//
//            );
//            log.info("申请退款：{}", result);
//        }

        Orders ordersNew = new Orders();
        ordersNew.setId(ordersCancelDTO.getId());
        ordersNew.setStatus(Orders.CANCELLED);
        ordersNew.setCancelReason(ordersCancelDTO.getCancelReason());
        ordersNew.setCancelTime(LocalDateTime.now());
        ordersNew.setPayStatus(Orders.REFUND);
        ordersMapper.update(ordersNew);
        log.info("finish cancel");

    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        ordersMapper.update(orders);
    }

    @Override
    public OrderVO detail(Long id) {
        Orders orders = ordersMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        List<OrderDetail> orderDetailList = orderDetailMapper.getAllByOrderId(orders.getId());
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void delivery(Long id) {
        Orders orders = ordersMapper.getById(id);
        // 校验订单是否存在，并且状态为3
        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders ordersNew = new Orders();
        ordersNew.setId(orders.getId());
        ordersNew.setStatus(Orders.DELIVERY_IN_PROGRESS);
        ordersMapper.update(ordersNew);
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = ordersMapper.pageQuery(ordersPageQueryDTO);
        Long total = page.getTotal();
        List<Orders> ordersList = page.getResult();
        List<OrderVO> list = new ArrayList();

        if(ordersList != null && ordersList.size() > 0){
            for(Orders orders : ordersList){
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);
                orderVO.setOrderDishes(orderDishes);
                list.add(orderVO);
            }
        }

        PageResult pageResult = new PageResult(total,list);
        return pageResult;
    }

    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getAllByOrderId(orders.getId());

        // 用来存每一道菜的字符串
        List<String> orderDishList = new ArrayList<>();

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        for (OrderDetail x : orderDetailList) {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            orderDishList.add(orderDish);
        }

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }


    @Override
    public void reminder(Long id) {
        Orders orders = ordersMapper.getById(id);

    }

    @Override
    public void repetition(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getAllByOrderId(id);
        //核心思路就是把orderdetail又插入一次shoppingcart
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        for (OrderDetail x : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public PageResult pageQuery4User(int page, int pageSize, Integer status) {
        PageHelper.startPage(page,pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        Page<Orders> pageQuery = ordersMapper.pageQuery(ordersPageQueryDTO);
        Long total = pageQuery.getTotal();
        List<Orders> ordersList = pageQuery.getResult();

        //接下来将orders转换为ordersvo
        List<OrderVO> list = new ArrayList();
        if(ordersList != null && ordersList.size() > 0){
            for(Orders orders : ordersList){
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                List<OrderDetail> orderDetailList = orderDetailMapper.getAllByOrderId(orders.getId());
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }

        PageResult pageResult = new PageResult(total,list);
        return pageResult;

    }

    @Override
    public void userCancelById(Long id) throws Exception {
        // 根据订单号查询订单
        Orders orders = ordersMapper.getById(id);

        //删除订单要查这个订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if (orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders newOrders = new Orders();
        newOrders.setId(id);

//        if(orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
//            //退款
//            String result = weChatPayUtil.refund(
//                    orders.getNumber(),//商户订单号
//                    orders.getNumber(),//商户退款单号
//                    orders.getAmount(),//不知道退款金额是多少
//                    orders.getAmount() //原订单金额，单位 元
//
//            );
//            log.info("退款结束" + result);
//            orders.setPayStatus(Orders.REFUND);
//        }

        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason("用户取消");
        orders.setStatus(Orders.CANCELLED);
        ordersMapper.update(orders);





        //if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

    }


}
