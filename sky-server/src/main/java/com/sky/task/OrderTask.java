package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Resource
    OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void orderCancel() {
        log.info("orderCancel execute at {}", LocalDateTime.now());
        LocalDateTime dateTime = LocalDateTime.now().plusMinutes(-15);
        orderMapper.updateByStatusAndTimeLt(Orders.PENDING_PAYMENT, dateTime, Orders.CANCELLED, "订单超时, 自动取消", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void orderConfirm() {
        log.info("orderConfirm execute at {}", LocalDateTime.now());
        LocalDateTime dateTime = LocalDateTime.now().plusMinutes(-60);
        orderMapper.updateByAutoCompleted(Orders.DELIVERY_IN_PROGRESS, dateTime, Orders.COMPLETED);
    }

}
