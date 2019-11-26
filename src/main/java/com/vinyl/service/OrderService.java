package com.vinyl.service;

import com.vinyl.model.Order;

import java.util.Optional;

public interface OrderService {
    Optional<Order> findById(Long id);
}
