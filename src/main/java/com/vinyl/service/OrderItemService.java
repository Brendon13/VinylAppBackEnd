package com.vinyl.service;

import com.vinyl.model.OrderItem;

import java.util.List;

public interface OrderItemService {
    List<OrderItem> findByOrderId(Long orderId);
    OrderItem findByItemId(Long itemId);
}
