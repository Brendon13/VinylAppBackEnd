package com.vinyl.model;

import javax.persistence.*;


@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long quantity;

    @ManyToOne
    @JoinColumn(name="orders_id", nullable=false, foreignKey=@ForeignKey(name = "Fk_order_item_orders_id"))
    private Order order;

    @ManyToOne
    @JoinColumn(name="item_id", nullable=false, foreignKey=@ForeignKey(name = "Fk_order_item_item_id"))
    private Item item;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}

