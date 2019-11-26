package com.vinyl.repository;

import com.vinyl.model.Cart;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends CrudRepository<Cart, Long> {
    Cart findByUserId(Long userId);
}
