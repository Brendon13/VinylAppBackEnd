package com.vinyl.model;

import javax.persistence.*;

@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey=@ForeignKey(name = "Fk_cart_user_id"))
    private User user;

    @OneToOne
    @JoinColumn(name = "orders_id", nullable = false, foreignKey=@ForeignKey(name = "Fk_cart_orders_id"))
    private Order order;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
