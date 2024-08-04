package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /**
     * 根据状态统计订单数量
     * @param status
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    Double turnoverStatistics(Map map);

    Integer cntOrders(Map map);

    List<GoodsSalesDTO> goodsSalesDTOS(Map map);
}
