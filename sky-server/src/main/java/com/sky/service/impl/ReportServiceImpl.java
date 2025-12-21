package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    OrdersMapper ordersMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    WorkspaceService workspaceService;


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

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        Map map = new HashMap();
        map.put("begin", LocalDateTime.of(begin, LocalTime.MIN));
        map.put("end", LocalDateTime.of(end, LocalTime.MAX));
        map.put("status", Orders.COMPLETED);
        List<GoodsSalesDTO> goodsSalesDTOList = ordersMapper.getTop10Dish(map);

        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();

//        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOList) {
//            nameList.add(goodsSalesDTO.getName());
//            numberList.add(goodsSalesDTO.getNumber());
//        }

        nameList = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        numberList = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        log.debug("长度是{}", nameList.size());
        log.debug("长度是{}", numberList.size());




        salesTop10ReportVO = SalesTop10ReportVO.builder().nameList(StringUtils.join(nameList,",")).numberList(StringUtils.join(numberList,",")).build();
        return salesTop10ReportVO;


    }

    @Override
    public void export(HttpServletResponse response) {
        LocalDate now = LocalDate.now();
        LocalDate begin = now.minusDays(30);
        LocalDate end = now.minusDays(1);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.businessData(beginTime, endTime);
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/report_template.xlsx");

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            XSSFRow row = sheet.getRow(1);
            row.getCell(1).setCellValue("时间: " + begin + "至" + end);

            row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());


            for(int i=0;i<30;i++){
                LocalDate date = begin.plusDays(i);
                //查询某一天的营业数
                BusinessDataVO businessDatavo = workspaceService.businessData(LocalDateTime.of(date,LocalTime.MIN),LocalDateTime.of(date,LocalTime.MAX));
                //获得某一行
                row = sheet.getRow(7+i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessDatavo.getTurnover());
                row.getCell(3).setCellValue(businessDatavo.getValidOrderCount());
                row.getCell(4).setCellValue(businessDatavo.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDatavo.getUnitPrice());
                row.getCell(6).setCellValue(businessDatavo.getNewUsers());
            }

            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);

            outputStream.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
