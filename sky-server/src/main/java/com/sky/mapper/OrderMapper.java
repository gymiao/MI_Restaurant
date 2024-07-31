package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);

    @Select("select * from orders where number=#{outTradeNo}")
    Orders getByNumber(String outTradeNo);

    void update(Orders orders);

    @Select("select * from orders where status = #{pendingPayment} and order_time<#{dateTime}")
    List<Orders> selectByStatusAndTimeLt(Integer pendingPayment, LocalDateTime dateTime);

    @Update("update orders set status=#{cancelled}, cancel_reason=#{s}, cancel_time=#{now}  where status = #{pendingPayment} and order_time<#{dateTime}")
    void updateByStatusAndTimeLt(Integer pendingPayment, LocalDateTime dateTime, Integer cancelled, String s, LocalDateTime now);

    @Update("update orders set status=#{completed} where status=#{deliveryInProgress} and order_time<#{dateTime}")
    void updateByAutoCompleted(Integer deliveryInProgress, LocalDateTime dateTime, Integer completed);
}
