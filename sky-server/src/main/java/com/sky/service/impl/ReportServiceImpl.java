package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    OrdersMapper ordersMapper;

    @Autowired
    UserMapper userMapper;


    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate nowDate = begin;
        while (nowDate.isBefore(end)) {
            dateList.add(nowDate);
            nowDate = nowDate.plusDays(1);
        }
        dateList.add(end);
        turnoverReportVO.setDateList(StringUtils.join(dateList,","));

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime todayMin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime todayMax = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", todayMin);
            map.put("end", todayMax);
            map.put("status", Orders.COMPLETED);
            Double turnover = ordersMapper.getByMap(map);

            turnover = turnover == null ? 0 : turnover;


            turnoverList.add(turnover);
        }
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList,","));
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        UserReportVO userReportVO = new UserReportVO();
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate nowDate = begin;
        while (nowDate.isBefore(end)) {
            dateList.add(nowDate);
            nowDate = nowDate.plusDays(1);
        }
        dateList.add(end);
        userReportVO.setDateList(StringUtils.join(dateList,","));

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate localDate : dateList) {
            LocalDateTime todayMin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime todayMax = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", todayMin);
            Integer totalUser = userMapper.selectByMap(map);
            totalUserList.add(totalUser);
            map.put("end", todayMax);
            Integer newUser = userMapper.selectByMap(map);
            newUserList.add(newUser);
        }

        userReportVO.setTotalUserList(StringUtils.join(totalUserList,","));
        userReportVO.setNewUserList(StringUtils.join(newUserList,","));
        return userReportVO;

    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        OrderReportVO orderReportVO = new OrderReportVO();
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate nowDate = begin;
        while (nowDate.isBefore(end)) {
            dateList.add(nowDate);
            nowDate = nowDate.plusDays(1);
        }
        dateList.add(end);
        orderReportVO.setDateList(StringUtils.join(dateList,","));

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Map map = new HashMap();
        for (LocalDate localDate : dateList) {
            LocalDateTime todayMin = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime todayMax = LocalDateTime.of(localDate, LocalTime.MAX);

            map.put("begin", todayMin);
            map.put("end", todayMax);

            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = ordersMapper.getCountByMap(map);
            validOrderCountList.add(validOrderCount);

            map.put("status", null);
            Integer orderCount = ordersMapper.getCountByMap(map);
            orderCountList.add(orderCount);
        }
        orderReportVO.setOrderCountList(StringUtils.join(orderCountList,","));
        orderReportVO.setValidOrderCountList(StringUtils.join(validOrderCountList,","));


        map.put("begin", begin);
        map.put("end", end);
        Integer totalOrderCount = ordersMapper.getCountByMap(map);
        //Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        orderReportVO.setTotalOrderCount(totalOrderCount);


        map.put("status", Orders.COMPLETED);
        Integer validOrderCount = ordersMapper.getCountByMap(map);
        //Integer validOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        orderReportVO.setValidOrderCount(validOrderCount);




        Double orderCompletionRate = 0.0;
        if(totalOrderCount!=0){
            orderCompletionRate = validOrderCount.doubleValue()/totalOrderCount;
        }
        orderReportVO.setOrderCompletionRate(orderCompletionRate);

        return orderReportVO;
    }
}
