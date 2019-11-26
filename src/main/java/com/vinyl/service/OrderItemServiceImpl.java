package com.vinyl.service;

import com.vinyl.model.OrderItem;
import com.vinyl.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService{
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public List<OrderItem> findByOrderId(Long orderId){
        return (List<OrderItem>) orderItemRepository.findByOrderId(orderId);
    }

    @Override
    public OrderItem findByItemId(Long itemId){
        return orderItemRepository.findByItemId(itemId);
    }
}
