package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.DocumentationCache;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Resource
    OrderMapper orderMapper;

    @Resource
    UserMapper userMapper;

    @Autowired
    private DocumentationCache resourceGroupCache;

    @Resource
    WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {

        // dateList
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // turnoverList
        List<Double> turnoverList = new ArrayList<>();
        for(LocalDate date:dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.turnoverStatistics(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {

        // dateList
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // totalUserList & addUserList = cur - prev
        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> addUserList = new ArrayList<>();

        // prevCnt
        LocalDateTime prev = LocalDateTime.of(dateList.get(0).plusDays(-1), LocalTime.MAX);
        Integer prvCnt = userMapper.countByDate(prev);

        for(LocalDate date:dateList) {
            LocalDateTime curTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer curCnt = userMapper.countByDate(curTime);
            totalUserList.add(curCnt);
            addUserList.add(curCnt - prvCnt);
            prvCnt = curCnt;
        }


        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(addUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {

        // dateList
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
//            System.out.println(begin);
        }

        //
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totals = 0;
        Integer valids = 0;
        for( LocalDate date:dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Integer total = orderMapper.cntOrders(map);
            totals += total;
            orderCountList.add(total);
            map.put("status", Orders.COMPLETED);
            Integer valid = orderMapper.cntOrders(map);
            valids += valid;
            validOrderCountList.add(valid);
        }
        Double rate = 0.0;
        if (totals != 0) {
            rate = Double.valueOf(valids)/totals;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totals)
                .validOrderCount(valids)
                .orderCompletionRate(rate)
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        Map map = new HashMap<>();
        map.put("begin", beginTime);
        map.put("end", endTime);
        map.put("status", Orders.COMPLETED);
        List<GoodsSalesDTO> goodsSalesDTOS = orderMapper.goodsSalesDTOS(map);
        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        goodsSalesDTOS.stream().forEach(goodsSalesDTO -> {
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber());
        });

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    @Override
    public void export(HttpServletResponse httpServletResponse) {

        LocalDate beginDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(beginDate, LocalTime.MIN)
                , LocalDateTime.of(endDate, LocalTime.MAX));

        // 获取输入流
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/data.xlsx");

        try {

            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = excel.getSheetAt(0);

            sheet.getRow(1).getCell(1).setCellValue("时间：从"+beginDate+"至"+endDate);
            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());

            sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());

            int row = 7;
            for(int i=0; i<30; i++) {
                beginDate = beginDate.plusDays(1);
                BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(beginDate, LocalTime.MIN)
                        , LocalDateTime.of(beginDate, LocalTime.MAX));
                sheet.getRow(row).getCell(1).setCellValue(beginDate.toString());
                sheet.getRow(row).getCell(2).setCellValue(businessDataVO.getTurnover());
                sheet.getRow(row).getCell(3).setCellValue(businessDataVO.getValidOrderCount());
                sheet.getRow(row).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                sheet.getRow(row).getCell(5).setCellValue(businessDataVO.getUnitPrice());
                sheet.getRow(row).getCell(6).setCellValue(businessDataVO.getNewUsers());
                row ++;
            }


            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            excel.write(outputStream);
            outputStream.close();
            inputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
