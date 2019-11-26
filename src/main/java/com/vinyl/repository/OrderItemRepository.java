package com.vinyl.repository;

import com.vinyl.model.OrderItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends CrudRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    OrderItem findByItemId(Long itemId);
}

