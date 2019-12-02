package com.vinyl.service;

import com.vinyl.model.Order;
import com.vinyl.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public Order findById(Long Id){ return orderRepository.getOne(Id); }

    @Override
    public void save(Order order){
        orderRepository.save(order);
    }
}
